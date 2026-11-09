package com.example.supportagent.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supportagent.model.GroceryCartDeck
import com.example.supportagent.model.GroceryProductCard
import com.example.supportagent.model.ProductAlternative
import com.example.supportagent.model.StockStatus

@Composable
fun GroceryFlashCardDeck(
    deck: GroceryCartDeck,
    modifier: Modifier = Modifier,
    onAlternativeSelected: (cardId: String, alternativeId: String) -> Unit,
    onQuantityChanged: (cardId: String, newQty: Int) -> Unit,
    onItemToggled: (cardId: String, isSelected: Boolean) -> Unit,
    onSubmitToCart: (GroceryCartDeck) -> Unit,
    onEditAddressClick: () -> Unit
) {
    if (deck.cards.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { deck.cards.size })

    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1018)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x4064D2FF), Color(0x10FFFFFF))
                ),
                shape = RoundedCornerShape(26.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp)
        ) {
            // Deck Header: Store + Counter
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A84FF))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WALMART CATALOG CURATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64D2FF),
                        letterSpacing = 1.2.sp
                    )
                }

                Text(
                    text = "${pagerState.currentPage + 1} of ${deck.cards.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8E8E93)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                repeat(deck.cards.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF0A84FF) else Color(0xFF2C2C2E),
                        label = "dotColor"
                    )
                    val dotWidth by androidx.compose.animation.core.animateDpAsState(
                        targetValue = if (isSelected) 22.dp else 6.dp,
                        label = "dotWidth"
                    )

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(5.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(3.dp))
                            .background(dotColor)
                    )
                }
            }

            // Horizontal Swipeable Pager Cards
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 20.dp),
                pageSpacing = 12.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val card = deck.cards[page]
                GroceryItemFlashCard(
                    card = card,
                    onAlternativeSelected = { altId -> onAlternativeSelected(card.id, altId) },
                    onQuantityChanged = { newQty -> onQuantityChanged(card.id, newQty) },
                    onItemToggled = { selected -> onItemToggled(card.id, selected) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom Summary & Master Cart Injection Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                // Address Bar with Edit Tap
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161722))
                        .border(1.dp, Color(0x1FFFFFFF), RoundedCornerShape(12.dp))
                        .clickable { onEditAddressClick() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = Color(0xFFFF9F0A),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = deck.deliveryAddress,
                            fontSize = 11.sp,
                            color = Color(0xFFE5E5EA),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Edit",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A84FF)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Total & Count Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${deck.selectedCards.size} Items Verified",
                        fontSize = 12.sp,
                        color = Color(0xFF8E8E93),
                        fontWeight = FontWeight.Medium
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Estimated Total: ",
                            fontSize = 12.sp,
                            color = Color(0xFF8E8E93)
                        )
                        Text(
                            text = deck.formattedTotal,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Master Load into Walmart Cart Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF0A84FF), Color(0xFF0071DC))
                            )
                        )
                        .clickable { onSubmitToCart(deck) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🛒 Add ${deck.selectedCards.size} Items to Cart (${deck.formattedTotal})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GroceryItemFlashCard(
    card: GroceryProductCard,
    onAlternativeSelected: (alternativeId: String) -> Unit,
    onQuantityChanged: (newQty: Int) -> Unit,
    onItemToggled: (Boolean) -> Unit
) {
    val activeAlt = card.alternatives.find { it.id == card.activeAlternativeId }
    val displayTitle = activeAlt?.title ?: card.matchedTitle
    val displayBrand = activeAlt?.brand ?: card.brand
    val displayPrice = activeAlt?.formattedPrice ?: card.formattedPrice
    val displayImage = activeAlt?.imageUrl ?: card.imageUrl
    val displayStock = activeAlt?.stockStatus ?: card.stockStatus

    val isOutOfStock = displayStock == StockStatus.OUT_OF_STOCK

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141522)),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (isOutOfStock) Color(0xFFFF9F0A).copy(alpha = 0.5f) else Color(0x26FFFFFF),
                RoundedCornerShape(20.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Card Top Row: Category Pill & Stock Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val activeOrigin = activeAlt?.storeOrigin ?: card.storeOrigin
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(activeOrigin.brandColorHex))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = activeOrigin.storeBadge,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(activeOrigin.accentColorHex)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E2030))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = card.queryItem.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64D2FF)
                        )
                    }
                }

                // Stock status badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (displayStock) {
                                    StockStatus.IN_STOCK -> Color(0xFF30D158)
                                    StockStatus.LOW_STOCK -> Color(0xFFFF9F0A)
                                    StockStatus.OUT_OF_STOCK -> Color(0xFFFF453A)
                                }
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (displayStock) {
                            StockStatus.IN_STOCK -> "IN STOCK"
                            StockStatus.LOW_STOCK -> "LOW STOCK"
                            StockStatus.OUT_OF_STOCK -> "OUT OF STOCK"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (displayStock) {
                            StockStatus.IN_STOCK -> Color(0xFF30D158)
                            StockStatus.LOW_STOCK -> Color(0xFFFFD60A)
                            StockStatus.OUT_OF_STOCK -> Color(0xFFFF453A)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Product Image + Price Box
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color.White)
                        .padding(4.dp)
                ) {
                    AsyncImage(
                        model = displayImage,
                        contentDescription = displayTitle,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "$displayBrand • ${card.department}",
                        fontSize = 11.sp,
                        color = Color(0xFF8E8E93)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = displayPrice,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                        if (card.unitPrice.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(${card.unitPrice})",
                                fontSize = 11.sp,
                                color = Color(0xFF8E8E93)
                            )
                        }
                    }
                }
            }

            // User preference note pill
            if (card.userPreferenceNote != null && activeAlt == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1B2236))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = Color(0xFFFF2D55),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = card.userPreferenceNote,
                        fontSize = 10.sp,
                        color = Color(0xFF93C5FD),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Smart Substitute Selector (If Out of Stock or Alternatives Available)
            if (card.alternatives.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isOutOfStock) "⚠️ OUT OF STOCK • TAP ALTERNATIVE TO SWAP:" else "AVAILABLE IN-STOCK ALTERNATIVES:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isOutOfStock) Color(0xFFFF9F0A) else Color(0xFF64D2FF),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                ) {
                    card.alternatives.forEach { alt ->
                        val isAltActive = card.activeAlternativeId == alt.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isAltActive) Color(0xFF0D2818) else Color(0xFF1E2030))
                                .border(
                                    1.dp,
                                    if (isAltActive) Color(0xFF30D158) else Color(0x33FFFFFF),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onAlternativeSelected(alt.id) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isAltActive) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF30D158),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(alt.storeOrigin.brandColorHex))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = alt.storeOrigin.storeBadge,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color(alt.storeOrigin.accentColorHex)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = alt.title,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isAltActive) Color(0xFF30D158) else Color.White,
                                        maxLines = 1
                                    )
                                }
                                Text(
                                    text = "${alt.formattedPrice} • ${alt.reasonForMatch}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF8E8E93)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stepper and Remove
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Quantity Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1E2030))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { if (card.quantity > 1) onQuantityChanged(card.quantity - 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Text(
                        text = "${card.quantity}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    IconButton(
                        onClick = { onQuantityChanged(card.quantity + 1) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Skip / Include Toggle
                TextButton(
                    onClick = { onItemToggled(!card.isSelected) }
                ) {
                    Text(
                        text = if (card.isSelected) "✕ Skip Item" else "+ Include",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (card.isSelected) Color(0xFFFF453A) else Color(0xFF30D158)
                    )
                }
            }
        }
    }
}
