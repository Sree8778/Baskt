package com.example.supportagent.ui.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.UserStoreCart
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutPaymentSheet(
    cart: UserStoreCart,
    selectedTip: Double,
    isDarkTheme: Boolean,
    onConfirmCheckout: (paymentMethod: String, instructions: String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPayment by remember { mutableStateOf("Google Pay") }
    var deliveryInstructions by remember { mutableStateOf("Leave at front door • Gate code #4412") }

    val bgColor = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    val finalTotal = cart.subtotal + 4.99 + selectedTip + cart.estimatedTax
    val totalSavings = cart.subtotal * 0.25

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.90f)
                .padding(horizontal = 18.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Review & Place Order",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "Zero Commission • Exact Store Shelf Prices",
                        fontSize = 11.sp,
                        color = Color(0xFF30D158)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = subText, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Address & Delivery ETA Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isDarkTheme) Color(0xFF1C1D2A) else Color(0xFFF2F2F7))
                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0A84FF), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Delivery Address", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(cart.deliveryAddress, fontSize = 11.sp, color = subText)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Doorstep Arrival: 25 - 35 min (Priority Dispatch)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF30D158))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Delivery Instructions
            Text("DELIVERY INSTRUCTIONS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subText, letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = deliveryInstructions,
                onValueChange = { deliveryInstructions = it },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = primaryText,
                    unfocusedTextColor = primaryText,
                    focusedBorderColor = Color(0xFF0A84FF),
                    unfocusedBorderColor = borderCol
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Payment Methods Selection
            Text("PAYMENT METHOD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subText, letterSpacing = 0.8.sp)
            Spacer(modifier = Modifier.height(6.dp))

            val paymentMethods = listOf(
                Pair("Google Pay", "Fast 1-tap checkout with Google Wallet"),
                Pair("Chase Visa (•••• 4242)", "Direct debit payment"),
                Pair("Apple Card / Mastercard (•••• 8812)", "Credit card")
            )

            paymentMethods.forEach { (method, desc) ->
                val isSelected = selectedPayment == method
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.12f) else if (isDarkTheme) Color(0xFF181A26) else Color(0xFFF9F9FA))
                        .border(1.dp, if (isSelected) Color(0xFF0A84FF) else borderCol, RoundedCornerShape(12.dp))
                        .clickable { selectedPayment = method }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedPayment = method },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0A84FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(method, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text(desc, fontSize = 10.sp, color = subText)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Price Breakdown Summary
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkTheme) Color(0xFF1B1D2C) else Color(0xFFF2F2F7)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, borderCol, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Items Subtotal (${cart.totalItemCount} items)", fontSize = 11.sp, color = subText)
                        Text(cart.formattedSubtotal, fontSize = 11.sp, color = primaryText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Provi App Markup", fontSize = 11.sp, color = subText)
                        Text("$0.00 (Zero Markup)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF30D158))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Flat Driver Delivery Fee", fontSize = 11.sp, color = subText)
                        Text("$4.99", fontSize = 11.sp, color = primaryText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Driver Tip (100% to Driver)", fontSize = 11.sp, color = subText)
                        Text(String.format(Locale.US, "$%.2f", selectedTip), fontSize = 11.sp, color = primaryText)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Sales Tax (7%)", fontSize = 11.sp, color = subText)
                        Text(cart.formattedEstimatedTax, fontSize = 11.sp, color = primaryText)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = borderCol)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Total to Pay", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = primaryText)
                        Text(
                            text = String.format(Locale.US, "$%.2f", finalTotal),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF30D158)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (totalSavings > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE))
                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "🎉 You are saving ~$${String.format(Locale.US, "%.2f", totalSavings)} vs Instacart's marked-up prices!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF30D158)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Place Order Button
            Button(
                onClick = { onConfirmCheckout(selectedPayment, deliveryInstructions) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0071DC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pay with $selectedPayment • ${String.format(Locale.US, "$%.2f", finalTotal)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
