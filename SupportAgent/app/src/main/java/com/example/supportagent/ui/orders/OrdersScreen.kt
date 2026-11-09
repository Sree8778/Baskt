package com.example.supportagent.ui.orders

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supportagent.model.ActiveOrder
import com.example.supportagent.model.CartItem
import com.example.supportagent.model.OrderProgressStage

@Composable
fun OrdersScreen(
    activeOrder: ActiveOrder?,
    pastOrders: List<ActiveOrder>,
    isDarkTheme: Boolean,
    onSwitchToDriver: () -> Unit,
    onToggleTheme: () -> Unit,
    onStartShopping: () -> Unit,
    onAdvanceOrderStage: () -> Unit,
    onOpenDriverChat: () -> Unit = {},
    onOpenRating: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val bgColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))

        // Top App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Text(
                    text = "Track & Orders",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryText,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Live Doorstep Dispatch",
                    fontSize = 12.sp,
                    color = subText
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Role Switcher
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) Color(0xFF1C1D26) else Color(0xFFE5E5EA))
                        .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { onSwitchToDriver() }
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

                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (activeOrder != null) {
                item {
                    // Section Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF30D158))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE DELIVERY",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF30D158),
                                letterSpacing = 1.sp
                            )
                        }

                        // Simulation button to advance stage
                        TextButton(
                            onClick = onAdvanceOrderStage,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Simulate Next Step ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0A84FF)
                            )
                        }
                    }

                    // Uber Eats / Instacart Live Active Delivery Card
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderCol, RoundedCornerShape(22.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Store & ETA
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column {
                                    Text(
                                        text = activeOrder.storeName,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    Text(
                                        text = "Order ${activeOrder.orderId}",
                                        fontSize = 11.sp,
                                        color = subText
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isDarkTheme) Color(0xFF0E2215) else Color(0xFFE8F8EE))
                                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = "ETA ${activeOrder.estimatedArrivalMinutes} min",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF30D158)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // 5-Stage Visual Progress Bar
                            OrderProgressIndicator(
                                currentStage = activeOrder.progress,
                                isDarkTheme = isDarkTheme
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Interactive Simulated Live Route Map Card
                            LiveOrderMapCard(
                                activeOrder = activeOrder,
                                isDarkTheme = isDarkTheme
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Simulated Route Card
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isDarkTheme) Color(0xFF1B1C28) else Color(0xFFE5E5EA))
                                    .padding(14.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0071DC))
                                        ) {
                                            Icon(Icons.Default.Storefront, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = "Picked up at ${activeOrder.storeName}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                            Text(text = "22100 S State Rd 7, Boca Raton", fontSize = 10.sp, color = subText)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF30D158))
                                        ) {
                                            Icon(Icons.Default.Home, contentDescription = null, tint = Color.White, modifier = Modifier.size(13.dp))
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(text = "Delivering to Door", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                                            Text(text = activeOrder.deliveryAddress, fontSize = 10.sp, color = subText)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Driver Profile Card
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(if (isDarkTheme) Color(0xFF181924) else Color(0xFFF2F2F7))
                                    .padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                                                )
                                            )
                                    ) {
                                        Text("AL", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = activeOrder.driverName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = primaryText
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(13.dp))
                                        }
                                        Text(
                                            text = "${activeOrder.driverVehicle} • ★ ${activeOrder.driverRating}",
                                            fontSize = 11.sp,
                                            color = subText
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_DIAL, android.net.Uri.parse("tel:5615550192"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF30D158).copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = Color(0xFF30D158), modifier = Modifier.size(16.dp))
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = onOpenDriverChat,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF0A84FF).copy(alpha = 0.2f))
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = "Message", tint = Color(0xFF0A84FF), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Provi 0% Markup Savings Proof
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFF0D2818))
                                    .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "You Saved ${activeOrder.formattedSavings} vs Instacart",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF30D158)
                                        )
                                        Text(
                                            text = "Exact shelf price paid: ${activeOrder.formattedSubtotal} • $0 app markup",
                                            fontSize = 10.sp,
                                            color = Color(0xFF8E8E93)
                                        )
                                    }
                                    Text(
                                        text = activeOrder.formattedTotalPaid,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (activeOrder.progress == OrderProgressStage.DELIVERED) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = onOpenRating,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Rate Alex Rivera & Tip Courier", fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 13.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                item {
                    // No Active Order empty state
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(if (isDarkTheme) Color(0xFF1B1C28) else Color(0xFFE5E5EA))
                            ) {
                                Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Color(0xFF0A84FF), modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Active Delivery",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "When you checkout from Walmart or any partner store, real-time driver tracking will appear here.",
                                fontSize = 12.sp,
                                color = subText,
                                lineHeight = 17.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onStartShopping,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text("Browse Stores", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // Past Orders History Header
            item {
                Text(
                    text = "Past Orders",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(pastOrders, key = { it.orderId }) { pastOrder ->
                PastOrderCard(
                    order = pastOrder,
                    isDarkTheme = isDarkTheme
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun OrderProgressIndicator(
    currentStage: OrderProgressStage,
    isDarkTheme: Boolean
) {
    val stages = listOf(
        OrderProgressStage.ORDER_CONFIRMED,
        OrderProgressStage.DRIVER_ASSIGNED,
        OrderProgressStage.SHOPPING,
        OrderProgressStage.OUT_FOR_DELIVERY,
        OrderProgressStage.DELIVERED
    )

    val currentStep = currentStage.stepNumber
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            stages.forEachIndexed { index, stage ->
                val isCompleted = stage.stepNumber <= currentStep
                val isCurrent = stage.stepNumber == currentStep

                val circleColor = when {
                    isCompleted -> Color(0xFF30D158)
                    else -> if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFD1D1D6)
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(if (isCurrent) 22.dp else 16.dp)
                        .clip(CircleShape)
                        .background(circleColor)
                ) {
                    if (isCompleted) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                if (index < stages.size - 1) {
                    val nextCompleted = stages[index + 1].stepNumber <= currentStep
                    val lineColor = if (nextCompleted) Color(0xFF30D158) else if (isDarkTheme) Color(0xFF2C2C2E) else Color(0xFFD1D1D6)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(3.dp)
                            .background(lineColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = currentStage.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF30D158)
            )
            Text(
                text = currentStage.subtitle,
                fontSize = 11.sp,
                color = subText
            )
        }
    }
}

@Composable
fun PastOrderCard(
    order: ActiveOrder,
    isDarkTheme: Boolean
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(16.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Column {
                Text(text = order.storeName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = primaryText)
                Text(text = "${order.orderId} • Delivered", fontSize = 11.sp, color = subText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Saved ${order.formattedSavings} with 0% markup", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF30D158))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(text = order.formattedTotalPaid, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText)
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0A84FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Reorder", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun LiveOrderMapCard(
    activeOrder: ActiveOrder,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val mapBg = if (isDarkTheme) Color(0xFF0C1017) else Color(0xFFE8EEF5)
    val roadGridColor = if (isDarkTheme) Color(0x1AFFFFFF) else Color(0x2E000000)
    val majorRoadColor = if (isDarkTheme) Color(0x380A84FF) else Color(0x280071DC)
    val waterColor = if (isDarkTheme) Color(0x250A84FF) else Color(0x3364D2FF)
    val routeGlowColor = Color(0xFF30D158)
    val borderCol = if (isDarkTheme) Color(0x2EFFFFFF) else Color(0x2E000000)

    val transition = rememberInfiniteTransition(label = "RadarMapTransition")
    val pulseRadius by transition.animateFloat(
        initialValue = 8f,
        targetValue = 36f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseRadius"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    val simulatedDriveProgress by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.86f,
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "DriveProgress"
    )

    val staticProgressT = when (activeOrder.progress) {
        OrderProgressStage.ORDER_CONFIRMED -> 0.05f
        OrderProgressStage.DRIVER_ASSIGNED -> 0.22f
        OrderProgressStage.SHOPPING -> 0.12f
        OrderProgressStage.OUT_FOR_DELIVERY -> simulatedDriveProgress
        OrderProgressStage.DELIVERED -> 0.98f
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(mapBg)
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Grid street network
            val stepX = w / 7f
            for (i in 0..7) {
                drawLine(
                    color = roadGridColor,
                    start = Offset(i * stepX, 0f),
                    end = Offset(i * stepX, h),
                    strokeWidth = 2.dp.toPx()
                )
            }
            val stepY = h / 4f
            for (j in 0..4) {
                drawLine(
                    color = roadGridColor,
                    start = Offset(0f, j * stepY),
                    end = Offset(w, j * stepY),
                    strokeWidth = 2.dp.toPx()
                )
            }

            // 2. Diagonal arterial highways (Glades Rd & Florida Turnpike)
            drawLine(
                color = majorRoadColor,
                start = Offset(0f, h * 0.45f),
                end = Offset(w, h * 0.55f),
                strokeWidth = 5.dp.toPx()
            )
            drawLine(
                color = majorRoadColor,
                start = Offset(w * 0.25f, 0f),
                end = Offset(w * 0.45f, h),
                strokeWidth = 4.dp.toPx()
            )

            // 3. Waterway canal accent
            val waterPath = Path().apply {
                moveTo(0f, h * 0.85f)
                cubicTo(w * 0.3f, h * 0.95f, w * 0.7f, h * 0.75f, w, h * 0.88f)
            }
            drawPath(
                path = waterPath,
                color = waterColor,
                style = Stroke(width = 10.dp.toPx())
            )

            // 4. Bezier Route curve (Store to Doorstep)
            val p0 = Offset(w * 0.15f, h * 0.68f)
            val p1 = Offset(w * 0.32f, h * 0.26f)
            val p2 = Offset(w * 0.62f, h * 0.82f)
            val p3 = Offset(w * 0.85f, h * 0.30f)

            val routePath = Path().apply {
                moveTo(p0.x, p0.y)
                cubicTo(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y)
            }

            // Route background glow shadow
            drawPath(
                path = routePath,
                color = routeGlowColor.copy(alpha = 0.25f),
                style = Stroke(width = 8.dp.toPx(), pathEffect = PathEffect.cornerPathEffect(12f))
            )

            // Sharp main route path
            drawPath(
                path = routePath,
                color = routeGlowColor,
                style = Stroke(
                    width = 4.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 10f), 0f)
                )
            )

            // 5. Compute vehicle position on Bezier
            val u = 1f - staticProgressT
            val tt = staticProgressT * staticProgressT
            val uu = u * u
            val uuu = uu * u
            val ttt = tt * staticProgressT

            val carX = uuu * p0.x + 3 * uu * staticProgressT * p1.x + 3 * u * tt * p2.x + ttt * p3.x
            val carY = uuu * p0.y + 3 * uu * staticProgressT * p1.y + 3 * u * tt * p2.y + ttt * p3.y
            val carPos = Offset(carX, carY)

            // 6. Pulse radar wave around vehicle
            drawCircle(
                color = Color(0xFF30D158).copy(alpha = pulseAlpha),
                radius = pulseRadius.dp.toPx(),
                center = carPos
            )

            // 7. Store pin marker (P0)
            drawCircle(
                color = Color(0xFF0071DC),
                radius = 12.dp.toPx(),
                center = p0
            )
            drawCircle(
                color = Color.White,
                radius = 5.dp.toPx(),
                center = p0
            )

            // 8. Doorstep customer marker (P3)
            drawCircle(
                color = Color(0xFF30D158),
                radius = 13.dp.toPx(),
                center = p3
            )
            drawCircle(
                color = Color.White,
                radius = 6.dp.toPx(),
                center = p3
            )

            // 9. Delivery vehicle marker
            drawCircle(
                color = Color.Black,
                radius = 11.dp.toPx(),
                center = carPos
            )
            drawCircle(
                color = Color(0xFF30D158),
                radius = 8.dp.toPx(),
                center = carPos
            )
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = carPos
            )
        }

        // Live Overlays on top of the Map
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF30D158))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "LIVE GPS • ${activeOrder.estimatedArrivalMinutes} min",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.75f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "0% Markup Delivery",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF30D158)
                )
            }
        }

        // Bottom route corridor pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.70f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Glades Rd ➔ S Military Trl (1.4 mi)",
                fontSize = 9.sp,
                color = Color(0xFFE5E5EA),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
