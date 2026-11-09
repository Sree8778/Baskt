package com.example.supportagent.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.supportagent.ui.stores.StoresScreen
import com.example.supportagent.ui.orders.OrdersScreen
import com.example.supportagent.ui.orders.OrderRatingDialog
import com.example.supportagent.ui.cart.CheckoutPaymentSheet
import com.example.supportagent.ui.chat.CustomerDriverChatSheet
import com.example.supportagent.ui.driver.DriverOnboardingDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.supportagent.model.ActionStatus
import com.example.supportagent.model.AgentMessage
import com.example.supportagent.model.GroceryCartDeck
import com.example.supportagent.theme.SupportAgentTheme
import com.example.supportagent.model.Sender
import com.example.supportagent.model.AppRole
import com.example.supportagent.ui.cart.CartsScreen
import com.example.supportagent.ui.driver.DriverCockpitScreen
import com.example.supportagent.ui.components.ActionProposalCard
import com.example.supportagent.ui.components.GlowingOrb
import com.example.supportagent.ui.components.GroceryFlashCardDeck
import com.example.supportagent.ui.components.LiveMissionTracker
import com.example.supportagent.ui.components.OrbState

@Composable
fun MainScreen(
    onItemClick: (NavKey) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MainScreenViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onMicTapped()
        }
    }

    val onMicClick: () -> Unit = {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.onMicTapped()
        } else {
            permissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-scroll on new message
    LaunchedEffect(state.messages.size, state.messages.lastOrNull()?.actionProposal?.execution?.steps) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
        viewModel.checkGhostTouchStatus()
    }

    // Ghost Touch Accessibility Setup Dialog
    if (state.showGhostTouchDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.closeGhostTouchDialog() },
            containerColor = Color(0xFF14151F),
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enable Ghost Touch",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Ghost Touch allows ProxyAgent to autonomously navigate installed apps (like Uber Eats and Walmart), search items, and assemble carts on your screen.\n\nTap below to open Android Accessibility settings and turn ON 'ProxyAgent Ghost Touch'.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93),
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.launchGhostTouchSettings() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
                ) {
                    Text("Open Android Settings", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.closeGhostTouchDialog() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Gemini 2.0 Live API Key Settings Dialog
    if (state.showApiKeyDialog) {
        var keyInput by remember { mutableStateOf(state.currentApiKey) }

        AlertDialog(
            onDismissRequest = { viewModel.closeApiKeyDialog() },
            containerColor = Color(0xFF14151F),
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFF64D2FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Gemini 2.0 Live API",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Connect Google Gemini 2.0 Live WebSocket for unbounded, real-time intelligence across any store or service.\n\nFree API keys available at aistudio.google.com",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93),
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        placeholder = { Text("Paste AIzaSy... API Key", fontSize = 13.sp, color = Color(0xFF636366)) },
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0A0B10),
                            unfocusedContainerColor = Color(0xFF0A0B10),
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.saveApiKey(keyInput) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                ) {
                    Text("Save & Connect", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.closeApiKeyDialog() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    // Edit Delivery Address Dialog
    if (state.showEditAddressDialog) {
        val currentProfile = viewModel.userProfileManager.getProfile()
        var streetInput by remember { mutableStateOf(currentProfile.addressLine) }
        var cityInput by remember { mutableStateOf(currentProfile.city) }
        var stateInput by remember { mutableStateOf(currentProfile.state) }
        var zipInput by remember { mutableStateOf(currentProfile.zipCode) }

        AlertDialog(
            onDismissRequest = { viewModel.closeEditAddressDialog() },
            containerColor = Color(0xFF14151F),
            shape = RoundedCornerShape(22.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = Color(0xFFFF9F0A),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Delivery Address",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "This address is used to check live Walmart store inventory and delivery availability.",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = streetInput,
                        onValueChange = { streetInput = it },
                        label = { Text("Street Address") },
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF0F1018),
                            unfocusedContainerColor = Color(0xFF0F1018),
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color(0x33FFFFFF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cityInput,
                            onValueChange = { cityInput = it },
                            label = { Text("City") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F1018),
                                unfocusedContainerColor = Color(0xFF0F1018),
                                focusedBorderColor = Color(0xFF0A84FF),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = stateInput,
                            onValueChange = { stateInput = it },
                            label = { Text("State") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F1018),
                                unfocusedContainerColor = Color(0xFF0F1018),
                                focusedBorderColor = Color(0xFF0A84FF),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = zipInput,
                            onValueChange = { zipInput = it },
                            label = { Text("ZIP") },
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF0F1018),
                                unfocusedContainerColor = Color(0xFF0F1018),
                                focusedBorderColor = Color(0xFF0A84FF),
                                unfocusedBorderColor = Color(0x33FFFFFF),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier.weight(1.5f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveUserAddress(streetInput, cityInput, stateInput, zipInput)
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
                ) {
                    Text("Save Address", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.closeEditAddressDialog() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Cancel", color = Color(0xFF8E8E93))
                }
            }
        )
    }

    SupportAgentTheme(darkTheme = state.isDarkTheme) {
        if (state.currentRole == AppRole.DRIVER) {
            DriverCockpitScreen(
                driver = state.driverProfile,
                batches = state.availableBatches,
                isDarkTheme = state.isDarkTheme,
                onToggleOnline = { viewModel.toggleDriverOnline() },
                onAcceptBatch = { viewModel.acceptDriverBatch(it) },
                onCompleteBatch = { viewModel.completeDriverBatch(it) },
                onToggleItemFound = { bId, item -> viewModel.toggleDriverItemFound(bId, item) },
                onAdvanceBatch = { viewModel.advanceDriverBatch(it) },
                onCashOut = { viewModel.cashOutDriver() },
                onSwitchToCustomer = { viewModel.switchRole(AppRole.CUSTOMER) },
                onToggleTheme = { viewModel.toggleTheme() },
                onOpenCustomerChat = { viewModel.openDriverChat() },
                onOpenOnboarding = { viewModel.openDriverOnboarding() },
                modifier = modifier
            )
        } else {
            val bgColor = if (state.isDarkTheme) Color(0xFF000000) else Color(0xFFF2F2F7)
            val cardBg = if (state.isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
            val primaryText = if (state.isDarkTheme) Color.White else Color(0xFF000000)
            val subText = if (state.isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
            val borderCol = if (state.isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(bgColor)
            ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (state.currentTab) {
                AppTab.STORES -> {
                    StoresScreen(
                        cart = state.cart,
                        isDarkTheme = state.isDarkTheme,
                        onAddToCart = { viewModel.addStoreProductToCart(it) },
                        onRemoveFromCart = { viewModel.removeCartItem(it) },
                        onUpdateQuantity = { id, q -> viewModel.updateCartQuantity(id, q) },
                        onSwitchToDriver = { viewModel.switchRole(AppRole.DRIVER) },
                        onToggleTheme = { viewModel.toggleTheme() },
                        onEditAddressClick = { viewModel.openEditAddressDialog() },
                        onViewCartClick = { viewModel.selectTab(AppTab.CARTS) }
                    )
                }
                AppTab.ASSISTANT -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                    // Apple Intelligence Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (state.isLiveConnected) Color(0xFF30D158) else Color(0xFF0A84FF))
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(
                        text = "PROVI AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64D2FF),
                        letterSpacing = 1.2.sp
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = state.liveStatus,
                    fontSize = 11.sp,
                    color = subText
                )
            }

            // Role Switcher, Theme Toggle & Ghost Touch Action Badges
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Customer -> Driver Switch Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (state.isDarkTheme) Color(0xFF1C1D26) else Color(0xFFE5E5EA))
                        .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { viewModel.switchRole(AppRole.DRIVER) }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Driver",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Driver Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Light / Dark Theme Toggle
                IconButton(
                    onClick = { viewModel.toggleTheme() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (state.isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (state.isDarkTheme) Color(0xFF1C1D26) else Color(0xFFE5E5EA))
                        .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { viewModel.openApiKeyDialog() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = subText,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (state.currentApiKey.isNotBlank()) "Gemini Live" else "API Key",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryText
                        )
                    }
                }
            }
        }

        // Apple Intelligence Siri Chromatic Glowing Orb
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            GlowingOrb(
                state = state.orbState,
                onClick = onMicClick
            )
        }

        // Cupertino Horizontal Suggestion Chips
        val suggestions = listOf(
            "🛒 Put Groceries in Walmart Cart",
            "🌯 Add Chipotle to Uber Eats Cart",
            "📦 Put Items in Amazon Cart",
            "📞 Cancel Planet Fitness",
            "💬 Negotiate Comcast",
            "🚗 Denver SUV Rental"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            suggestions.forEach { suggestion ->
                val query = when {
                    suggestion.contains("Walmart") -> "Put milk, organic eggs, honeycrisp apples, and bread in my Walmart cart"
                    suggestion.contains("Chipotle") -> "Put a chicken burrito bowl with brown rice and black beans in my Uber Eats Chipotle cart"
                    suggestion.contains("Amazon") -> "Put paper towels and coffee in my Amazon cart"
                    suggestion.contains("Planet Fitness") -> "Cancel my Planet Fitness membership"
                    suggestion.contains("Comcast") -> "Negotiate lower bill with Comcast"
                    suggestion.contains("Denver") -> "Find affordable SUV car rental in Denver"
                    else -> suggestion
                }

                Box(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(cardBg)
                        .border(1.dp, borderCol, RoundedCornerShape(18.dp))
                        .clickable { viewModel.submitQuery(query) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = suggestion,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = primaryText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Messages & Action Cards Feed
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            items(state.messages, key = { it.id }) { message ->
                AppleMessageBubble(
                    message = message,
                    isDarkTheme = state.isDarkTheme,
                    onFixItClick = { proposal ->
                        android.widget.Toast.makeText(context, "Ghost Touch: Executing ${proposal.title}", android.widget.Toast.LENGTH_SHORT).show()
                        viewModel.executeFixIt(proposal)
                    },
                    onAcceptOffer = { proposal -> viewModel.resolveOffer(proposal, true) },
                    onDeclineOffer = { proposal -> viewModel.resolveOffer(proposal, false) },
                    onAlternativeSelected = { cardId, altId ->
                        viewModel.onAlternativeSelected(message.id, cardId, altId)
                    },
                    onQuantityChanged = { cardId, newQty ->
                        viewModel.onQuantityChanged(message.id, cardId, newQty)
                    },
                    onItemToggled = { cardId, isSelected ->
                        viewModel.onItemToggled(message.id, cardId, isSelected)
                    },
                    onSubmitToCart = { deck ->
                        viewModel.submitDeckToWalmartCart(deck)
                    },
                    onEditAddressClick = {
                        viewModel.openEditAddressDialog()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Cupertino Floating Search & Voice Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            OutlinedTextField(
                value = state.inputText,
                onValueChange = { viewModel.onInputTextChanged(it) },
                placeholder = {
                    Text(
                        text = "Ask anything or delegate a task...",
                        fontSize = 13.sp,
                        color = Color(0xFF636366)
                    )
                },
                shape = RoundedCornerShape(26.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = cardBg,
                    unfocusedContainerColor = cardBg,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedBorderColor = borderCol,
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { viewModel.submitQuery(state.inputText) }),
                trailingIcon = {
                    IconButton(onClick = onMicClick) {
                        Icon(
                            imageVector = if (state.orbState == OrbState.LISTENING) Icons.Default.Mic else Icons.Default.MicNone,
                            contentDescription = "Voice Input",
                            tint = if (state.orbState == OrbState.LISTENING) Color(0xFFFF375F) else Color(0xFF8E8E93)
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(10.dp))

            IconButton(
                onClick = { viewModel.submitQuery(state.inputText) },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                        )
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
            } // end Column
        }
        AppTab.ORDERS -> {
            OrdersScreen(
                activeOrder = state.activeOrder,
                pastOrders = state.pastOrders,
                isDarkTheme = state.isDarkTheme,
                onSwitchToDriver = { viewModel.switchRole(AppRole.DRIVER) },
                onToggleTheme = { viewModel.toggleTheme() },
                onStartShopping = { viewModel.selectTab(AppTab.STORES) },
                onAdvanceOrderStage = { viewModel.advanceOrderStage() },
                onOpenDriverChat = { viewModel.openDriverChat() },
                onOpenRating = { viewModel.openOrderRating() }
            )
        }
        AppTab.CARTS -> {
            CartsScreen(
                cart = state.cart,
                isDarkTheme = state.isDarkTheme,
                onQuantityChanged = { itemId, newQty -> viewModel.updateCartQuantity(itemId, newQty) },
                onRemoveItem = { itemId -> viewModel.removeCartItem(itemId) },
                onClearCart = { viewModel.clearCart() },
                onEditAddressClick = { viewModel.openEditAddressDialog() },
                onStartShoppingClick = { viewModel.selectTab(AppTab.STORES) },
                onCheckoutClick = { viewModel.openCheckoutSheet() },
                showOrderSuccessDialog = state.showOrderSuccessDialog,
                lastOrderNumber = state.lastOrderNumber,
                onDismissSuccessDialog = { viewModel.dismissOrderSuccessDialog() }
            )
        }
    }
} // end Box

    // Apple Cupertino Docked Bottom Navigation Bar
    CupertinoBottomNavBar(
        currentTab = state.currentTab,
        cartItemCount = state.cart.totalItemCount,
        isDarkTheme = state.isDarkTheme,
        onTabSelected = { viewModel.selectTab(it) }
    )
    } // end Column
    } // end else

    // Full Delivery Platform Sheets & Dialogs
    if (state.showCheckoutSheet) {
        CheckoutPaymentSheet(
            cart = state.cart,
            selectedTip = 5.0,
            isDarkTheme = state.isDarkTheme,
            onConfirmCheckout = { method, notes ->
                viewModel.checkoutCart(paymentMethod = method, deliveryNotes = notes)
            },
            onDismiss = { viewModel.closeCheckoutSheet() }
        )
    }

    if (state.showDriverChat) {
        CustomerDriverChatSheet(
            driverName = state.activeOrder?.driverName ?: state.driverProfile.fullName,
            driverVehicle = state.activeOrder?.driverVehicle ?: state.driverProfile.vehicleModel,
            driverRating = state.activeOrder?.driverRating ?: state.driverProfile.rating,
            messages = state.driverChatMessages,
            isDarkTheme = state.isDarkTheme,
            onSendMessage = { viewModel.sendDriverChatMessage(it) },
            onDismiss = { viewModel.closeDriverChat() }
        )
    }

    if (state.showDriverOnboarding) {
        DriverOnboardingDialog(
            isDarkTheme = state.isDarkTheme,
            onCompleteOnboarding = { viewModel.completeDriverOnboarding(it) },
            onDismiss = { viewModel.closeDriverOnboarding() }
        )
    }

    if (state.showOrderRatingDialog && state.activeOrder != null) {
        OrderRatingDialog(
            order = state.activeOrder!!,
            isDarkTheme = state.isDarkTheme,
            onSubmitRating = { stars, comps, tip ->
                viewModel.submitOrderRating(stars, comps, tip)
            },
            onDismiss = { viewModel.closeOrderRating() }
        )
    }
} // end SupportAgentTheme
} // end MainScreen

@Composable
fun CupertinoBottomNavBar(
    currentTab: com.example.supportagent.ui.main.AppTab,
    cartItemCount: Int,
    isDarkTheme: Boolean = true,
    onTabSelected: (com.example.supportagent.ui.main.AppTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val navBg = if (isDarkTheme) Color(0xFF0D0E16) else Color(0xFFFFFFFF)
    val navBorder = if (isDarkTheme) Color(0x33FFFFFF) else Color(0x1F000000)
    val activeLabelColor = if (isDarkTheme) Color.White else Color(0xFF000000)
    val inactiveLabelColor = Color(0xFF8E8E93)

    Surface(
        color = navBg,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = (0.5).dp,
                color = navBorder,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Stores Tab
            val isStoresSelected = currentTab == AppTab.STORES
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(AppTab.STORES) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Storefront,
                    contentDescription = "Stores",
                    tint = if (isStoresSelected) Color(0xFF0A84FF) else inactiveLabelColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Stores",
                    fontSize = 11.sp,
                    fontWeight = if (isStoresSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isStoresSelected) activeLabelColor else inactiveLabelColor
                )
            }

            // 2. Assistant Tab
            val isAssistantSelected = currentTab == AppTab.ASSISTANT
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(AppTab.ASSISTANT) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Assistant",
                    tint = if (isAssistantSelected) Color(0xFF64D2FF) else inactiveLabelColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Assistant",
                    fontSize = 11.sp,
                    fontWeight = if (isAssistantSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isAssistantSelected) activeLabelColor else inactiveLabelColor
                )
            }

            // 3. Orders Tab
            val isOrdersSelected = currentTab == AppTab.ORDERS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(AppTab.ORDERS) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = "Orders",
                    tint = if (isOrdersSelected) Color(0xFF30D158) else inactiveLabelColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Orders",
                    fontSize = 11.sp,
                    fontWeight = if (isOrdersSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isOrdersSelected) activeLabelColor else inactiveLabelColor
                )
            }

            // 4. Carts Tab
            val isCartsSelected = currentTab == AppTab.CARTS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelected(AppTab.CARTS) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                BadgedBox(
                    badge = {
                        if (cartItemCount > 0) {
                            Badge(
                                containerColor = Color(0xFF0071DC),
                                contentColor = Color.White
                            ) {
                                Text("$cartItemCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carts",
                        tint = if (isCartsSelected) Color(0xFF0071DC) else inactiveLabelColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Carts",
                    fontSize = 11.sp,
                    fontWeight = if (isCartsSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isCartsSelected) activeLabelColor else inactiveLabelColor
                )
            }
        }
    }
}

@Composable
fun AppleMessageBubble(
    message: AgentMessage,
    isDarkTheme: Boolean = true,
    onFixItClick: (com.example.supportagent.model.ActionProposal) -> Unit,
    onAcceptOffer: (com.example.supportagent.model.ActionProposal) -> Unit,
    onDeclineOffer: (com.example.supportagent.model.ActionProposal) -> Unit,
    onAlternativeSelected: (cardId: String, alternativeId: String) -> Unit,
    onQuantityChanged: (cardId: String, newQty: Int) -> Unit,
    onItemToggled: (cardId: String, isSelected: Boolean) -> Unit,
    onSubmitToCart: (GroceryCartDeck) -> Unit,
    onEditAddressClick: () -> Unit
) {
    val isUser = message.sender == Sender.USER

    Column(
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (!isUser) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF64D2FF),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "Agent Intelligence",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64D2FF)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
                )
            }
        }

        val bubbleBg = if (isUser) Color(0xFF0A84FF) else if (isDarkTheme) Color(0xFF161722) else Color(0xFFFFFFFF)
        val bubbleBorder = if (isUser) Color(0x33FFFFFF) else if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)
        val bubbleTextColor = if (isUser || isDarkTheme) Color.White else Color(0xFF1C1C1E)

        // Apple-style message container
        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = bubbleBg
            ),
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.85f else 1.0f)
                .border(
                    width = 1.dp,
                    color = bubbleBorder,
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = bubbleTextColor,
                lineHeight = 21.sp,
                letterSpacing = (-0.1).sp,
                modifier = Modifier.padding(16.dp)
            )
        }

        // Horizontal Swipeable Grocery Flash Card Deck
        message.groceryDeck?.let { deck ->
            Spacer(modifier = Modifier.height(14.dp))
            GroceryFlashCardDeck(
                deck = deck,
                onAlternativeSelected = onAlternativeSelected,
                onQuantityChanged = onQuantityChanged,
                onItemToggled = onItemToggled,
                onSubmitToCart = onSubmitToCart,
                onEditAddressClick = onEditAddressClick
            )
        }

        // Context-Adaptive Action Card or Live Mission Tracker
        message.actionProposal?.let { proposal ->
            Spacer(modifier = Modifier.height(12.dp))

            when (proposal.status) {
                ActionStatus.PROPOSED -> {
                    ActionProposalCard(
                        proposal = proposal,
                        onFixItClick = onFixItClick
                    )
                }
                ActionStatus.EXECUTING,
                ActionStatus.REQUIRES_USER_DECISION,
                ActionStatus.COMPLETED -> {
                    LiveMissionTracker(
                        proposal = proposal,
                        onAcceptOffer = onAcceptOffer,
                        onDeclineOffer = onDeclineOffer
                    )
                }
                else -> {}
            }
        }
    }
}
