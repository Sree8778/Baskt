package com.example.supportagent.network.catalog

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object ItemQueryExtractor {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun extractItems(rawQuery: String, geminiApiKey: String): List<String> {
        if (geminiApiKey.isNotBlank()) {
            try {
                val prompt = """
                    Extract the individual grocery or shopping items from this user request as a JSON array of clean, concise search terms.
                    User request: "$rawQuery"
                    Respond ONLY with a valid JSON array of strings, e.g. ["whole milk", "grade a eggs", "dish soap"]. No explanation, no markdown backticks.
                """.trimIndent()

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$geminiApiKey"
                val jsonBody = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url(url)
                    .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respStr = response.body?.string() ?: ""
                        val json = JSONObject(respStr)
                        val candidates = json.optJSONArray("candidates")
                        val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val text = parts?.optJSONObject(0)?.optString("text")?.trim() ?: ""

                        val cleanedText = text.replace("```json", "").replace("```", "").trim()
                        val array = JSONArray(cleanedText)
                        val result = mutableListOf<String>()
                        for (i in 0 until array.length()) {
                            val item = array.optString(i).trim()
                            if (item.isNotBlank()) result.add(item)
                        }
                        if (result.isNotEmpty()) {
                            Log.d("ItemQueryExtractor", "Gemini extracted items: $result")
                            return result
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("ItemQueryExtractor", "Gemini item extraction failed, using fallback regex", e)
            }
        }

        return fallbackExtraction(rawQuery)
    }

    private fun fallbackExtraction(query: String): List<String> {
        val items = mutableListOf<String>()
        val lower = query.lowercase()

        val cleaned = lower
            .replace("put", "")
            .replace("add", "")
            .replace("in my walmart cart", "")
            .replace("in my cart", "")
            .replace("in my instacart", "")
            .replace("to my cart", "")
            .replace("to cart", "")
            .replace("can you", "")
            .replace("please", "")
            .replace("get me", "")
            .replace("i need", "")
            .replace("and", ",")
            .trim()

        val split = cleaned.split(",", ";").map { it.trim() }.filter { it.length > 1 }
        for (part in split) {
            val words = part.split(" ").filter { it.isNotBlank() }
            if (words.isNotEmpty()) {
                items.add(words.joinToString(" "))
            }
        }

        return if (items.isNotEmpty()) items else listOf(query.trim())
    }
}
