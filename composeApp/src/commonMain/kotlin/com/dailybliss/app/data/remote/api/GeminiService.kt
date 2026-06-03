package com.dailybliss.app.data.remote.api

import com.dailybliss.app.core.network.ApiConfig
import com.dailybliss.app.core.util.decodeBase64
import com.dailybliss.app.data.remote.dto.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GeminiService(
    private val httpClient: HttpClient,
    private val apiConfig: ApiConfig,
) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        private const val MAX_RETRIES = 5
        private const val INITIAL_DELAY_MS = 1000L
    }

    private suspend fun <T> retryWithBackoff(
        maxRetries: Int = MAX_RETRIES,
        initialDelay: Long = INITIAL_DELAY_MS,
        block: suspend () -> T,
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw Exception("Gagal setelah beberapa percobaan")
    }

    suspend fun streamContent(contents: List<GeminiContent>, systemPrompt: String? = null): Flow<String> = flow {
        val modelName = apiConfig.geminiModelName.ifBlank { "gemini-1.5-flash" }

        // Use streamGenerateContent for streaming
        val url = "$BASE_URL/models/$modelName:streamGenerateContent?alt=sse"

        val request = GeminiRequest(
            contents = contents,
            systemInstruction = systemPrompt?.let {
                GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
            },
            generationConfig = GenerationConfig(
                temperature = 0.8,
                maxOutputTokens = 2000,
            ),
        )

        retryWithBackoff {
            httpClient.preparePost(url) {
                header("x-goog-api-key", apiConfig.geminiApiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
                timeout {
                    requestTimeoutMillis = 60_000
                    socketTimeoutMillis = 60_000
                }
            }.execute { response ->
                if (response.status != HttpStatusCode.OK) {
                    val errorBody = response.bodyAsText()
                    throw Exception("API Error (${response.status.value}): $errorBody")
                }

                val channel = response.bodyAsChannel()
                while (!channel.isClosedForRead) {
                    val line = channel.readUTF8Line()
                    if (line == null) {
                        break
                    }
                    if (line.startsWith("data: ")) {
                        val data = line.substring(6).trim()
                        if (data.isNotEmpty()) {
                            try {
                                val geminiResponse = json.decodeFromString<GeminiResponse>(data)
                                val text = geminiResponse.getTextContent()
                                if (text != null) {
                                    emit(text)
                                }
                            } catch (e: Exception) {
                                println("GeminiService: SSE stream parse error: ${e.message}")
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun generateContent(parts: List<GeminiPart>, systemPrompt: String? = null): Result<String> = runCatching {
        retryWithBackoff {
            val modelName = apiConfig.geminiModelName.ifBlank { "gemini-1.5-flash" }

            val contents = listOf(
                GeminiContent(
                    parts = parts,
                    role = "user",
                ),
            )

            val url = "$BASE_URL/models/$modelName:generateContent"

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemPrompt?.let {
                    GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
                },
            )

            val response: HttpResponse = httpClient.post(url) {
                header("x-goog-api-key", apiConfig.geminiApiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                throw Exception("API Error (${response.status.value}): $errorBody")
            }

            val geminiResponse = response.body<GeminiResponse>()
            geminiResponse.getErrorMessage()?.let { throw Exception(it) }
            geminiResponse.getTextContent() ?: throw Exception("Respons kosong")
        }
    }

    suspend fun generateChat(
        contents: List<GeminiContent>,
        systemPrompt: String? = null,
        tools: List<GeminiTool>? = null,
    ): Result<GeminiResponse> = runCatching {
        retryWithBackoff {
            val modelName = apiConfig.geminiModelName.ifBlank { "gemini-1.5-flash" }

            val url = "$BASE_URL/models/$modelName:generateContent"

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemPrompt?.let {
                    GeminiSystemInstruction(parts = listOf(GeminiPart(text = it)))
                },
                tools = tools,
            )

            val response: HttpResponse = httpClient.post(url) {
                header("x-goog-api-key", apiConfig.geminiApiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()
                throw Exception("API Error (${response.status.value}): $errorBody")
            }

            val geminiResponse = response.body<GeminiResponse>()
            geminiResponse.getErrorMessage()?.let { throw Exception(it) }
            geminiResponse
        }
    }

    suspend fun generateTTS(text: String, voiceName: String): Result<ByteArray> = runCatching {
        suspend fun attemptTTS(model: String): ByteArray {
            val url = "$BASE_URL/models/$model:generateContent"
            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = text)),
                        role = "user",
                    ),
                ),
                generationConfig = GenerationConfig(
                    responseModalities = listOf("AUDIO"),
                    speechConfig = SpeechConfig(
                        voiceConfig = VoiceConfig(
                            prebuiltVoiceConfig = PrebuiltVoiceConfig(
                                voiceName = voiceName,
                            ),
                        ),
                    ),
                ),
            )

            val response: HttpResponse = httpClient.post(url) {
                header("x-goog-api-key", apiConfig.geminiApiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (!response.status.isSuccess()) {
                val errorBody = response.bodyAsText()
                throw Exception("API Error (${response.status.value}): $errorBody")
            }

            val jsonString = response.bodyAsText()
            println("TTS Response received, parsing candidates...")

            val jsonObj = json.parseToJsonElement(jsonString).jsonObject
            val candidates = jsonObj["candidates"]?.jsonArray
            val firstCandidate = candidates?.get(0)?.jsonObject
            val content = firstCandidate?.get("content")?.jsonObject
            val parts = content?.get("parts")?.jsonArray

            var dataStr: String? = null
            parts?.forEach { partElement ->
                val partObj = partElement.jsonObject
                val inlineData = partObj["inlineData"]?.jsonObject
                val data = inlineData?.get("data")?.jsonPrimitive?.content
                if (!data.isNullOrEmpty()) {
                    dataStr = data
                    println("Audio inlineData found")
                    return@forEach
                }
            }

            if (dataStr.isNullOrEmpty()) {
                println("Failed to get audio inlineData: \n$jsonString")
                throw Exception("Gagal mengekstrak audio dari response")
            }

            val decoded = dataStr!!.decodeBase64()
            println("Audio bytes decoded size: ${decoded.size}")
            return decoded
        }

        try {
            attemptTTS("gemini-3.1-flash-tts-preview")
        } catch (e: Exception) {
            println("GeminiService TTS fallback to 2.5 flash: ${e.message}")
            attemptTTS("gemini-2.5-flash-preview-tts")
        }
    }
}
