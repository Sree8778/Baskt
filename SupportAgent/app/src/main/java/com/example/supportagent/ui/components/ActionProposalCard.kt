package com.example.supportagent.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supportagent.model.ActionCategory
import com.example.supportagent.model.ActionIconType
import com.example.supportagent.model.ActionProposal

@Composable
fun ActionProposalCard(
    proposal: ActionProposal,
    onFixItClick: (ActionProposal) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14151F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x33FFFFFF), Color(0x0FFFFFFF))
                ),
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Pill & Duration
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF222436))
                        .border(1.dp, Color(0x26FFFFFF), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = proposal.categoryBadge,
                        color = Color(0xFF8E8E93),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = Color(0xFF8E8E93),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = proposal.estimatedTime,
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title
            Text(
                text = proposal.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.3).sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Target
            Text(
                text = "${proposal.targetEntity} • ${proposal.targetContact}",
                fontSize = 13.sp,
                color = Color(0xFF0A84FF),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Strategy Steps
            Text(
                text = "EXECUTION PLAN",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF636366),
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            proposal.strategySteps.take(4).forEachIndexed { index, step ->
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2C2C2E))
                    ) {
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E5EA)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Text(
                        text = step,
                        fontSize = 13.sp,
                        color = Color(0xFFD1D1D6),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Guardrails Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0D0E14))
                    .border(1.dp, Color(0x1AFFFFFF), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF30D158),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = proposal.guardrails,
                    fontSize = 11.sp,
                    color = Color(0xFF8E8E93),
                    maxLines = 2,
                    lineHeight = 15.sp
                )
            }

            // Cart Items Preview
            if (proposal.cartItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "ITEMS TO ADD TO CART",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64D2FF),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    proposal.cartItems.forEach { item ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1E2030))
                                .border(1.dp, Color(0x3364D2FF), RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "🛒 $item",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFE0F2FE)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Apple Pay / Dynamic Island Context-Adaptive Action Button
            val buttonGradient = when (proposal.category) {
                ActionCategory.FOOD_DELIVERY -> listOf(Color(0xFFFF9F0A), Color(0xFFFF375F))
                ActionCategory.GROCERY_RESTOCK -> listOf(Color(0xFF0A84FF), Color(0xFF5E5CE6))
                ActionCategory.TRAVEL_RENTAL, ActionCategory.LODGING_RESERVATION -> listOf(Color(0xFF30D158), Color(0xFF0A84FF))
                ActionCategory.DISPUTE_REFUND -> listOf(Color(0xFFBF5AF2), Color(0xFFFF375F))
                else -> listOf(Color(0xFF0A84FF), Color(0xFFBF5AF2))
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(brush = Brush.horizontalGradient(buttonGradient))
                    .clickable {
                        onFixItClick(proposal)
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    val icon = when (proposal.actionIcon) {
                        ActionIconType.PHONE -> Icons.Default.PhoneInTalk
                        ActionIconType.RESTAURANT -> Icons.Default.Restaurant
                        ActionIconType.GROCERY -> Icons.Default.ShoppingCart
                        ActionIconType.CAR -> Icons.Default.DirectionsCar
                        ActionIconType.HOTEL -> Icons.Default.Hotel
                        ActionIconType.SHIELD -> Icons.Default.Shield
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = proposal.actionButtonText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = (-0.2).sp
                    )

                    if (proposal.requiresBiometric) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Fingerprint,
                            contentDescription = "Biometric Required",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
