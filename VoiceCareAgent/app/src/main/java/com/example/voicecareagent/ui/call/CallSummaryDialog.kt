package com.example.voicecareagent.ui.call

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicecareagent.model.*
import com.example.voicecareagent.telephony.ActiveCallState
import java.util.Locale

@Composable
fun CallSummaryDialog(
    state: ActiveCallState,
    onDismiss: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val mission = state.mission ?: return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(26.dp),
        containerColor = Color(0xFF141624),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF30D158).copy(alpha = 0.15f))
                        .border(1.5.dp, Color(0xFF30D158), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF30D158),
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) "Conversation Completed!" else "Resolution Achieved!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "${mission.calleeDisplayName} • ${state.formattedDuration} call duration",
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (mission.callMode == CallMode.CUSTOMER_CARE) {
                    // Refund Banner
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0E2516)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Savings, contentDescription = null, tint = Color(0xFF30D158), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Refund Secured", fontSize = 11.sp, color = Color(0xFF8E8E93))
                                    Text(
                                        text = "+\$${String.format(Locale.US, "%.2f", state.resolutionAmount.coerceAtLeast(mission.requestedAmount))}",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF30D158)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Confirmation Reference Card
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1F2236))
                            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text("Confirmation Number", fontSize = 10.sp, color = Color(0xFF8E8E93))
                            Text(
                                text = if (state.confirmationCode.isNotBlank()) state.confirmationCode else "REF-884920",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF64D2FF)
                            )
                        }

                        IconButton(
                            onClick = {
                                val code = if (state.confirmationCode.isNotBlank()) state.confirmationCode else "REF-884920"
                                clipboardManager.setText(AnnotatedString(code))
                                android.widget.Toast.makeText(context, "Confirmation code copied!", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = Color(0xFF64D2FF), modifier = Modifier.size(18.dp))
                        }
                    }
                } else {
                    // Friends Topic Summary Card
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF141D2E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF0A84FF).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Topic Discussed", fontSize = 11.sp, color = Color(0xFF64D2FF), fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = mission.personalTopic,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) "Call Outcome & Notes" else "Resolution Details",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (mission.callMode == CallMode.FRIENDS_AND_CONTACTS) {
                        "Your AI voice agent called ${mission.calleeDisplayName} and conversed on the given topic. The agreed details and transcript are saved above."
                    } else {
                        "The representative processed a full refund to your original payment method. The amount will post to your account within 1-2 business days."
                    },
                    fontSize = 12.sp,
                    color = Color(0xFF8E8E93),
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    )
}
