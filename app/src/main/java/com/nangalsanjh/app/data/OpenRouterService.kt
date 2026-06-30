package com.nangalsanjh.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object OpenRouterService {


    private const val API_KEY = "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"
    private const val API_URL = "https://openrouter.ai/api/v1/chat/completions"

    suspend fun sendMessage(
        userMessage: String,
        chatHistory: List<Pair<String, String>>
    ): String = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $API_KEY")
            connection.setRequestProperty("HTTP-Referer", "https://nangalseva.com")
            connection.setRequestProperty("X-Title", "Nangal Seva")
            connection.doOutput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000

            // Build messages array
            val messagesArray = JSONArray()

            // System message
            val systemMessage = JSONObject().apply {
                put("role", "system")
                put("content", "You are Nangal Seva AI Assistant. Answer every question fully and accurately. Use Hinglish or Punjabi if the user speaks Hindi.")
            }
            messagesArray.put(systemMessage)

            // Chat history
            for (entry in chatHistory) {
                val msg = JSONObject().apply {
                    put("role", entry.first)
                    put("content", entry.second)
                }
                messagesArray.put(msg)
            }

            // User message
            val userMsg = JSONObject().apply {
                put("role", "user")
                put("content", userMessage)
            }
            messagesArray.put(userMsg)

            // ✅ FINAL WORKING MODEL: openrouter/free (auto-selects best free model)
            val requestBody = JSONObject().apply {
                put("model", "openrouter/free")  // ✅ This ALWAYS works!
                put("messages", messagesArray)
                put("max_tokens", 8192)
                put("temperature", 0.7f)
            }

            println("🔥 OpenRouter Request Body: ${requestBody.toString()}")

            // Send request
            connection.outputStream.write(requestBody.toString().toByteArray())

            // Read response
            val responseCode = connection.responseCode
            val inputStream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = inputStream.bufferedReader().readText()

            println("🔥 OpenRouter Response Code: $responseCode")
            println("🔥 OpenRouter Response Body: $response")

            val jsonResponse = JSONObject(response)

            // Check for error
            if (jsonResponse.has("error")) {
                val error = jsonResponse.getJSONObject("error")
                val errorMessage = error.optString("message", "Unknown API error")
                return@withContext "❌ $errorMessage"
            }

            // Check for choices
            if (!jsonResponse.has("choices")) {
                return@withContext "❌ No 'choices' in response. Please check your API key and credits."
            }

            val choices = jsonResponse.getJSONArray("choices")
            if (choices.length() == 0) {
                return@withContext "❌ No choices returned. Try again."
            }

            val message = choices.getJSONObject(0).getJSONObject("message")
            message.getString("content")

        } catch (e: Exception) {
            println("🔥 OpenRouter Exception: ${e.localizedMessage}")
            "❌ Error: ${e.localizedMessage ?: "Network issue. Please try again."}"
        }
    }
}