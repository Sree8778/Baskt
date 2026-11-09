package com.example.supportagent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.ActionProposal
import com.example.supportagent.model.ActionStatus
import com.example.supportagent.model.StepStatus

@Composable
fun LiveMissionTracker(
    proposal: ActionProposal,
    onAcceptOffer: (ActionProposal) -> Unit,
    onDeclineOffer: (ActionProposal) -> Unit,
    modifier: Modifier = Modifier
) {
    val execution = proposal.execution ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1018)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = when (proposal.status) {
                    ActionStatus.COMPLETED -> Color(0xFF30D158)
                    ActionStatus.REQUIRES_USER_DECISION -> Color(0xFFFF9F0A)
                    else -> Color(0xFF0A84FF)
                },
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Live Status Header (Dynamic Island Style)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(
                                when (proposal.status) {
                                    ActionStatus.COMPLETED -> Color(0xFF30D158)
                                    ActionStatus.REQUIRES_USER_DECISION -> Color(0xFFFF9F0A)
                                    else -> Color(0xFF0A84FF).copy(alpha = pulseAlpha)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (proposal.status) {
                            ActionStatus.COMPLETED -> "MISSION COMPLETED"
                            ActionStatus.REQUIRES_USER_DECISION -> "DECISION REQUIRED"
                            else -> "LIVE EXECUTION IN PROGRESS"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (proposal.status) {
                            ActionStatus.COMPLETED -> Color(0xFF30D158)
                            ActionStatus.REQUIRES_USER_DECISION -> Color(0xFFFF9F0A)
                            else -> Color(0xFF64D2FF)
                        },
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = proposal.categoryBadge.split("•").firstOrNull()?.trim() ?: "TASK",
                    fontSize = 11.sp,
                    color = Color(0xFF8E8E93),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = proposal.title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp
            )

            // Hold Music Indicator Banner
            AnimatedVisibility(visible = execution.isHolding) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1B132C))
                        .border(1.dp, Color(0x33BF5AF2), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color(0xFFBF5AF2),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hold Music Active 🎵 • Monitoring queue in background...",
                        fontSize = 12.sp,
                        color = Color(0xFFD8B4FE),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Progress Checklist
            Text(
                text = "EXECUTION TIMELINE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF636366),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            execution.steps.forEach { step ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    when (step.status) {
                        StepStatus.COMPLETED -> {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF30D158),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        StepStatus.IN_PROGRESS -> {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color(0xFF0A84FF),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        StepStatus.ALERT -> {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9F0A),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        StepStatus.PENDING -> {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3A3A3C))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = step.description,
                        fontSize = 13.sp,
                        color = when (step.status) {
                            StepStatus.COMPLETED -> Color(0xFF8E8E93)
                            StepStatus.IN_PROGRESS -> Color.White
                            StepStatus.ALERT -> Color(0xFFFFD60A)
                            StepStatus.PENDING -> Color(0xFF48484A)
                        },
                        fontWeight = if (step.status == StepStatus.IN_PROGRESS) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Retention Decision Card
            AnimatedVisibility(visible = proposal.status == ActionStatus.REQUIRES_USER_DECISION && execution.pendingOffer != null) {
                val offer = execution.pendingOffer ?: return@AnimatedVisibility

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E170A)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                        .border(1.dp, Color(0xFFFF9F0A), RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9F0A),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Retention Offer Received",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD60A)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Representative offered: ${offer.offeredPrice} (regular ${offer.originalPrice}) for ${offer.duration}.",
                            fontSize = 13.sp,
                            color = Color(0xFFFEF08A),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { onAcceptOffer(proposal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Accept Deal", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                            }

                            OutlinedButton(
                                onClick = { onDeclineOffer(proposal) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF453A)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Insist on Cancel", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Completed Result Banner & External Action Handoff
            AnimatedVisibility(visible = proposal.status == ActionStatus.COMPLETED && execution.resultSummary != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0D2818))
                            .border(1.dp, Color(0x3330D158), RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF30D158),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = execution.resultSummary ?: "Completed",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFD1FAE5),
                                lineHeight = 18.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            com.example.supportagent.util.ActionExecutor.openExternalTarget(context, proposal)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1D26)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            text = when (proposal.category) {
                                com.example.supportagent.model.ActionCategory.SUPPORT_CALL -> "📞 Open Phone Dialer"
                                com.example.supportagent.model.ActionCategory.FOOD_DELIVERY -> "🛵 View Cart in Uber Eats"
                                com.example.supportagent.model.ActionCategory.GROCERY_RESTOCK -> "🛒 View Cart in Walmart"
                                com.example.supportagent.model.ActionCategory.TRAVEL_RENTAL -> "🚗 View in Turo"
                                com.example.supportagent.model.ActionCategory.LODGING_RESERVATION -> "🏡 View in Airbnb"
                                else -> "🛒 Open & View Cart"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Live Call Transcript Drawer
            Text(
                text = "LIVE AUDIO & DIALOGUE STREAM",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF636366),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF07080D))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(execution.liveTranscript) { item ->
                        Column(modifier = Modifier.padding(vertical = 3.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = item.speaker,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        item.speaker.contains("Voice") || item.speaker.contains("Agent") -> Color(0xFF64D2FF)
                                        item.speaker.contains("Rep") -> Color(0xFFFF9F0A)
                                        else -> Color(0xFF8E8E93)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = item.timestamp,
                                    fontSize = 10.sp,
                                    color = Color(0xFF48484A)
                                )
                            }
                            Text(
                                text = item.text,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color(0xFFE5E5EA),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
