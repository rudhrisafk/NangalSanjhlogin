package com.nangalsanjh.app.data

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content

object GeminiService {

    private const val API_KEY = "AQ.Ab8RN6I3zFJFev_MUCacYodW3CFJ_QtjqfN4dpkZH9kx3U3OSw"

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = API_KEY,
        systemInstruction = content {
            text("""
                Tu Nangal Seva app ka AI assistant hai. 
                Nangal ek town hai Punjab mein.
            """.trimIndent())
        }
    )

    private var chat = model.startChat()

    suspend fun sendMessage(
        userMessage: String,
        chatHistory: List<Pair<String, String>>
    ): String {
        // Pehle local response try karo
        val localResponse = getLocalResponse(userMessage)
        if (localResponse != null) return localResponse

        // Phir API try karo
        return try {
            val response = chat.sendMessage(userMessage)
            response.text ?: "Kuch samajh nahi aaya, dobara try karo."
        } catch (e: Exception) {
            getFallbackResponse(userMessage)
        }
    }

    private fun getLocalResponse(message: String): String? {
        val msg = message.lowercase()
        return when {
            msg.contains("plumber") ->
                "🔧 Plumber ke liye:\n1. Services tab pe jao\n2. Plumber filter select karo\n3. Available worker ko call karo!"
            msg.contains("electrician") || msg.contains("bijli") || msg.contains("light") ->
                "⚡ Electrician ke liye:\n1. Services tab pe jao\n2. Electrician filter select karo\n3. Call karo!\n\nYa street light complaint ke liye Complaints tab pe jao."
            msg.contains("painter") ->
                "🎨 Painter ke liye Services tab pe jao aur Painter filter select karo!"
            msg.contains("carpenter") ->
                "🪚 Carpenter ke liye Services tab pe jao aur Carpenter filter select karo!"
            msg.contains("cleaner") || msg.contains("safai") ->
                "🧹 Cleaner ke liye Services tab pe jao aur Cleaner filter select karo!"
            msg.contains("pothole") || msg.contains("sadak") || msg.contains("road") ->
                "🕳️ Pothole complaint ke liye:\n1. Complaints tab pe jao\n2. + button dabao\n3. Category: Pothole select karo\n4. Location aur description likho\n5. Submit karo!"
            msg.contains("garbage") || msg.contains("kachra") ->
                "🗑️ Garbage complaint ke liye:\n1. Complaints tab pe jao\n2. + button dabao\n3. Category: Garbage select karo\n4. Submit karo!"
            msg.contains("water") || msg.contains("paani") || msg.contains("pipe") ->
                "💧 Water leakage complaint ke liye:\n1. Complaints tab pe jao\n2. + button dabao\n3. Category: Water Leakage select karo\n4. Submit karo!"
            msg.contains("complaint") ->
                "📋 Complaint karne ke liye:\n1. Complaints tab pe jao\n2. + button dabao\n3. Category select karo\n4. Description aur location likho\n5. Submit karo!"
            msg.contains("worker") || msg.contains("help") || msg.contains("app") ->
                "📱 App mein ye features hain:\n• Feed - Community posts\n• Services - Local workers\n• Complaints - Civic issues\n• Profile - Aapki details\n\nKya dhundh rahe ho?"
            msg.contains("hello") || msg.contains("hi") || msg.contains("sat sri") ->
                "Sat Sri Akaal! 🙏 Main Nangal Seva ka AI Assistant hoon!\nPlumber, electrician ya koi complaint ke baare mein poocho!"
            else -> null
        }
    }

    private fun getFallbackResponse(message: String): String {
        return "🤖 Abhi AI server busy hai. Lekin main help kar sakta hoon!\n\n" +
                "• Plumber/Worker ke liye → Services tab\n" +
                "• Complaint ke liye → Complaints tab → + button\n" +
                "• Posts ke liye → Feed tab\n\n" +
                "Kya chahiye specifically?"
    }

    fun resetChat() {
        chat = model.startChat()
    }
}