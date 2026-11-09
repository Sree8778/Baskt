package com.example.supportagent.model

import java.util.UUID

data class GroceryStore(
    val id: String,
    val name: String,
    val chain: String,
    val origin: StoreOrigin,
    val logoEmoji: String,
    val badge: String,
    val rating: Double,
    val reviewCount: String,
    val deliveryEta: String,
    val distanceMiles: String,
    val deliveryFee: String = "$4.99 Flat (100% to Driver)",
    val bannerColor: Long = 0xFF0071DC,
    val categories: List<String>,
    val highlights: String = "Exact Shelf Price • $0 Markup"
)

data class StoreProduct(
    val id: String = UUID.randomUUID().toString(),
    val storeId: String,
    val storeName: String,
    val origin: StoreOrigin,
    val name: String,
    val brand: String,
    val price: Double,
    val formattedPrice: String,
    val unitSize: String,
    val category: String,
    val imageUrl: String,
    val upc: String = "",
    val isPopular: Boolean = false,
    val discountTag: String? = null
)
