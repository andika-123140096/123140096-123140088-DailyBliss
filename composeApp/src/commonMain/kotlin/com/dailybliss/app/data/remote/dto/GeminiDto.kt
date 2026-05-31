package com.dailybliss.app.data.remote.dto

import kotlinx.serialization.Serializable

// ==================== REQUEST ====================

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null,
    val systemInstruction: GeminiSystemInstruction? = null,
    val tools: List<GeminiTool>? = null,
)

@Serializable
data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>,
)

@Serializable
data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: GeminiFunctionParameters? = null,
)

@Serializable
data class GeminiFunctionParameters(
    val type: String = "object",
    val properties: Map<String, GeminiFunctionProperty>? = null,
    val required: List<String>? = null,
)

@Serializable
data class GeminiFunctionProperty(
    val type: String,
    val description: String? = null,
    val enum: List<String>? = null,
)

@Serializable
data class GeminiSystemInstruction(val parts: List<GeminiPart>)

@Serializable
data class GeminiContent(val parts: List<GeminiPart>, val role: String = "model")

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null,
    val thought: Boolean? = null,
    val functionCall: GeminiFunctionCall? = null,
    val functionResponse: GeminiFunctionResponse? = null,
)

@Serializable
data class GeminiFunctionCall(
    val name: String,
    val args: kotlinx.serialization.json.JsonObject? = null,
)

@Serializable
data class GeminiFunctionResponse(
    val name: String,
    val response: kotlinx.serialization.json.JsonObject,
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String, // Base64
)

@Serializable
data class GenerationConfig(
    val temperature: Double = 0.7,
    val maxOutputTokens: Int = 2000,
    val topP: Double = 0.95,
    val topK: Int = 40,
)

@Serializable
data class SafetySetting(val category: String, val threshold: String)

// ==================== RESPONSE ====================

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null,
    val promptFeedback: PromptFeedback? = null,
    val error: GeminiError? = null,
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String? = null,
    val index: Int = 0,
    val safetyRatings: List<SafetyRating>? = null,
)

@Serializable
data class SafetyRating(val category: String, val probability: String)

@Serializable
data class PromptFeedback(val safetyRatings: List<SafetyRating>? = null, val blockReason: String? = null)

@Serializable
data class GeminiError(val code: Int, val message: String, val status: String)

// ==================== HELPER EXTENSIONS ====================

fun GeminiResponse.getTextContent(): String? = candidates?.firstOrNull()?.content?.parts?.firstOrNull {
    it.text != null && it.thought != true
}?.text

fun GeminiResponse.getFunctionCall(): GeminiFunctionCall? = candidates?.firstOrNull()?.content?.parts?.firstOrNull {
    it.functionCall != null
}?.functionCall

fun GeminiResponse.isBlocked(): Boolean = promptFeedback?.blockReason != null

fun GeminiResponse.getErrorMessage(): String? = error?.message ?: if (isBlocked()) {
    "Konten diblokir: ${promptFeedback?.blockReason}"
} else {
    null
}
