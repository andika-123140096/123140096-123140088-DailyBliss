package com.dailybliss.app.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.dailybliss.app.core.util.toBase64
import com.dailybliss.app.data.local.BlissDatabase
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.data.remote.api.GeminiService
import com.dailybliss.app.data.remote.api.SystemPrompts
import com.dailybliss.app.data.remote.api.AITools
import com.dailybliss.app.data.remote.dto.*
import com.dailybliss.app.domain.model.ChatMessage
import com.dailybliss.app.domain.model.ChatSession
import com.dailybliss.app.domain.repository.*
import com.dailybliss.app.presentation.util.FileStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

class AIRepositoryImpl(
    private val geminiService: GeminiService,
    private val userPreferences: UserPreferences,
    private val database: BlissDatabase,
    private val fileStorage: FileStorage,
    private val momentRepository: MomentRepository,
    private val newsRepository: NewsRepository,
    private val weatherRepository: WeatherRepository,
    private val currencyRepository: CurrencyRepository,
    private val applicationScope: CoroutineScope,
) : AIRepository {
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    private val chatQueries = database.chatQueries

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    override val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _currentSessionId = MutableStateFlow<Long?>(null)
    override val currentSessionId: StateFlow<Long?> = _currentSessionId.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    override val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    override fun getAllChatSessions(): Flow<List<ChatSession>> {
        return chatQueries.getAllChatSessions { id, title, createdAt, updatedAt ->
            ChatSession(id, title, createdAt, updatedAt)
        }.asFlow().mapToList(Dispatchers.Default)
    }

    override fun loadSession(sessionId: Long) {
        applicationScope.launch {
            val entities = chatQueries.getMessagesBySessionId(sessionId).executeAsList()
            val messages = entities.map { entity ->
                val imageBytes = entity.image_path?.let { fileStorage.loadImage(it) }
                ChatMessage(
                    role = entity.role,
                    text = entity.text_content,
                    imageBytes = imageBytes,
                    imagePath = entity.image_path,
                    isError = entity.is_error != 0L,
                )
            }

            _chatMessages.value = messages
            _currentSessionId.value = sessionId
        }
    }

    override suspend fun deleteSession(sessionId: Long) {
        chatQueries.deleteChatSession(sessionId)
        if (_currentSessionId.value == sessionId) {
            startNewSession()
        }
    }

    override fun startNewSession() {
        _chatMessages.value = emptyList()
        _currentSessionId.value = null
        _isChatLoading.value = false
    }

    override suspend fun chat(
        messages: List<ChatMessage>,
        tools: List<GeminiTool>?
    ): GeminiResponse {
        val geminiContents = mapToGeminiContents(messages)
        return geminiService
            .generateChat(
                contents = geminiContents,
                systemPrompt = getDynamicSystemPrompt(),
                tools = tools
            ).getOrThrow()
    }

    override fun sendMessage(text: String, imageBytes: ByteArray?) {
        applicationScope.launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val imagePath = imageBytes?.let { fileStorage.saveImage(it) }

            val userMessage = ChatMessage(
                role = "user",
                text = text.trim(),
                imageBytes = imageBytes,
                imagePath = imagePath,
            )

            // 1. Update UI immediately for instant feedback
            val placeholderModelMessage = ChatMessage(role = "model", text = "")
            _chatMessages.update { it + userMessage + placeholderModelMessage }
            _isChatLoading.value = true

            try {
                var sessionId = _currentSessionId.value

                // 2. Handle Session Creation in background
                if (sessionId == null) {
                    val title = text.take(30).ifBlank { "Gambar" }
                    chatQueries.insertChatSession(title, now, now)
                    sessionId = chatQueries.lastInsertId().executeAsOne()
                    _currentSessionId.value = sessionId
                    
                    // Generate better title in background without blocking
                    launch {
                        generateChatTitle(text)?.let { betterTitle ->
                            chatQueries.updateChatSessionTitle(betterTitle, now, sessionId)
                        }
                    }
                }

                // 3. Save user message to DB
                chatQueries.insertChatMessage(
                    session_id = sessionId!!,
                    role = userMessage.role,
                    text_content = userMessage.text,
                    image_path = userMessage.imagePath,
                    is_error = 0,
                    created_at = now,
                )
                chatQueries.updateChatSessionTimestamp(now, sessionId)

                yield()
                var attempt = 1
                while (true) {
                    try {
                        val history = _chatMessages.value.dropLast(1)
                        val messagesForAI = mapToGeminiContents(history).toMutableList()
                        
                        // Initial request with Tools
                        var currentResponse: GeminiResponse = chat(messages = history, tools = AITools.ALL_TOOLS)
                        
                        var loopCount = 0
                        while (currentResponse.getFunctionCall() != null && loopCount < 5) {
                            val functionCall = currentResponse.getFunctionCall()!!
                            messagesForAI.add(GeminiContent(role = "model", parts = listOf(GeminiPart(functionCall = functionCall))))
                            
                            val result = handleFunctionCall(functionCall)
                            messagesForAI.add(GeminiContent(role = "function", parts = listOf(GeminiPart(functionResponse = GeminiFunctionResponse(functionCall.name, result)))))
                            
                            currentResponse = geminiService.generateChat(
                                contents = messagesForAI,
                                systemPrompt = getDynamicSystemPrompt(),
                                tools = AITools.ALL_TOOLS
                            ).getOrThrow()
                            loopCount++
                        }

                        val fullResponse = currentResponse.getTextContent() ?: ""
                        val modelMessage = ChatMessage(role = "model", text = fullResponse)
                        
                        // Save model response to DB
                        chatQueries.insertChatMessage(
                            session_id = sessionId,
                            role = modelMessage.role,
                            text_content = modelMessage.text,
                            image_path = null,
                            is_error = 0,
                            created_at = Clock.System.now().toEpochMilliseconds(),
                        )

                        _chatMessages.update { messages ->
                            val updated = messages.toMutableList()
                            if (updated.isNotEmpty()) {
                                updated[updated.lastIndex] = updated[updated.lastIndex].copy(text = fullResponse, isError = false)
                            }
                            updated
                        }
                        _isChatLoading.value = false
                        break
                    } catch (e: Exception) {
                        _chatMessages.update { messages ->
                            val updated = messages.toMutableList()
                            if (updated.isNotEmpty()) {
                                updated[updated.lastIndex] = updated[updated.lastIndex].copy(
                                    text = "Koneksi terputus (Percobaan $attempt): ${e.message}. Mencoba menghubungkan kembali...",
                                    isError = true,
                                )
                            }
                            updated
                        }
                        attempt++
                        if (attempt > 3) {
                            _isChatLoading.value = false
                            break
                        }
                        delay(2000)
                    }
                }
            } catch (e: Exception) {
                _isChatLoading.value = false
                // Handle session/db errors
            }
        }
    }

    override fun clearChat() {
        startNewSession()
    }

    private suspend fun generateChatTitle(firstMessage: String): String? {
        val parts = listOf(GeminiPart(text = firstMessage))
        return geminiService
            .generateContent(
                parts = parts,
                systemPrompt = SystemPrompts.CHAT_TITLE_PROMPT,
            ).getOrNull()?.trim()?.removeSurrounding("\"")
    }

    override suspend fun analyzeMood(content: String, imageBytes: ByteArray?): MoodResult? {
        val parts = mutableListOf<GeminiPart>()
        if (imageBytes != null) {
            parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", imageBytes.toBase64())))
        }
        parts.add(GeminiPart(text = content))

        val result =
            geminiService
                .generateContent(
                    parts = parts,
                    systemPrompt = SystemPrompts.MOOD_ANALYSIS_PROMPT,
                ).getOrNull()

        return result?.let {
            try {
                // Remove Markdown code blocks if present
                val jsonStr = it.replace("```json", "").replace("```", "").trim()
                json.decodeFromString<MoodResponse>(jsonStr).let { response ->
                    MoodResult(response.mood, response.emoji)
                }
            } catch (ignore: Exception) {
                null
            }
        }
    }

    override suspend fun generateTags(content: String, imageBytes: ByteArray?): List<String> {
        val parts = mutableListOf<GeminiPart>()
        if (imageBytes != null) {
            parts.add(GeminiPart(inlineData = GeminiInlineData("image/jpeg", imageBytes.toBase64())))
        }
        parts.add(GeminiPart(text = content))

        val result =
            geminiService
                .generateContent(
                    parts = parts,
                    systemPrompt = SystemPrompts.TAG_GENERATION_PROMPT,
                ).getOrNull()

        return result?.let {
            try {
                val jsonStr = it.replace("```json", "").replace("```", "").trim()
                json.decodeFromString<TagsResponse>(jsonStr).tags
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()
    }

    override suspend fun generateGlobalSummary(momentsText: String): String? {
        val parts = listOf(GeminiPart(text = momentsText))
        return geminiService
            .generateContent(
                parts = parts,
                systemPrompt = SystemPrompts.JOURNAL_SUMMARY_PROMPT,
            ).getOrNull()
    }

    override suspend fun generateDailyInsight(momentsText: String): String? {
        val parts = listOf(GeminiPart(text = momentsText))
        return geminiService
            .generateContent(
                parts = parts,
                systemPrompt = SystemPrompts.DAILY_INSIGHT_PROMPT,
            ).getOrNull()
    }

    @Serializable
    private data class MoodResponse(val mood: String, val emoji: String)

    @Serializable
    private data class TagsResponse(val tags: List<String>)

    private suspend fun handleFunctionCall(functionCall: GeminiFunctionCall): JsonObject {
        val args = functionCall.args ?: JsonObject(emptyMap())
        
        return when (functionCall.name) {
            "get_moments" -> {
                val keyword = args["keyword"]?.jsonPrimitive?.contentOrNull
                
                val moments = momentRepository.getAllMoments().first().filter { 
                    keyword == null || it.content.contains(keyword, ignoreCase = true) 
                }
                
                buildJsonObject {
                    put("moments", buildJsonArray {
                        moments.take(5).forEach { moment ->
                            addJsonObject {
                                put("date", moment.createdAt.toString())
                                put("content", moment.content)
                                put("mood", moment.mood)
                            }
                        }
                    })
                }
            }
            "get_news" -> {
                val news = newsRepository.getPrabowoNews()
                
                buildJsonObject {
                    put("news", buildJsonArray {
                        news.take(5).forEach { item ->
                            addJsonObject {
                                put("title", item.title)
                                put("description", item.summary)
                            }
                        }
                    })
                }
            }
            "get_weather" -> {
                val location = args["location"]?.jsonPrimitive?.contentOrNull ?: "Jakarta"
                val weather = weatherRepository.getCurrentWeather(location)
                buildJsonObject {
                    put("result", weather)
                }
            }
            "get_currency_rate" -> {
                val base = args["base"]?.jsonPrimitive?.contentOrNull ?: "USD"
                val target = args["target"]?.jsonPrimitive?.contentOrNull ?: "IDR"
                val rate = currencyRepository.getExchangeRate(base, target)
                buildJsonObject {
                    put("result", rate)
                }
            }
            else -> buildJsonObject { put("error", "Fungsi tidak ditemukan") }
        }
    }

    private suspend fun getDynamicSystemPrompt(): String {
        val nickname = userPreferences.nickname.first()
        val style = userPreferences.aiLanguageStyle.first()
        val journalSummary = userPreferences.journalSummary.first()
        val nowInstant = Clock.System.now()
        val currentDateTime = nowInstant.toString()

        val summaryContext = if (journalSummary.isNotBlank()) {
            """
            RINGKASAN JURNAL PENGGUNA (Gunakan ini sebagai konteks memori):
            $journalSummary
            """
        } else {
            ""
        }

        return """
            ${SystemPrompts.CHAT_SYSTEM_PROMPT}

            KONTEKS WAKTU SAAT INI: $currentDateTime
            (Gunakan informasi ini untuk menjawab pertanyaan tentang 'hari ini', 'besok', atau 'saat ini').

            $summaryContext

            PANDUAN KHUSUS UNTUK $nickname:
            - Kamu sedang berbicara dengan: $nickname.
            - Gaya bahasa WAJIB: $style.

            DEFINISI GAYA BAHASA '$style' (Ikuti dengan ketat):
            1. 'Santai/Kasual': Gunakan bahasa percakapan sehari-hari yang akrab namun sopan. Boleh gunakan kata seperti 'banget', 'kok', 'sih'. Hindari bahasa yang terlalu alay/lebay. Anggap $nickname adalah teman dekat.
            2. 'Formal/Baku': Gunakan kosakata bahasa Indonesia yang standar (EYD). Gunakan kalimat yang lengkap dan tertata. Tetap hangat, tapi pertahankan profesionalisme. Cocok untuk refleksi serius.
            3. 'Puitis/Puitik': Gunakan diksi yang indah, lembut, dan penuh makna. Gunakan sedikit metafora alam atau perasaan. Fokus pada ketenangan dan keindahan momen kecil.

            CATATAN PENTING:
            - Jangan berlebihan (jangan 'lebay'). Tetaplah terasa natural seperti manusia, bukan AI yang dipaksakan.
            - Pastikan perbedaan antara gaya 'Santai' dan 'Puitis' sangat terasa jelas dari pilihan kata (diksi).
            - Selalu panggil nama '$nickname' dalam responmu agar terasa personal.
        """.trimIndent()
    }

    private fun mapToGeminiContents(messages: List<ChatMessage>): List<GeminiContent> = messages.map { chatMessage ->
        val parts = mutableListOf<GeminiPart>()

        chatMessage.imageBytes?.let { bytes ->
            parts.add(
                GeminiPart(
                    inlineData =
                    GeminiInlineData(
                        mimeType = "image/jpeg",
                        data = bytes.toBase64(),
                    ),
                ),
            )
        }

        val textContent =
            if (chatMessage.text.isBlank() && chatMessage.imageBytes != null) {
                "Jelaskan gambar ini."
            } else {
                chatMessage.text
            }

        if (textContent.isNotBlank()) {
            parts.add(GeminiPart(text = textContent))
        }

        GeminiContent(
            parts = parts,
            role = when (chatMessage.role) {
                "user" -> "user"
                "model" -> "model"
                else -> "user" // Default to user if unknown
            },
        )
    }
}
