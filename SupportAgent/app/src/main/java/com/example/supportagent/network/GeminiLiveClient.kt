package com.example.supportagent.network

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.supportagent.engine.SupportAgentEngine
import com.example.supportagent.model.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

class GeminiLiveClient(
    private val apiKey: String,
    private val onMessageReceived: (String, ActionProposal?) -> Unit,
    private val onStatusChanged: (String) -> Unit
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var webSocket: WebSocket? = null
    private var isConnected = false
    private var accumulatedText = StringBuilder()

    fun connect() {
        if (apiKey.isBlank()) {
            notifyStatus("No API Key")
            return
        }

        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativelanguage.v1alpha.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder().url(url).build()

        notifyStatus("Connecting Live...")

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                isConnected = true
                Log.d("GeminiLive", "Connected to Gemini Live WebSocket")
                notifyStatus("Gemini Live Active")
                sendSetupHandshake()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("GeminiLive", "Received WebSocket message: $text")
                handleServerMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                isConnected = false
                Log.d("GeminiLive", "WebSocket Closing: code=$code reason=$reason")
                notifyStatus("Gemini 3.8 Flash Ready")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                isConnected = false
                val errorBody = try { response?.body?.string() ?: "" } catch (e: Exception) { "" }
                Log.e("GeminiLive", "WebSocket Failure: ${t.message} | Response: $errorBody", t)
                // Seamlessly falls back to Gemini 3.8 Flash REST
                notifyStatus("Gemini 3.8 Flash Ready")
            }
        })
    }

    private fun sendSetupHandshake() {
        try {
            val setupJson = JSONObject().apply {
                val setup = JSONObject().apply {
                    put("model", "models/gemini-3.8-flash")
                    put("generationConfig", JSONObject().apply {
                        put("responseModalities", JSONArray().put("TEXT"))
                        put("temperature", 0.4)
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply {
                            put("text", "You are ProxyAgent, an autonomous personal concierge on Android. When the user asks for shopping, food delivery, or groceries (e.g. Walmart, Uber Eats, Chipotle, Amazon, Costco): Your primary goal is to PUT ITEMS DIRECTLY INTO THEIR SHOPPING CART so they can review before checkout. Do NOT finalize orders; assemble the cart. 1) Explain your cart strategy concisely in 1-2 sentences. 2) ALWAYS call the `propose_action` tool with realistic parameters and the list of items.")
                        }))
                    })
                }
                put("setup", setup)
            }
            webSocket?.send(setupJson.toString())
        } catch (e: Exception) {
            Log.e("GeminiLive", "Error sending setup handshake", e)
        }
    }

    fun sendPrompt(userPrompt: String) {
        if (isConnected && webSocket != null) {
            accumulatedText.clear()
            try {
                val promptJson = JSONObject().apply {
                    val clientContent = JSONObject().apply {
                        val turns = JSONArray().apply {
                            put(JSONObject().apply {
                                put("role", "user")
                                put("parts", JSONArray().put(JSONObject().apply {
                                    put("text", userPrompt)
                                }))
                            })
                        }
                        put("turns", turns)
                        put("turnComplete", true)
                    }
                    put("clientContent", clientContent)
                }
                webSocket?.send(promptJson.toString())
                return
            } catch (e: Exception) {
                Log.e("GeminiLive", "Error sending via WebSocket, falling back to REST", e)
            }
        }

        // Reliable Gemini 3.8 Flash REST Pipeline with Function Calling
        sendPromptViaRest(userPrompt)
    }

    private fun sendPromptViaRest(userPrompt: String) {
        notifyStatus("Gemini 3.8 Reasoning...")

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent?key=$apiKey"

        val bodyJson = JSONObject().apply {
            put("system_instruction", JSONObject().apply {
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", "You are ProxyAgent, an autonomous personal concierge and support proxy on Android. For shopping, groceries, or food (e.g. Walmart, Uber Eats, Chipotle, Amazon, Costco): YOUR PRIMARY GOAL IS TO ASSEMBLE AND PUT ITEMS DIRECTLY INTO THEIR SHOPPING CART for their review. Do NOT finalize payment or checkout. 1) Explain which items you will put in their cart concisely in 1-2 sentences. 2) ALWAYS call the `propose_action` function with realistic parameters, listing the items in the `items` array, and setting `action_button_text` to e.g. '🛒 Put Items in Walmart Cart' or '🌯 Add Chipotle to Cart'.")
                }))
            })
            put("contents", JSONArray().put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().put(JSONObject().apply {
                    put("text", userPrompt)
                }))
            }))
            put("tools", JSONArray().put(JSONObject().apply {
                put("function_declarations", JSONArray().put(JSONObject().apply {
                    put("name", "propose_action")
                    put("description", "Generates a structured action proposal card for the user with a context-adaptive action button.")
                    put("parameters", JSONObject().apply {
                        put("type", "OBJECT")
                        val properties = JSONObject().apply {
                            put("title", JSONObject().put("type", "STRING").put("description", "Short title"))
                            put("category", JSONObject().put("type", "STRING").put("description", "SUPPORT_CALL, FOOD_DELIVERY, GROCERY_RESTOCK, TRAVEL_RENTAL, LODGING_RESERVATION, DISPUTE_REFUND"))
                            put("target_entity", JSONObject().put("type", "STRING").put("description", "Company or merchant name"))
                            put("target_contact", JSONObject().put("type", "STRING").put("description", "Phone number, URL, or service desk"))
                            put("estimated_time", JSONObject().put("type", "STRING").put("description", "Estimated time"))
                            put("strategy_steps", JSONObject().put("type", "ARRAY").put("items", JSONObject().put("type", "STRING")).put("description", "List of 3-5 steps"))
                            put("items", JSONObject().put("type", "ARRAY").put("items", JSONObject().put("type", "STRING")).put("description", "List of items to add to cart"))
                            put("guardrails", JSONObject().put("type", "STRING").put("description", "Policy rules"))
                            put("action_button_text", JSONObject().put("type", "STRING").put("description", "Dynamic action button text with emoji e.g. '🛒 Put Items in Walmart Cart'"))
                            put("total_amount", JSONObject().put("type", "STRING").put("description", "Dollar amount or null"))
                            put("requires_biometric", JSONObject().put("type", "BOOLEAN").put("description", "True if payment"))
                        }
                        put("properties", properties)
                        put("required", JSONArray().put("title").put("category").put("target_entity").put("strategy_steps").put("action_button_text"))
                    })
                }))
            }))
        }

        val requestBody = bodyJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GeminiLive", "REST Call Failed: ${e.message}", e)
                fallbackToLocalEngine(userPrompt)
            }

            override fun onResponse(call: Call, response: Response) {
                val respText = response.body?.string() ?: ""
                Log.d("GeminiLive", "REST Response (Code ${response.code}): $respText")
                if (!response.isSuccessful) {
                    Log.e("GeminiLive", "REST returned HTTP ${response.code}: $respText")
                    fallbackToLocalEngine(userPrompt)
                    return
                }

                val parsed = parseRestResponse(respText, userPrompt)
                if (!parsed) {
                    fallbackToLocalEngine(userPrompt)
                }
            }
        })
    }

    private fun fallbackToLocalEngine(userPrompt: String) {
        val fallbackResponse = SupportAgentEngine.processPrompt(userPrompt)
        notifyMessage(fallbackResponse.text, fallbackResponse.actionProposal)
        notifyStatus("Gemini Live Ready")
    }

    private fun parseRestResponse(jsonStr: String, userPrompt: String): Boolean {
        try {
            val root = JSONObject(jsonStr)
            if (root.has("error")) {
                val err = root.optJSONObject("error")?.optString("message") ?: "API Error"
                Log.e("GeminiLive", "Gemini API returned error: $err")
                return false
            }

            val candidates = root.optJSONArray("candidates") ?: return false
            if (candidates.length() == 0) return false

            val firstCandidate = candidates.getJSONObject(0)
            val content = firstCandidate.optJSONObject("content") ?: return false
            val parts = content.optJSONArray("parts") ?: return false

            var textOutput = ""
            var proposal: ActionProposal? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    textOutput += part.getString("text")
                }
                if (part.has("functionCall")) {
                    val call = part.getJSONObject("functionCall")
                    if (call.getString("name") == "propose_action") {
                        val args = call.getJSONObject("args")
                        proposal = parseActionProposal(args)
                    }
                }
            }

            // Guarantee Explain + Action paradigm: ensure an action proposal card is always present
            if (proposal == null) {
                val fallback = SupportAgentEngine.processPrompt(userPrompt)
                proposal = fallback.actionProposal
            }

            val finalSpeech = if (textOutput.isNotBlank()) {
                textOutput.trim()
            } else {
                "I analyzed your request and prepared the action plan for ${proposal?.targetEntity ?: "your request"}."
            }

            notifyMessage(finalSpeech, proposal)
            notifyStatus("Gemini 3.8 Flash Ready")
            return true
        } catch (e: Exception) {
            Log.e("GeminiLive", "Error parsing REST response: ${e.message}", e)
            return false
        }
    }

    private fun handleServerMessage(text: String) {
        try {
            val root = JSONObject(text)
            val serverContent = root.optJSONObject("serverContent") ?: return
            val modelTurn = serverContent.optJSONObject("modelTurn") ?: return
            val parts = modelTurn.optJSONArray("parts") ?: return

            var generatedText = ""
            var actionProposal: ActionProposal? = null

            for (i in 0 until parts.length()) {
                val part = parts.getJSONObject(i)
                if (part.has("text")) {
                    generatedText += part.getString("text")
                }
                if (part.has("functionCall")) {
                    val call = part.getJSONObject("functionCall")
                    if (call.getString("name") == "propose_action") {
                        val args = call.getJSONObject("args")
                        actionProposal = parseActionProposal(args)
                    }
                }
            }

            if (generatedText.isNotBlank()) {
                accumulatedText.append(generatedText)
            }

            val isTurnComplete = serverContent.optBoolean("turnComplete", false)
            if (isTurnComplete || actionProposal != null) {
                val finalText = if (accumulatedText.isNotBlank()) accumulatedText.toString() else "I analyzed your request and prepared the action plan below."
                notifyMessage(finalText, actionProposal)
            }
        } catch (e: Exception) {
            Log.e("GeminiLive", "Error parsing WebSocket message", e)
        }
    }

    private fun parseActionProposal(args: JSONObject): ActionProposal {
        val title = args.optString("title", "Autonomous Task")
        val categoryStr = args.optString("category", "SUPPORT_CALL").uppercase()
        val category = when {
            categoryStr.contains("FOOD") || categoryStr.contains("DELIVERY") -> ActionCategory.FOOD_DELIVERY
            categoryStr.contains("GROCERY") || categoryStr.contains("RESTOCK") -> ActionCategory.GROCERY_RESTOCK
            categoryStr.contains("CAR") || categoryStr.contains("RENTAL") || categoryStr.contains("TRAVEL") -> ActionCategory.TRAVEL_RENTAL
            categoryStr.contains("HOTEL") || categoryStr.contains("LODGING") || categoryStr.contains("STAY") -> ActionCategory.LODGING_RESERVATION
            categoryStr.contains("DISPUTE") || categoryStr.contains("REFUND") || categoryStr.contains("CHARGE") -> ActionCategory.DISPUTE_REFUND
            else -> ActionCategory.SUPPORT_CALL
        }
        val targetEntity = args.optString("target_entity", "Target Service")
        val targetContact = args.optString("target_contact", "Support Desk")
        val estimatedTime = args.optString("estimated_time", "3 - 5 mins")
        val guardrails = args.optString("guardrails", "Prioritize user preference. Confirm all terms.")
        val actionButtonText = args.optString("action_button_text", "⚡ Execute Task")
        val totalAmount = if (args.has("total_amount") && !args.isNull("total_amount")) args.getString("total_amount") else null
        val requiresBiometric = args.optBoolean("requires_biometric", totalAmount != null)

        val stepsList = mutableListOf<String>()
        val stepsArray = args.optJSONArray("strategy_steps")
        if (stepsArray != null) {
            for (j in 0 until stepsArray.length()) {
                stepsList.add(stepsArray.getString(j))
            }
        }
        if (stepsList.isEmpty()) {
            stepsList.addAll(listOf("Connect to target service", "Navigate requirements", "Confirm resolution and save receipt"))
        }

        val cartItemsList = mutableListOf<String>()
        val itemsArray = args.optJSONArray("items")
        if (itemsArray != null) {
            for (j in 0 until itemsArray.length()) {
                cartItemsList.add(itemsArray.getString(j))
            }
        }

        val icon = when (category) {
            ActionCategory.FOOD_DELIVERY -> ActionIconType.RESTAURANT
            ActionCategory.GROCERY_RESTOCK -> ActionIconType.GROCERY
            ActionCategory.TRAVEL_RENTAL -> ActionIconType.CAR
            ActionCategory.LODGING_RESERVATION -> ActionIconType.HOTEL
            ActionCategory.DISPUTE_REFUND -> ActionIconType.SHIELD
            else -> ActionIconType.PHONE
        }

        val badge = when (category) {
            ActionCategory.FOOD_DELIVERY -> "FOOD DELIVERY • LIVE"
            ActionCategory.GROCERY_RESTOCK -> "GROCERY RESTOCK • LIVE"
            ActionCategory.TRAVEL_RENTAL -> "TRAVEL • CAR RENTAL"
            ActionCategory.LODGING_RESERVATION -> "LODGING • RESERVATION"
            ActionCategory.DISPUTE_REFUND -> "DISPUTE • REFUND NOTICE"
            else -> "TELEPHONY PROXY • LIVE"
        }

        return ActionProposal(
            title = title,
            category = category,
            categoryBadge = badge,
            targetEntity = targetEntity,
            targetContact = targetContact,
            estimatedTime = estimatedTime,
            strategySteps = stepsList,
            guardrails = guardrails,
            actionButtonText = actionButtonText,
            actionIcon = icon,
            requiresBiometric = requiresBiometric,
            totalAmount = totalAmount,
            cartItems = cartItemsList
        )
    }

    private fun notifyStatus(status: String) {
        mainHandler.post { onStatusChanged(status) }
    }

    private fun notifyMessage(text: String, proposal: ActionProposal?) {
        mainHandler.post { onMessageReceived(text, proposal) }
    }

    fun disconnect() {
        webSocket?.close(1000, "App closed")
        isConnected = false
    }
}
