package com.example.supportagent.ui.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.supportagent.data.ApiKeyManager
import com.example.supportagent.data.cart.CartManager
import com.example.supportagent.data.driver.DriverManager
import com.example.supportagent.data.profile.UserProfileManager
import com.example.supportagent.engine.SupportAgentEngine
import com.example.supportagent.model.*
import com.example.supportagent.network.GeminiLiveClient
import com.example.supportagent.network.catalog.MultiStoreCatalogAggregator
import com.example.supportagent.ui.components.OrbState
import com.example.supportagent.voice.VoiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppTab {
    STORES,
    ASSISTANT,
    ORDERS,
    CARTS
}

data class MainUiState(
    val currentRole: AppRole = AppRole.CUSTOMER,
    val isDarkTheme: Boolean = true,
    val currentTab: AppTab = AppTab.STORES,
    val cart: UserStoreCart = UserStoreCart(),
    val driverProfile: DriverProfile = DriverProfile(),
    val availableBatches: List<DeliveryBatch> = emptyList(),
    val activeOrder: ActiveOrder? = null,
    val pastOrders: List<ActiveOrder> = listOf(
        ActiveOrder(
            orderId = "#PV-7214",
            storeName = "Walmart Supercenter",
            storeOrigin = StoreOrigin.WALMART,
            progress = OrderProgressStage.DELIVERED,
            subtotal = 38.40,
            deliveryFee = 4.99,
            driverTip = 5.00,
            totalPaid = 48.39,
            competitorInstacartMarkup = 14.20
        ),
        ActiveOrder(
            orderId = "#PV-5102",
            storeName = "ALDI (via Instacart Network)",
            storeOrigin = StoreOrigin.INSTACART_ALDI,
            progress = OrderProgressStage.DELIVERED,
            subtotal = 22.80,
            deliveryFee = 4.99,
            driverTip = 4.00,
            totalPaid = 31.79,
            competitorInstacartMarkup = 8.50
        )
    ),
    val showOrderSuccessDialog: Boolean = false,
    val lastOrderNumber: String = "",
    val messages: List<AgentMessage> = emptyList(),
    val orbState: OrbState = OrbState.IDLE,
    val inputText: String = "",
    val isTtsEnabled: Boolean = true,
    val isLiveConnected: Boolean = false,
    val liveStatus: String = "Built-in Engine",
    val showApiKeyDialog: Boolean = false,
    val currentApiKey: String = "",
    val isGhostTouchEnabled: Boolean = false,
    val showGhostTouchDialog: Boolean = false,
    val showEditAddressDialog: Boolean = false,
    val showDriverChat: Boolean = false,
    val driverChatMessages: List<DriverChatMessage> = listOf(
        DriverChatMessage(
            senderName = "Alex Rivera",
            isFromCustomer = false,
            text = "Hi! I just accepted your order and I'm heading to the store now. Let me know if you need any adjustments!",
            timestamp = "Just now"
        )
    ),
    val showCheckoutSheet: Boolean = false,
    val showDriverOnboarding: Boolean = false,
    val showOrderRatingDialog: Boolean = false
)

class MainScreenViewModel(application: Application) : AndroidViewModel(application) {

    private val apiKeyManager = ApiKeyManager(application)
    val userProfileManager = UserProfileManager(application)
    val cartManager = CartManager(application)
    val driverManager = DriverManager(application)
    private var geminiLiveClient: GeminiLiveClient? = null
    private var voiceManager: VoiceManager? = null

    private val _uiState = MutableStateFlow(
        MainUiState(
            currentApiKey = apiKeyManager.getApiKey(),
            liveStatus = if (apiKeyManager.hasApiKey()) "Gemini 3.8 Flash Ready" else "Built-in Engine",
            cart = cartManager.cartFlow.value,
            driverProfile = driverManager.driverFlow.value,
            availableBatches = driverManager.batchesFlow.value,
            messages = listOf(
                AgentMessage(
                    sender = Sender.AGENT,
                    text = "Welcome to Provi — Your Zero-Commission Grocery & Delivery Platform.\n\nTell me what you need, and I'll find the best shelf prices across Walmart and local stores without hidden delivery markups.",
                    timestamp = SupportAgentEngine.getCurrentTimestamp()
                )
            )
        )
    )
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        initGeminiLive()
        initVoice()
        initCartSync()
        initDriverSync()
    }

    private fun initDriverSync() {
        viewModelScope.launch {
            driverManager.driverFlow.collect { profile ->
                _uiState.update { it.copy(driverProfile = profile) }
            }
        }
        viewModelScope.launch {
            driverManager.batchesFlow.collect { batches ->
                _uiState.update { it.copy(availableBatches = batches) }
            }
        }
    }

    fun switchRole(newRole: AppRole) {
        _uiState.update { it.copy(currentRole = newRole) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun toggleDriverOnline() {
        driverManager.toggleOnlineStatus()
    }

    fun acceptDriverBatch(batchId: String) {
        driverManager.acceptBatch(batchId)
    }

    fun completeDriverBatch(batchId: String) {
        driverManager.completeBatch(batchId)
    }

    fun toggleDriverItemFound(batchId: String, item: String) {
        driverManager.toggleBatchItemFound(batchId, item)
    }

    fun advanceDriverBatch(batchId: String) {
        driverManager.advanceBatchStatus(batchId)
    }

    fun cashOutDriver() {
        driverManager.cashOutEarnings()
    }

    fun addStoreProductToCart(product: StoreProduct) {
        val cartItem = CartItem(
            store = product.storeName,
            storeOrigin = product.origin,
            title = product.name,
            brand = product.brand,
            price = product.price,
            formattedPrice = product.formattedPrice,
            unitPrice = product.unitSize,
            quantity = 1,
            imageUrl = product.imageUrl
        )
        cartManager.addItem(cartItem)
    }

    fun advanceOrderStage() {
        val currentOrder = _uiState.value.activeOrder ?: return
        val nextStage = when (currentOrder.progress) {
            OrderProgressStage.ORDER_CONFIRMED -> OrderProgressStage.DRIVER_ASSIGNED
            OrderProgressStage.DRIVER_ASSIGNED -> OrderProgressStage.SHOPPING
            OrderProgressStage.SHOPPING -> OrderProgressStage.OUT_FOR_DELIVERY
            OrderProgressStage.OUT_FOR_DELIVERY -> OrderProgressStage.DELIVERED
            OrderProgressStage.DELIVERED -> OrderProgressStage.ORDER_CONFIRMED
        }
        val newEta = when (nextStage) {
            OrderProgressStage.ORDER_CONFIRMED -> 45
            OrderProgressStage.DRIVER_ASSIGNED -> 35
            OrderProgressStage.SHOPPING -> 25
            OrderProgressStage.OUT_FOR_DELIVERY -> 12
            OrderProgressStage.DELIVERED -> 0
        }
        _uiState.update {
            it.copy(
                activeOrder = currentOrder.copy(progress = nextStage, estimatedArrivalMinutes = newEta),
                showOrderRatingDialog = if (nextStage == OrderProgressStage.DELIVERED) true else it.showOrderRatingDialog
            )
        }
    }

    private fun initCartSync() {
        viewModelScope.launch {
            cartManager.cartFlow.collect { updatedCart ->
                _uiState.update { it.copy(cart = updatedCart) }
            }
        }
    }

    fun selectTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    private fun initGeminiLive() {
        val key = apiKeyManager.getApiKey()
        if (key.isNotBlank()) {
            geminiLiveClient?.disconnect()
            geminiLiveClient = GeminiLiveClient(
                apiKey = key,
                onMessageReceived = { text, proposal ->
                    handleGeminiResponse(text, proposal)
                },
                onStatusChanged = { status ->
                    _uiState.update {
                        it.copy(
                            liveStatus = status,
                            isLiveConnected = status.contains("Connected") || status.contains("Ready")
                        )
                    }
                }
            )
            geminiLiveClient?.connect()
        }
    }

    private fun initVoice() {
        voiceManager = VoiceManager(
            context = getApplication<Application>().applicationContext,
            onSpeechRecognized = { recognizedText ->
                submitQuery(recognizedText)
            },
            onListeningStateChanged = { isListening ->
                _uiState.update {
                    it.copy(orbState = if (isListening) OrbState.LISTENING else OrbState.IDLE)
                }
            },
            onSpeakingStateChanged = { isSpeaking ->
                _uiState.update {
                    it.copy(orbState = if (isSpeaking) OrbState.SPEAKING else OrbState.IDLE)
                }
            }
        )
    }

    fun checkGhostTouchStatus() {
        val ready = com.example.supportagent.automation.GhostTouchCommander.isServiceReady()
        _uiState.update { it.copy(isGhostTouchEnabled = ready) }
    }

    fun openGhostTouchDialog() {
        _uiState.update { it.copy(showGhostTouchDialog = true) }
    }

    fun closeGhostTouchDialog() {
        _uiState.update { it.copy(showGhostTouchDialog = false) }
    }

    fun launchGhostTouchSettings() {
        _uiState.update { it.copy(showGhostTouchDialog = false) }
        com.example.supportagent.automation.GhostTouchCommander.openAccessibilitySettings(getApplication())
    }

    fun openApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = true) }
    }

    fun closeApiKeyDialog() {
        _uiState.update { it.copy(showApiKeyDialog = false) }
    }

    fun openEditAddressDialog() {
        _uiState.update { it.copy(showEditAddressDialog = true) }
    }

    fun closeEditAddressDialog() {
        _uiState.update { it.copy(showEditAddressDialog = false) }
    }

    fun saveApiKey(newKey: String) {
        apiKeyManager.saveApiKey(newKey)
        _uiState.update {
            it.copy(
                currentApiKey = newKey,
                showApiKeyDialog = false,
                liveStatus = if (newKey.isNotBlank()) "Connecting..." else "Built-in Engine"
            )
        }
        initGeminiLive()
    }

    fun onInputTextChanged(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    fun submitQuery(query: String) {
        if (query.isBlank()) return

        voiceManager?.stopSpeaking()
        val userMessage = AgentMessage(
            sender = Sender.USER,
            text = query,
            timestamp = SupportAgentEngine.getCurrentTimestamp()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                inputText = "",
                orbState = OrbState.THINKING
            )
        }

        val lower = query.lowercase()
        val isGroceryQuery = lower.contains("walmart") || lower.contains("grocery") ||
                lower.contains("milk") || lower.contains("egg") || lower.contains("bread") ||
                lower.contains("apple") || lower.contains("banana") || lower.contains("restock") ||
                lower.contains("shop") || lower.contains("cart") || lower.contains("buy") ||
                lower.contains("find") || lower.contains("search") || lower.contains("order") ||
                lower.contains("soap") || lower.contains("cereal") || lower.contains("coffee") ||
                lower.contains("chip") || lower.contains("sauce") || lower.contains("pasta")

        if (isGroceryQuery) {
            // Build real-time Multi-Store Catalog Flash Card Deck (Walmart + Instacart)
            viewModelScope.launch {
                kotlinx.coroutines.delay(400)
                val profile = userProfileManager.getProfile()
                val apiKey = apiKeyManager.getApiKey()
                val deck = MultiStoreCatalogAggregator.aggregateForQuery(query, profile, apiKey)

                val replyText = "I ran a live global search across Walmart and Instacart (ALDI, Target, Kroger) for your request:\n\nSwipe through the flash cards below to check live pricing, stock, and alternatives. Tap any alternative badge to swap stores instantly."

                val agentMsg = AgentMessage(
                    sender = Sender.AGENT,
                    text = replyText,
                    timestamp = SupportAgentEngine.getCurrentTimestamp(),
                    groceryDeck = deck
                )

                _uiState.update {
                    it.copy(
                        messages = it.messages + agentMsg,
                        orbState = OrbState.SPEAKING
                    )
                }

                if (_uiState.value.isTtsEnabled) {
                    voiceManager?.speak(replyText)
                } else {
                    kotlinx.coroutines.delay(1200)
                    _uiState.update { it.copy(orbState = OrbState.IDLE) }
                }
            }
            return
        }

        if (apiKeyManager.hasApiKey() && geminiLiveClient != null) {
            geminiLiveClient?.sendPrompt(query)
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(600)
                val agentResponse = SupportAgentEngine.processPrompt(query)
                handleGeminiResponse(agentResponse.text, agentResponse.actionProposal)
            }
        }
    }

    private fun handleGeminiResponse(text: String, proposal: ActionProposal?) {
        val agentMessage = AgentMessage(
            sender = Sender.AGENT,
            text = text,
            timestamp = SupportAgentEngine.getCurrentTimestamp(),
            actionProposal = proposal
        )

        _uiState.update {
            it.copy(
                messages = it.messages + agentMessage,
                orbState = OrbState.SPEAKING
            )
        }

        if (_uiState.value.isTtsEnabled) {
            voiceManager?.speak(text)
        } else {
            viewModelScope.launch {
                kotlinx.coroutines.delay(1200)
                _uiState.update { it.copy(orbState = OrbState.IDLE) }
            }
        }
    }

    fun onAlternativeSelected(messageId: String, cardId: String, altId: String) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId && msg.groceryDeck != null) {
                    val updatedCards = msg.groceryDeck.cards.map { card ->
                        if (card.id == cardId) {
                            val newAltId = if (card.activeAlternativeId == altId) null else altId
                            val selectedAlt = card.alternatives.find { it.id == newAltId }
                            if (selectedAlt != null) {
                                userProfileManager.saveBrandPreference(card.queryItem, selectedAlt.title)
                            }
                            card.copy(activeAlternativeId = newAltId)
                        } else {
                            card
                        }
                    }
                    msg.copy(groceryDeck = msg.groceryDeck.copy(cards = updatedCards))
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }
    }

    fun onQuantityChanged(messageId: String, cardId: String, newQty: Int) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId && msg.groceryDeck != null) {
                    val updatedCards = msg.groceryDeck.cards.map { card ->
                        if (card.id == cardId) card.copy(quantity = newQty) else card
                    }
                    msg.copy(groceryDeck = msg.groceryDeck.copy(cards = updatedCards))
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }
    }

    fun onItemToggled(messageId: String, cardId: String, isSelected: Boolean) {
        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.id == messageId && msg.groceryDeck != null) {
                    val updatedCards = msg.groceryDeck.cards.map { card ->
                        if (card.id == cardId) card.copy(isSelected = isSelected) else card
                    }
                    msg.copy(groceryDeck = msg.groceryDeck.copy(cards = updatedCards))
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }
    }

    fun submitDeckToWalmartCart(deck: GroceryCartDeck) {
        val profile = userProfileManager.getProfile()
        cartManager.addDeckCards(
            cards = deck.selectedCards,
            storeName = "Multi-Store Smart Cart",
            deliveryAddress = profile.fullFormattedAddress
        )

        val confirmText = "🛒 Added ${deck.selectedCards.size} item(s) to your Carts (${deck.formattedTotal})!\n\nI have switched you to your Carts tab where you can review your Walmart and Instacart baskets separately."
        val confirmMsg = AgentMessage(
            sender = Sender.AGENT,
            text = confirmText,
            timestamp = SupportAgentEngine.getCurrentTimestamp()
        )

        _uiState.update {
            it.copy(
                messages = it.messages + confirmMsg,
                currentTab = AppTab.CARTS
            )
        }

        if (_uiState.value.isTtsEnabled) {
            voiceManager?.speak("Added ${deck.selectedCards.size} items to your cart.")
        }
    }

    fun updateCartQuantity(itemId: String, newQty: Int) {
        cartManager.updateQuantity(itemId, newQty)
    }

    fun removeCartItem(itemId: String) {
        cartManager.removeItem(itemId)
    }

    fun clearCart() {
        cartManager.clearCart()
    }

    fun openCheckoutSheet() {
        _uiState.update { it.copy(showCheckoutSheet = true) }
    }

    fun closeCheckoutSheet() {
        _uiState.update { it.copy(showCheckoutSheet = false) }
    }

    fun openDriverChat() {
        _uiState.update { it.copy(showDriverChat = true) }
    }

    fun closeDriverChat() {
        _uiState.update { it.copy(showDriverChat = false) }
    }

    fun sendDriverChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = DriverChatMessage(
            senderName = "You",
            isFromCustomer = true,
            text = text.trim(),
            timestamp = "Just now"
        )
        _uiState.update { it.copy(driverChatMessages = it.driverChatMessages + userMsg) }

        // Simulate instant driver response
        viewModelScope.launch {
            kotlinx.coroutines.delay(1200)
            val driverResponses = listOf(
                "Got your note! I'm on it right now.",
                "Sounds good! I'll make sure to double check before checking out.",
                "No problem at all! Leaving at your front door as requested.",
                "Thank you for letting me know! I'm about 5 minutes away."
            )
            val replyText = driverResponses.random()
            val driverMsg = DriverChatMessage(
                senderName = "Alex Rivera",
                isFromCustomer = false,
                text = replyText,
                timestamp = "Just now"
            )
            _uiState.update { it.copy(driverChatMessages = it.driverChatMessages + driverMsg) }
        }
    }

    fun openDriverOnboarding() {
        _uiState.update { it.copy(showDriverOnboarding = true) }
    }

    fun closeDriverOnboarding() {
        _uiState.update { it.copy(showDriverOnboarding = false) }
    }

    fun completeDriverOnboarding(profile: DriverProfile) {
        driverManager.updateProfile(profile)
        _uiState.update {
            it.copy(
                driverProfile = profile,
                showDriverOnboarding = false
            )
        }
    }

    fun openOrderRating() {
        _uiState.update { it.copy(showOrderRatingDialog = true) }
    }

    fun closeOrderRating() {
        _uiState.update { it.copy(showOrderRatingDialog = false) }
    }

    fun submitOrderRating(stars: Int, compliments: List<String>, extraTip: Double) {
        val currentOrder = _uiState.value.activeOrder
        if (currentOrder != null && extraTip > 0) {
            val updatedOrder = currentOrder.copy(
                driverTip = currentOrder.driverTip + extraTip,
                totalPaid = currentOrder.totalPaid + extraTip
            )
            _uiState.update { it.copy(activeOrder = updatedOrder) }
        }
        _uiState.update { it.copy(showOrderRatingDialog = false) }
    }

    fun checkoutCart(paymentMethod: String = "Google Pay", deliveryNotes: String = "Leave at front door") {
        val orderNum = "#PV-${(1000..9999).random()}"
        val currentCart = _uiState.value.cart

        val newOrder = ActiveOrder(
            orderId = orderNum,
            storeName = if (currentCart.items.any { it.storeOrigin == StoreOrigin.WALMART }) "Walmart Supercenter" else "ALDI (via Instacart Network)",
            items = currentCart.items,
            progress = OrderProgressStage.ORDER_CONFIRMED,
            estimatedArrivalMinutes = 35,
            deliveryAddress = currentCart.deliveryAddress,
            subtotal = currentCart.subtotal,
            deliveryFee = 4.99,
            driverTip = 5.00,
            totalPaid = currentCart.subtotal + 4.99 + 5.00 + currentCart.estimatedTax,
            competitorInstacartMarkup = currentCart.subtotal * 0.25
        )

        // Dispatch batch to driver pool
        driverManager.dispatchBatchFromOrder(
            orderNumber = orderNum,
            storeName = newOrder.storeName,
            deliveryAddress = currentCart.deliveryAddress,
            items = currentCart.items,
            tip = 5.00
        )

        cartManager.clearCart()

        val receiptMsg = AgentMessage(
            sender = Sender.AGENT,
            text = "✅ Order Placed with 0% Markup via $paymentMethod!\n\nReference: $orderNum\nDelivering to: ${currentCart.deliveryAddress}\nInstructions: $deliveryNotes\nTotal Paid: ${newOrder.formattedTotalPaid}\nSaved vs Instacart: ${newOrder.formattedSavings}\nDriver assigned: Alex Rivera • Tracking live now.",
            timestamp = SupportAgentEngine.getCurrentTimestamp()
        )

        _uiState.update {
            it.copy(
                activeOrder = newOrder,
                currentTab = AppTab.ORDERS,
                showCheckoutSheet = false,
                showOrderSuccessDialog = true,
                lastOrderNumber = orderNum,
                messages = it.messages + receiptMsg
            )
        }

        if (_uiState.value.isTtsEnabled) {
            voiceManager?.speak("Your zero-commission grocery order has been placed and dispatched to local drivers!")
        }
    }

    fun dismissOrderSuccessDialog() {
        _uiState.update { it.copy(showOrderSuccessDialog = false) }
    }

    fun saveUserAddress(addressLine: String, city: String, state: String, zip: String) {
        userProfileManager.saveAddress(addressLine, city, state, zip)
        _uiState.update { it.copy(showEditAddressDialog = false) }

        val updatedAddress = userProfileManager.getProfile().fullFormattedAddress
        cartManager.updateDeliveryAddress(updatedAddress)

        _uiState.update { state ->
            val updatedMessages = state.messages.map { msg ->
                if (msg.groceryDeck != null) {
                    msg.copy(groceryDeck = msg.groceryDeck.copy(deliveryAddress = updatedAddress))
                } else {
                    msg
                }
            }
            state.copy(messages = updatedMessages)
        }
    }

    fun executeFixIt(proposal: ActionProposal) {
        viewModelScope.launch {
            voiceManager?.stopSpeaking()
            _uiState.update { it.copy(orbState = OrbState.THINKING) }

            launch {
                com.example.supportagent.automation.GhostTouchCommander.runAutonomousAppMission(
                    getApplication(),
                    proposal
                ) { progressStep ->
                    android.util.Log.d("GhostTouch", progressStep)
                }
            }

            SupportAgentEngine.executeMission(proposal).collect { updatedProposal ->
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.actionProposal?.id == proposal.id) {
                            msg.copy(actionProposal = updatedProposal)
                        } else {
                            msg
                        }
                    }
                    state.copy(
                        messages = updatedMessages,
                        orbState = if (updatedProposal.status == ActionStatus.EXECUTING) OrbState.SPEAKING else OrbState.IDLE
                    )
                }
            }
        }
    }

    fun resolveOffer(proposal: ActionProposal, acceptOffer: Boolean) {
        viewModelScope.launch {
            SupportAgentEngine.resolveOffer(proposal, acceptOffer).collect { updatedProposal ->
                _uiState.update { state ->
                    val updatedMessages = state.messages.map { msg ->
                        if (msg.actionProposal?.id == proposal.id) {
                            msg.copy(actionProposal = updatedProposal)
                        } else {
                            msg
                        }
                    }
                    state.copy(
                        messages = updatedMessages,
                        orbState = OrbState.IDLE
                    )
                }
            }
        }
    }

    fun onMicTapped() {
        val currentState = _uiState.value.orbState
        if (currentState == OrbState.IDLE) {
            voiceManager?.startListening()
        } else if (currentState == OrbState.LISTENING) {
            voiceManager?.stopListening()
        } else {
            voiceManager?.stopSpeaking()
            _uiState.update { it.copy(orbState = OrbState.IDLE) }
        }
    }

    override fun onCleared() {
        super.onCleared()
        geminiLiveClient?.disconnect()
        voiceManager?.destroy()
    }
}
