package com.example.supportagent.ui.cart

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supportagent.model.CartItem
import com.example.supportagent.model.UserStoreCart

@Composable
fun CartsScreen(
    cart: UserStoreCart,
    onQuantityChanged: (itemId: String, newQty: Int) -> Unit,
    onRemoveItem: (itemId: String) -> Unit,
    onClearCart: () -> Unit,
    onEditAddressClick: () -> Unit,
    onStartShoppingClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    showOrderSuccessDialog: Boolean,
    lastOrderNumber: String,
    onDismissSuccessDialog: () -> Unit,
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean = true
) {
    val bgColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryTextColor = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subTextColor = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderColor = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)
    if (showOrderSuccessDialog) {
        AlertDialog(
            onDismissRequest = onDismissSuccessDialog,
            containerColor = Color(0xFF14151F),
            shape = RoundedCornerShape(24.dp),
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
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Order Placed!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Your order $lastOrderNumber has been placed with Walmart Supercenter.",
                        fontSize = 14.sp,
                        color = Color(0xFFE5E5EA),
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C1D28))
                            .padding(12.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivery Window", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                Text("Today, in 45-60 min", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Delivering to", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                Text(cart.deliveryAddress, fontSize = 11.sp, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismissSuccessDialog,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                ) {
                    Text("Done", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Top App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column {
                Text(
                    text = "My Carts",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryTextColor,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "Personal Grocery & Retail Hub",
                    fontSize = 12.sp,
                    color = subTextColor
                )
            }

            if (cart.items.isNotEmpty()) {
                TextButton(
                    onClick = onClearCart,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFFF453A))
                ) {
                    Text("Clear Cart", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Store Filter Switcher (All / Walmart / Instacart)
        var selectedFilter by remember { mutableStateOf("ALL") }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            val totalCount = cart.items.size
            val wmtCount = cart.walmartItems.size
            val instaCount = cart.instacartItems.size

            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All ($totalCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0A84FF),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF141520),
                    labelColor = Color(0xFF8E8E93)
                )
            )

            FilterChip(
                selected = selectedFilter == "WALMART",
                onClick = { selectedFilter = "WALMART" },
                label = { Text("Walmart ($wmtCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0071DC),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF141520),
                    labelColor = Color(0xFF8E8E93)
                )
            )

            FilterChip(
                selected = selectedFilter == "INSTACART",
                onClick = { selectedFilter = "INSTACART" },
                label = { Text("Instacart ($instaCount)") },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF004B87),
                    selectedLabelColor = Color.White,
                    containerColor = Color(0xFF141520),
                    labelColor = Color(0xFF8E8E93)
                )
            )
        }

        val filteredItems = when (selectedFilter) {
            "WALMART" -> cart.walmartItems
            "INSTACART" -> cart.instacartItems
            else -> cart.items
        }

        // Active Store Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFF0B1E38), Color(0xFF10192A))
                    )
                )
                .border(1.dp, Color(0x330071DC), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0071DC))
                    ) {
                        Text(
                            text = "🛍️",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Multi-Store Smart Cart",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1E3A24))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "SYNCHRONIZED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF30D158)
                                )
                            }
                        }
                        Text(
                            text = "${cart.walmartItems.size} at Walmart • ${cart.instacartItems.size} at Instacart Stores",
                            fontSize = 11.sp,
                            color = Color(0xFF64D2FF)
                        )
                    }
                }

                // Delivery address pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF161722))
                        .border(1.dp, Color(0x2EFFFFFF), RoundedCornerShape(10.dp))
                        .clickable { onEditAddressClick() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Address",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E5EA)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (cart.items.isEmpty()) {
            // Empty State
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF141520))
                            .border(1.dp, Color(0x26FFFFFF), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color(0xFF636366),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Your Cart is Empty",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Items you approve from the flash card deck in the Assistant will appear here in your live cart.",
                        fontSize = 13.sp,
                        color = Color(0xFF8E8E93),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onStartShoppingClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Start Shopping with Assistant", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
        var selectedTip by remember { mutableStateOf(5.00) }

        // Cart Items List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredItems, key = { it.id }) { item ->
                CartItemRow(
                    item = item,
                    isDarkTheme = isDarkTheme,
                    onQuantityChanged = { newQty -> onQuantityChanged(item.id, newQty) },
                    onRemove = { onRemoveItem(item.id) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                OrderSummaryCard(
                    cart = cart,
                    isDarkTheme = isDarkTheme,
                    selectedTip = selectedTip,
                    onTipSelected = { selectedTip = it }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

            // Checkout Footer
            Button(
                onClick = onCheckoutClick,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071DC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .padding(bottom = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Place Multi-Store Orders • ${cart.formattedFinalTotal}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    isDarkTheme: Boolean = true,
    onQuantityChanged: (Int) -> Unit,
    onRemove: () -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Product Image
            AsyncImage(
                model = item.imageUrl.ifBlank { "https://i5.walmartimages.com/seo/Great-Value-Whole-Vitamin-D-Milk-Gallon-128-fl-oz_6a7b1b6d-74d5-4e78-9e12-32a76f2dbe59.jpeg" },
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Title, Brand & Price
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(item.storeOrigin.brandColorHex))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.storeOrigin.storeBadge,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(item.storeOrigin.accentColorHex)
                        )
                    }
                    if (item.brand.isNotBlank()) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.brand.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0A84FF),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.formattedTotalPrice,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF30D158)
                    )
                    if (item.quantity > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(${item.formattedPrice} ea)",
                            fontSize = 11.sp,
                            color = subText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stepper & Remove
            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = subText,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                        .border(1.dp, borderCol, RoundedCornerShape(10.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onQuantityChanged(item.quantity - 1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = primaryText,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "${item.quantity}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChanged(item.quantity + 1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = primaryText,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(
    cart: UserStoreCart,
    isDarkTheme: Boolean = true,
    selectedTip: Double = 5.00,
    onTipSelected: (Double) -> Unit = {}
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    val finalTotalWithTip = cart.subtotal + 4.99 + selectedTip + cart.estimatedTax

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Order Summary & Tip",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Subtotal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Items Subtotal (${cart.totalItemCount} items)", fontSize = 12.sp, color = subText)
                Text(cart.formattedSubtotal, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Zero Markup callout
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Provi App Commission Fee", fontSize = 12.sp, color = subText)
                Text("\$0.00 (Zero Markup)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Flat Delivery
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Flat Driver Delivery Fee", fontSize = 12.sp, color = subText)
                Text("\$4.99", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = primaryText)
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tax
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Estimated Sales Tax (7%)", fontSize = 12.sp, color = subText)
                Text(cart.formattedEstimatedTax, fontSize = 12.sp, color = primaryText)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = borderCol)
            Spacer(modifier = Modifier.height(10.dp))

            // Driver Tip Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Driver Tip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "100% goes directly to your driver",
                        fontSize = 10.sp,
                        color = Color(0xFF30D158)
                    )
                }
                Text(
                    text = String.format(java.util.Locale.US, "$%.2f", selectedTip),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tip Option Chips
            val tipOptions = listOf(2.00, 4.00, 5.00, 7.00, 10.00)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tipOptions.forEach { tipVal ->
                    val isSelected = selectedTip == tipVal
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) Color(0xFF0A84FF) else if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                            .border(1.dp, if (isSelected) Color.Transparent else borderCol, RoundedCornerShape(10.dp))
                            .clickable { onTipSelected(tipVal) }
                            .padding(vertical = 7.dp)
                    ) {
                        Text(
                            text = String.format(java.util.Locale.US, "$%.0f", tipVal),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else primaryText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = borderCol)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Total", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = primaryText)
                Text(
                    text = String.format(java.util.Locale.US, "$%.2f", finalTotalWithTip),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF30D158)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Savings callout banner
            val estimatedSavings = cart.subtotal * 0.25
            if (estimatedSavings > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🎉 You save ~$${String.format(java.util.Locale.US, "%.2f", estimatedSavings)} with Provi 0% markup vs Instacart / Uber Eats",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF30D158)
                    )
                }
            }
        }
    }
}
