package com.example.supportagent.model

import java.util.UUID

data class CartItem(
    val id: String = UUID.randomUUID().toString(),
    val store: String = "Walmart Supercenter",
    val storeOrigin: StoreOrigin = StoreOrigin.WALMART,
    val title: String,
    val brand: String = "",
    val price: Double,
    val formattedPrice: String,
    val unitPrice: String = "",
    val quantity: Int = 1,
    val imageUrl: String = "",
    val deliveryAddress: String = "",
    val addedTimestamp: Long = System.currentTimeMillis()
) {
    val totalPrice: Double
        get() = price * quantity

    val formattedTotalPrice: String
        get() = String.format("$%.2f", totalPrice)
}

data class UserStoreCart(
    val id: String = UUID.randomUUID().toString(),
    val storeName: String = "Walmart Supercenter",
    val deliveryAddress: String = "1420 Ocean Ave, Apt 4B, Boca Raton, FL 33431",
    val items: List<CartItem> = emptyList(),
    val estimatedTaxRate: Double = 0.07,
    val deliveryFee: Double = 0.0
) {
    val subtotal: Double
        get() = items.sumOf { it.totalPrice }

    val formattedSubtotal: String
        get() = String.format("$%.2f", subtotal)

    val estimatedTax: Double
        get() = subtotal * estimatedTaxRate

    val formattedEstimatedTax: String
        get() = String.format("$%.2f", estimatedTax)

    val finalTotal: Double
        get() = subtotal + estimatedTax + deliveryFee

    val formattedFinalTotal: String
        get() = String.format("$%.2f", finalTotal)

    val totalItemCount: Int
        get() = items.sumOf { it.quantity }

    val walmartItems: List<CartItem>
        get() = items.filter { it.storeOrigin == StoreOrigin.WALMART }

    val instacartItems: List<CartItem>
        get() = items.filter { it.storeOrigin != StoreOrigin.WALMART }
}
