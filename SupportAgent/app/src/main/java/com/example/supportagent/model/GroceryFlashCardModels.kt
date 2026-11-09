package com.example.supportagent.model

import java.util.UUID

enum class StoreOrigin(
    val displayName: String,
    val storeBadge: String,
    val brandColorHex: Long,
    val accentColorHex: Long,
    val platformType: String,
    val estimatedDeliveryTime: String
) {
    WALMART(
        displayName = "Walmart Supercenter",
        storeBadge = "WALMART",
        brandColorHex = 0xFF0071DC,
        accentColorHex = 0xFFFFC220,
        platformType = "Walmart Direct",
        estimatedDeliveryTime = "Today, 1-2 hours"
    ),
    INSTACART_ALDI(
        displayName = "ALDI (via Instacart)",
        storeBadge = "ALDI",
        brandColorHex = 0xFF0A2240,
        accentColorHex = 0xFFFF5A00,
        platformType = "Instacart IDP",
        estimatedDeliveryTime = "Today, 45-60 min"
    ),
    INSTACART_TARGET(
        displayName = "Target (via Instacart)",
        storeBadge = "TARGET",
        brandColorHex = 0xFFCC0000,
        accentColorHex = 0xFFFFFFFF,
        platformType = "Instacart IDP",
        estimatedDeliveryTime = "Today, 1 hour"
    ),
    INSTACART_KROGER(
        displayName = "Kroger (via Instacart)",
        storeBadge = "KROGER",
        brandColorHex = 0xFF004B87,
        accentColorHex = 0xFFFFC82E,
        platformType = "Instacart IDP",
        estimatedDeliveryTime = "Today, 45 min"
    )
}

enum class StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}

data class ProductAlternative(
    val id: String = UUID.randomUUID().toString(),
    val storeOrigin: StoreOrigin = StoreOrigin.WALMART,
    val title: String,
    val brand: String,
    val price: Double,
    val formattedPrice: String,
    val reasonForMatch: String,
    val imageUrl: String,
    val stockStatus: StockStatus = StockStatus.IN_STOCK
)

data class GroceryProductCard(
    val id: String = UUID.randomUUID().toString(),
    val storeOrigin: StoreOrigin = StoreOrigin.WALMART,
    val queryItem: String,
    val matchedTitle: String,
    val brand: String,
    val department: String = "Grocery",
    val price: Double,
    val formattedPrice: String,
    val unitPrice: String = "",
    val imageUrl: String,
    val stockStatus: StockStatus = StockStatus.IN_STOCK,
    val stockNotice: String? = null,
    val quantity: Int = 1,
    val isSelected: Boolean = true,
    val alternatives: List<ProductAlternative> = emptyList(),
    val activeAlternativeId: String? = null,
    val userPreferenceNote: String? = null
) {
    val activeStoreOrigin: StoreOrigin
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.storeOrigin ?: storeOrigin
        }

    val activeTitle: String
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.title ?: matchedTitle
        }

    val activePrice: Double
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.price ?: price
        }

    val formattedActivePrice: String
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.formattedPrice ?: formattedPrice
        }

    val activeImageUrl: String
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.imageUrl ?: imageUrl
        }

    val activeBrand: String
        get() {
            val alt = alternatives.find { it.id == activeAlternativeId }
            return alt?.brand ?: brand
        }
}

data class GroceryCartDeck(
    val id: String = UUID.randomUUID().toString(),
    val storeName: String = "Multi-Store Smart Comparison",
    val deliveryAddress: String = "1420 Ocean Ave, Apt 4B, Boca Raton, FL 33431",
    val cards: List<GroceryProductCard> = emptyList(),
    val isSubmittedToCart: Boolean = false
) {
    val selectedCards: List<GroceryProductCard>
        get() = cards.filter { it.isSelected }

    val totalAmount: Double
        get() = selectedCards.sumOf { it.activePrice * it.quantity }

    val formattedTotal: String
        get() = String.format("$%.2f", totalAmount)

    val walmartTotal: Double
        get() = selectedCards.filter { it.activeStoreOrigin == StoreOrigin.WALMART }
            .sumOf { it.activePrice * it.quantity }

    val instacartTotal: Double
        get() = selectedCards.filter { it.activeStoreOrigin != StoreOrigin.WALMART }
            .sumOf { it.activePrice * it.quantity }

    val allInStock: Boolean
        get() = selectedCards.all { it.stockStatus != StockStatus.OUT_OF_STOCK || it.activeAlternativeId != null }
}
