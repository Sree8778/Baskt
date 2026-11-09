package com.example.supportagent.model

import java.util.UUID

enum class OrderProgressStage(val label: String, val subtitle: String, val stepNumber: Int) {
    ORDER_CONFIRMED("Order Placed", "Store received order", 1),
    DRIVER_ASSIGNED("Driver Assigned", "Alex Rivera accepted delivery", 2),
    SHOPPING("Shopper in Store", "Alex is picking shelf-price items", 3),
    OUT_FOR_DELIVERY("Out for Delivery", "Driver is en route to your door", 4),
    DELIVERED("Delivered", "Delivered safely with photo confirmation", 5)
}

data class ActiveOrder(
    val orderId: String = "#PV-" + (1000..9999).random(),
    val storeName: String = "Walmart Supercenter",
    val storeOrigin: StoreOrigin = StoreOrigin.WALMART,
    val items: List<CartItem> = emptyList(),
    val progress: OrderProgressStage = OrderProgressStage.OUT_FOR_DELIVERY,
    val driverName: String = "Alex Rivera",
    val driverPhone: String = "+1 (555) 234-5678",
    val driverVehicle: String = "Toyota RAV4 (Silver)",
    val driverRating: Double = 4.98,
    val estimatedArrivalMinutes: Int = 18,
    val deliveryAddress: String = "1420 Ocean Ave, Apt 4B, Boca Raton, FL",
    val subtotal: Double = 32.40,
    val deliveryFee: Double = 4.99,
    val driverTip: Double = 5.00,
    val totalPaid: Double = 42.39,
    val competitorInstacartMarkup: Double = 12.80, // Customer saved $12.80 vs 25% markup
    val placedTimestamp: Long = System.currentTimeMillis()
) {
    val formattedSubtotal: String get() = String.format("$%.2f", subtotal)
    val formattedDeliveryFee: String get() = String.format("$%.2f", deliveryFee)
    val formattedDriverTip: String get() = String.format("$%.2f", driverTip)
    val formattedTotalPaid: String get() = String.format("$%.2f", totalPaid)
    val formattedSavings: String get() = String.format("$%.2f", competitorInstacartMarkup)
}
