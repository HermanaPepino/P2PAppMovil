package com.example.p2pappmovil.data.gemini

import com.example.p2pappmovil.BuildConfig
import com.example.p2pappmovil.data.model.SupportMessage

class GeminiRepository {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val service = GeminiRetrofitClient.service

    private val systemPrompt = """
        Eres el asistente virtual oficial de CambioSeguro.
        CambioSeguro es una aplicación móvil P2P para intercambio de divisas.
        Funciones disponibles:
        • Marketplace
        • Publicar ofertas
        • Compra de dólares
        • Venta de dólares
        • Inicio de operaciones
        • Historial
        • Disputas
        • Centro de ayuda
        • Notificaciones
        • Perfil
        Tu trabajo consiste únicamente en responder preguntas relacionadas con la aplicación.
        No inventes funciones inexistentes.
        No respondas preguntas de cultura general.
        No respondas preguntas fuera del contexto de CambioSeguro.
        Si el usuario necesita intervención humana, recomiéndale abrir un ticket de soporte.
    """.trimIndent()

    suspend fun getAiResponse(history: List<SupportMessage>): String {
        val lastUserMessage = history.lastOrNull { it.sender == "USER" }?.text?.lowercase() ?: ""
        
        return try {
            val contents = history.map { message ->
                Content(
                    role = if (message.sender == "USER") "user" else "model",
                    parts = listOf(Part(text = message.text))
                )
            }

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val apiResponse = service.generateContent(apiKey, request)
            var textResponse = apiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Lo siento, no pude generar una respuesta. Por favor, intenta de nuevo."

            // Detección de casos para soporte
            val supportKeywords = listOf(
                "fraude", "estafa", "reclamo", "denuncia", "problema grave", 
                "administrador", "ticket", "disputa", "no recibí el dinero", 
                "el comprador no pagó"
            )

            if (supportKeywords.any { lastUserMessage.contains(it) }) {
                textResponse += "\n\nEste caso requiere revisión por parte de un administrador. Te recomiendo abrir un ticket de soporte."
            }

            textResponse
        } catch (e: Exception) {
            "Ocurrió un error al conectar con el asistente: ${e.localizedMessage}. Por favor, verifica tu conexión o intenta más tarde."
        }
    }
}
