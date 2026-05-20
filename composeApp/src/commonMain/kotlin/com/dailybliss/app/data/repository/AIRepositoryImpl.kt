package com.dailybliss.app.data.repository

import com.dailybliss.app.core.util.toBase64
import com.dailybliss.app.data.local.datastore.UserPreferences
import com.dailybliss.app.data.remote.api.GeminiService
import com.dailybliss.app.data.remote.api.SystemPrompts
import com.dailybliss.app.data.remote.dto.GeminiContent
import com.dailybliss.app.data.remote.dto.GeminiInlineData
import com.dailybliss.app.data.remote.dto.GeminiPart
import com.dailybliss.app.domain.repository.AIRepository
import com.dailybliss.app.domain.repository.MoodResult
import com.dailybliss.app.presentation.screens.ai.ChatMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class AIRepositoryImpl(private val geminiService: GeminiService, private val userPreferences: UserPreferences) : AIRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun streamChat(messages: List<ChatMessage>): Flow<String> {
        val geminiContents = mapToGeminiContents(messages)
        return geminiService.streamContent(
            contents = geminiContents,
            systemPrompt = getDynamicSystemPrompt(),
        )
    }

    override suspend fun chat(messages: List<ChatMessage>): String {
        val geminiContents = mapToGeminiContents(messages)
        return geminiService
            .generateChat(
                contents = geminiContents,
                systemPrompt = getDynamicSystemPrompt(),
            ).getOrThrow()
    }

    override suspend fun analyzeMood(content: String, imageBytes: ByteArray?): MoodResult? {
        val parts = mutableListOf<GeminiPart>()
        if (imageBytes != null) {
            parts.add(GeminiPart(inline_data = GeminiInlineData("image/jpeg", imageBytes.toBase64())))
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
            } catch (e: Exception) {
                null
            }
        }
    }

    override suspend fun generateTags(content: String, imageBytes: ByteArray?): List<String> {
        val parts = mutableListOf<GeminiPart>()
        if (imageBytes != null) {
            parts.add(GeminiPart(inline_data = GeminiInlineData("image/jpeg", imageBytes.toBase64())))
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
                println("AIRepository: operation failed: ${e.message}")
                emptyList()
            }
        } ?: emptyList()
    }

    override suspend fun generateDailyPrompt(): String? {
        val result =
            geminiService
                .generateContent(
                    parts = listOf(GeminiPart(text = "Berikan aku satu pertanyaan hari ini.")),
                    systemPrompt = SystemPrompts.DAILY_PROMPT_GENERATION,
                ).getOrNull()

        return result?.let {
            try {
                val jsonStr = it.replace("```json", "").replace("```", "").trim()
                json.decodeFromString<PromptResponse>(jsonStr).prompt
            } catch (e: Exception) {
                null
            }
        }
    }

    @Serializable
    private data class MoodResponse(val mood: String, val emoji: String)

    @Serializable
    private data class TagsResponse(val tags: List<String>)

    @Serializable
    private data class PromptResponse(val prompt: String)

    private suspend fun getDynamicSystemPrompt(): String {
        val nickname = userPreferences.nickname.first()
        val style = userPreferences.aiLanguageStyle.first()

        return """
            ${SystemPrompts.CHAT_SYSTEM_PROMPT}
            
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
                    inline_data =
                    GeminiInlineData(
                        mime_type = "image/jpeg",
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
            role = if (chatMessage.role == "user") "user" else "model",
        )
    }
}
