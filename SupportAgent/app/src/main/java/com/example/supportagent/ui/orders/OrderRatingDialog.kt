package com.example.supportagent.ui.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.ActiveOrder

@Composable
fun OrderRatingDialog(
    order: ActiveOrder,
    isDarkTheme: Boolean,
    onSubmitRating: (stars: Int, compliments: List<String>, extraTip: Double) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableStateOf(5) }
    var selectedCompliments by remember { mutableStateOf(setOf("Fast & Friendly", "Careful with items")) }
    var extraTip by remember { mutableStateOf(3.00) }

    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    val complimentOptions = listOf(
        "⚡ Fast & Friendly",
        "🥑 Great Replacements",
        "📦 Careful with items",
        "🎯 Followed instructions",
        "🚗 Smooth Delivery"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = cardBg,
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF30D158).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF30D158), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Order Delivered!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryText,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "How was Alex Rivera's service for ${order.orderId}?",
                    fontSize = 12.sp,
                    color = subText,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 5 Star Rating
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star stars",
                                tint = if (star <= rating) Color(0xFFFFD60A) else subText,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Compliments Chips
                Text(
                    text = "Add Compliments",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    complimentOptions.chunked(2).forEach { rowCompliments ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            rowCompliments.forEach { comp ->
                                val isSelected = selectedCompliments.contains(comp)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) Color(0xFF0A84FF).copy(alpha = 0.2f) else if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                                        .border(1.dp, if (isSelected) Color(0xFF0A84FF) else borderCol, RoundedCornerShape(12.dp))
                                        .clickable {
                                            selectedCompliments = if (isSelected) {
                                                selectedCompliments - comp
                                            } else {
                                                selectedCompliments + comp
                                            }
                                        }
                                        .padding(vertical = 8.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = comp,
                                        fontSize = 10.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF64D2FF) else primaryText,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Extra Tip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Add Extra Tip",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Text(
                        text = "100% goes to Alex Rivera ($0 fees)",
                        fontSize = 10.sp,
                        color = Color(0xFF30D158)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(0.0, 2.0, 3.0, 5.0).forEach { tip ->
                        val isSelected = extraTip == tip
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) Color(0xFF30D158).copy(alpha = 0.2f) else if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                                .border(1.dp, if (isSelected) Color(0xFF30D158) else borderCol, RoundedCornerShape(12.dp))
                                .clickable { extraTip = tip }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (tip == 0.0) "None" else "+$${String.format(java.util.Locale.US, "%.0f", tip)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF30D158) else primaryText
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitRating(rating, selectedCompliments.toList(), extraTip) },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Submit Rating${if (extraTip > 0) " & Tip \$${String.format(java.util.Locale.US, "%.2f", extraTip)}" else ""}",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip", color = subText, fontSize = 12.sp)
            }
        }
    )
}
