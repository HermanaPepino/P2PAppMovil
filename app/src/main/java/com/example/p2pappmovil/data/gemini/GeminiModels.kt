package com.example.p2pappmovil.data.gemini

import com.google.gson.annotations.SerializedName

data class GeminiRequest(
    @SerializedName("contents") val contents: List<Content>,
    @SerializedName("systemInstruction") val systemInstruction: Content? = null
)

data class Content(
    @SerializedName("role") val role: String? = null,
    @SerializedName("parts") val parts: List<Part>
)

data class Part(
    @SerializedName("text") val text: String
)

data class GeminiResponse(
    @SerializedName("candidates") val candidates: List<Candidate>
)

data class Candidate(
    @SerializedName("content") val content: Content,
    @SerializedName("finishReason") val finishReason: String? = null
)
