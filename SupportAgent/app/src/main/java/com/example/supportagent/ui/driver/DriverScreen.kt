package com.example.supportagent.ui.driver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.BatchStatus
import com.example.supportagent.model.DeliveryBatch
import com.example.supportagent.model.DriverProfile
import com.example.supportagent.model.VerificationStatus

@Composable
fun DriverCockpitScreen(
    driver: DriverProfile,
    batches: List<DeliveryBatch>,
    isDarkTheme: Boolean,
    onToggleOnline: () -> Unit,
    onAcceptBatch: (String) -> Unit,
    onCompleteBatch: (String) -> Unit,
    onToggleItemFound: (batchId: String, item: String) -> Unit = { _, _ -> },
    onAdvanceBatch: (batchId: String) -> Unit = {},
    onCashOut: () -> Unit = {},
    onSwitchToCustomer: () -> Unit,
    onToggleTheme: () -> Unit,
    onOpenCustomerChat: () -> Unit = {},
    onOpenOnboarding: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bgColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subTextColor = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderColor = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    var showVerificationDialog by remember { mutableStateOf(false) }
    var showCashOutDialog by remember { mutableStateOf(false) }
    var cashOutSuccessMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top Navigation: Role Switcher & Theme Toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Role switcher pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isDarkTheme) Color(0xFF1C1D28) else Color(0xFFE5E5EA))
                    .border(1.dp, borderColor, RoundedCornerShape(20.dp))
                    .clickable { onSwitchToCustomer() }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Switch to Customer",
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Customer Mode",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A84FF)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Zero Commission Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0D2818))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "0% COMMISSION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF30D158)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(1.dp, borderColor, CircleShape)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryTextColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Driver Profile Card with Online/Offline Switch
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardBg),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, borderColor, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0A84FF))
                        ) {
                            Text(
                                text = driver.fullName.take(2).uppercase(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = driver.fullName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Driver",
                                    tint = Color(0xFF30D158),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "${driver.vehicleModel} • ★ ${driver.rating}",
                                fontSize = 12.sp,
                                color = subTextColor
                            )
                        }
                    }

                    // Online/Offline Button
                    Button(
                        onClick = onToggleOnline,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (driver.isOnline) Color(0xFF30D158) else Color(0xFFFF453A)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (driver.isOnline) "ONLINE" else "OFFLINE",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Earnings Metrics Row
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Today's Earnings", fontSize = 11.sp, color = subTextColor)
                            if (driver.todayEarnings > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0A84FF))
                                        .clickable { showCashOutDialog = true }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("Cash Out", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                        Text(
                            text = driver.formattedTodayEarnings,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF30D158)
                        )
                    }
                    Column {
                        Text("Weekly Balance", fontSize = 11.sp, color = subTextColor)
                        Text(
                            text = driver.formattedWeeklyEarnings,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = primaryTextColor
                        )
                    }
                    Column {
                        Text("Completed", fontSize = 11.sp, color = subTextColor)
                        Text(
                            text = "${driver.completedTrips} trips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF64D2FF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(10.dp))

                // Verification & Membership Status Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) Color(0xFF1B1D28) else Color(0xFFE5E5EA))
                        .clickable { showVerificationDialog = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Provi Driver Pro • \$19.99/mo",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Text(
                                text = "All ID & Background Checks Verified",
                                fontSize = 10.sp,
                                color = Color(0xFF30D158)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Details",
                        tint = subTextColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Live Available Batches Feed
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (driver.isOnline) Color(0xFF30D158) else Color(0xFFFF453A))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (driver.isOnline) "NEARBY DISPATCH RADAR (${batches.size})" else "YOU ARE CURRENTLY OFFLINE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryTextColor,
                    letterSpacing = 0.5.sp
                )
            }
            if (driver.isOnline) {
                Text(
                    text = "Live Sync",
                    fontSize = 11.sp,
                    color = Color(0xFF30D158)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (!driver.isOnline) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = subTextColor,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "You are currently offline",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        text = "Switch status to ONLINE to receive nearby zero-commission batches.",
                        fontSize = 12.sp,
                        color = subTextColor,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else if (batches.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Text(
                    text = "Searching for high-paying grocery batches nearby...",
                    fontSize = 13.sp,
                    color = subTextColor
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(batches, key = { it.id }) { batch ->
                    BatchItemCard(
                        batch = batch,
                        cardBg = cardBg,
                        primaryTextColor = primaryTextColor,
                        subTextColor = subTextColor,
                        borderColor = borderColor,
                        onAccept = { onAcceptBatch(batch.id) },
                        onToggleItem = { item -> onToggleItemFound(batch.id, item) },
                        onAdvance = { onAdvanceBatch(batch.id) },
                        onComplete = { onCompleteBatch(batch.id) },
                        onOpenCustomerChat = onOpenCustomerChat
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Zero-Commission Driver Guarantee",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryTextColor
                                )
                                Text(
                                    text = "Provi does NOT take 30%-45% cut from your delivery pay or customer tips. You keep 100% of every dollar you earn.",
                                    fontSize = 10.sp,
                                    color = subTextColor,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        if (showVerificationDialog) {
            DriverVerificationDialog(
                driver = driver,
                isDarkTheme = isDarkTheme,
                onDismiss = { showVerificationDialog = false },
                onOpenOnboarding = {
                    showVerificationDialog = false
                    onOpenOnboarding()
                }
            )
        }

        if (showCashOutDialog) {
            DriverCashOutDialog(
                driver = driver,
                isDarkTheme = isDarkTheme,
                onDismiss = { showCashOutDialog = false },
                onConfirmPayout = {
                    val amountStr = driver.formattedTodayEarnings
                    onCashOut()
                    showCashOutDialog = false
                    cashOutSuccessMessage = "Successfully transferred $amountStr to your Chase Visa Debit card (•••• 4242) with $0.00 platform deduction."
                }
            )
        }

        if (cashOutSuccessMessage != null) {
            AlertDialog(
                onDismissRequest = { cashOutSuccessMessage = null },
                shape = RoundedCornerShape(22.dp),
                containerColor = cardBg,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payout Complete!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                    }
                },
                text = {
                    Text(
                        text = cashOutSuccessMessage ?: "",
                        fontSize = 13.sp,
                        color = subTextColor,
                        lineHeight = 18.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { cashOutSuccessMessage = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            )
        }
    }
}

@Composable
fun BatchItemCard(
    batch: DeliveryBatch,
    cardBg: Color,
    primaryTextColor: Color,
    subTextColor: Color,
    borderColor: Color,
    onAccept: () -> Unit,
    onToggleItem: (String) -> Unit,
    onAdvance: () -> Unit,
    onComplete: () -> Unit,
    onOpenCustomerChat: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF0071DC))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = batch.orderNumber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = batch.storeName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                }

                // Total payout pill
                Text(
                    text = batch.formattedTotalEarnings,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF30D158)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "📍 Pick up: ${batch.storeAddress}",
                fontSize = 11.sp,
                color = subTextColor
            )
            Text(
                text = "🏠 Deliver to: ${batch.deliveryAddress}",
                fontSize = 11.sp,
                color = subTextColor
            )

            if (batch.status != BatchStatus.AVAILABLE) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = borderColor)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "SHOPPING CHECKLIST (${batch.foundItems.size}/${batch.itemsSummary.size} Items)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64D2FF)
                    )
                    Text(
                        text = if (batch.status == BatchStatus.SHOPPING) "⚡ IN STORE" else "🚗 EN ROUTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF30D158)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                batch.itemsSummary.forEach { item ->
                    val isFound = batch.foundItems.contains(item)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onToggleItem(item) }
                            .padding(vertical = 3.dp, horizontal = 4.dp)
                    ) {
                        Checkbox(
                            checked = isFound,
                            onCheckedChange = { onToggleItem(item) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF30D158),
                                uncheckedColor = subTextColor
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = item,
                            fontSize = 12.sp,
                            color = if (isFound) subTextColor else primaryTextColor,
                            textDecoration = if (isFound) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // GPS Navigation & Customer Chat Action Bar for Driver
                val targetDestination = if (batch.status == BatchStatus.SHOPPING) batch.storeAddress else batch.deliveryAddress
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val uri = android.net.Uri.parse("google.navigation:q=" + android.net.Uri.encode(targetDestination))
                                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                mapIntent.setPackage("com.google.android.apps.maps")
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val fallbackUri = android.net.Uri.parse("geo:0,0?q=" + android.net.Uri.encode(targetDestination))
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, fallbackUri))
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF0A84FF).copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0A84FF)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = Color(0xFF64D2FF), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (batch.status == BatchStatus.SHOPPING) "GPS to Store" else "GPS to Door",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64D2FF)
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenCustomerChat,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF30D158).copy(alpha = 0.15f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF30D158)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Chat Customer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "${batch.itemCount} items • ${batch.formattedDistance} • Base $${batch.basePay} + Tip $${batch.tipAmount}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64D2FF)
                )

                if (batch.status == BatchStatus.AVAILABLE) {
                    Button(
                        onClick = onAccept,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Accept Batch", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                } else if (batch.status == BatchStatus.SHOPPING && batch.foundItems.size < batch.itemsSummary.size) {
                    Button(
                        onClick = onAdvance,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Start Delivery ➔", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                } else {
                    Button(
                        onClick = onComplete,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text("Complete & Collect (+$${batch.formattedTotalEarnings})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun DriverVerificationDialog(
    driver: DriverProfile,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onOpenOnboarding: () -> Unit = {}
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subTextColor = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderColor = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = cardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D2818))
                        .border(1.dp, Color(0xFF30D158), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Driver Membership",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        text = "Verified Identity & Zero Commission",
                        fontSize = 11.sp,
                        color = Color(0xFF30D158)
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Subscription Tier Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDarkTheme) Color(0xFF1B1D2C) else Color(0xFFF2F2F7)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Provi Unlimited Driver",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryTextColor
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0D2818))
                                    .border(1.dp, Color(0xFF30D158).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF30D158)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\$19.99 / month • Renews in 18 days",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0A84FF)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zero per-order commission. You keep 100% of the \$4.99 delivery fee + 100% of customer tips.",
                            fontSize = 10.sp,
                            color = subTextColor,
                            lineHeight = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ID & COMPLIANCE VERIFICATION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = subTextColor,
                    letterSpacing = 0.8.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Verification items list
                val verifications = listOf(
                    Triple("Driver's License (Gov ID)", "FL #R450-781-92 (Exp: 2029)", true),
                    Triple("MVR & Background Check", "Checkr Certified • Clear", true),
                    Triple("Vehicle Insurance & Registration", "${driver.vehicleModel} • Valid", true),
                    Triple("Instant Direct Deposit", "Chase Bank •••• 4242", true)
                )

                verifications.forEach { (title, subtitle, verified) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = primaryTextColor)
                            Text(subtitle, fontSize = 10.sp, color = subTextColor)
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Verified",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Savings Benchmark
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 You saved approximately \$214.50 in commissions this month compared to DoorDash & Instacart.",
                        fontSize = 10.sp,
                        color = primaryTextColor,
                        lineHeight = 14.sp
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onOpenOnboarding()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update KYC", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                }
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Close", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    )
}

@Composable
fun DriverCashOutDialog(
    driver: DriverProfile,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onConfirmPayout: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subTextColor = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderColor = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = cardBg,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Instant Payout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryTextColor
                    )
                    Text(
                        text = "Stripe Express • Zero Transfer Fee",
                        fontSize = 11.sp,
                        color = subTextColor
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Large Payout Callout
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .padding(vertical = 16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Transfer Amount",
                            fontSize = 11.sp,
                            color = subTextColor
                        )
                        Text(
                            text = driver.formattedTodayEarnings,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF30D158)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Transfer Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Destination", fontSize = 12.sp, color = subTextColor)
                    Text("Chase Visa (•••• 4242)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryTextColor)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Transfer Fee", fontSize = 12.sp, color = subTextColor)
                    Text("\$0.00 (Provi Benefit)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Estimated Arrival", fontSize = 12.sp, color = subTextColor)
                    Text("Instant (< 60 sec)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0A84FF))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmPayout,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158))
            ) {
                Text("Confirm Transfer", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = subTextColor)
            }
        }
    )
}
