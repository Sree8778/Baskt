package com.example.supportagent.ui.chat

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.DriverChatMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerDriverChatSheet(
    driverName: String = "Alex Rivera",
    driverVehicle: String = "Toyota RAV4 (Silver)",
    driverRating: Double = 4.98,
    messages: List<DriverChatMessage>,
    isDarkTheme: Boolean,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }

    val bgColor = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)
    val driverBubbleBg = if (isDarkTheme) Color(0xFF202230) else Color(0xFFE5E5EA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = bgColor,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 16.dp)
        ) {
            // Header: Driver Profile & Quick Call
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A84FF))
                    ) {
                        Text(
                            text = driverName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = driverName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Driver",
                                tint = Color(0xFF30D158),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Text(
                            text = "$driverVehicle • ★ $driverRating",
                            fontSize = 11.sp,
                            color = subText
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Call Button
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:5615550192"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call Driver",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = subText,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = borderCol)

            // Chat Messages Stream
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isFromCustomer) {
                        // Customer Bubble (Right aligned, Blue)
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp))
                                    .background(Color(0xFF0A84FF))
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    } else {
                        // Driver Bubble (Left aligned, Neutral Gray)
                        Row(
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .clip(RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                                    .background(driverBubbleBg)
                                    .border(1.dp, borderCol, RoundedCornerShape(18.dp, 18.dp, 18.dp, 4.dp))
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Column {
                                    Text(
                                        text = msg.text,
                                        fontSize = 13.sp,
                                        color = primaryText,
                                        lineHeight = 18.sp
                                    )
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 9.sp,
                                        color = subText,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Pre-set Chips
            val quickChips = listOf(
                "Gate code is #4412",
                "Leave at front door",
                "Ring bell upon arrival",
                "Green bananas please",
                "No contact delivery"
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                items(quickChips) { chipText ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isDarkTheme) Color(0xFF1E202E) else Color(0xFFE5E5EA))
                            .border(1.dp, borderCol, RoundedCornerShape(14.dp))
                            .clickable { onSendMessage(chipText) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = chipText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = primaryText
                        )
                    }
                }
            }

            // Message Input Field
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Message $driverName...", fontSize = 13.sp, color = subText)
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = if (isDarkTheme) Color(0xFF1C1D2A) else Color(0xFFF2F2F7),
                        unfocusedContainerColor = if (isDarkTheme) Color(0xFF1C1D2A) else Color(0xFFF2F2F7),
                        focusedBorderColor = Color(0xFF0A84FF),
                        unfocusedBorderColor = borderCol,
                        focusedTextColor = primaryText,
                        unfocusedTextColor = primaryText
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A84FF))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
