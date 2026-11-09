package com.example.supportagent.model

import java.util.UUID

enum class AppRole {
    CUSTOMER,
    DRIVER
}

enum class VerificationStatus {
    NOT_SUBMITTED,
    PENDING_REVIEW,
    VERIFIED,
    REJECTED
}

enum class DriverSubscriptionTier(val title: String, val pricePerMonth: String, val commissionRate: String) {
    ZERO_COMMISSION_PRO("Zero-Commission Pro", "$19.99/mo", "0% Commission (Keep 100%)"),
    PER_ORDER_STARTER("Starter Flex", "$0/mo", "Flat $1.50/batch")
}

data class DriverProfile(
    val id: String = UUID.randomUUID().toString(),
    val fullName: String = "Alex Rivera",
    val phone: String = "+1 (555) 234-5678",
    val email: String = "alex.driver@example.com",
    val driversLicenseNumber: String = "FL-R9204-8172",
    val vehicleModel: String = "Toyota RAV4 (2022) • Silver",
    val licensePlate: String = "7XYZ89",
    val isOnline: Boolean = true,
    val verificationStatus: VerificationStatus = VerificationStatus.VERIFIED,
    val subscriptionTier: DriverSubscriptionTier = DriverSubscriptionTier.ZERO_COMMISSION_PRO,
    val rating: Double = 4.98,
    val completedTrips: Int = 342,
    val todayEarnings: Double = 84.50,
    val weeklyEarnings: Double = 428.00
) {
    val formattedTodayEarnings: String
        get() = String.format("$%.2f", todayEarnings)

    val formattedWeeklyEarnings: String
        get() = String.format("$%.2f", weeklyEarnings)
}

enum class BatchStatus {
    AVAILABLE,
    ACCEPTED,
    SHOPPING,
    EN_ROUTE,
    DELIVERED
}

data class DeliveryBatch(
    val id: String = UUID.randomUUID().toString(),
    val orderNumber: String = "#BATCH-${(1000..9999).random()}",
    val storeName: String = "Walmart Supercenter",
    val storeAddress: String = "22100 S State Rd 7, Boca Raton",
    val deliveryAddress: String = "1420 Ocean Ave, Apt 4B, Boca Raton",
    val distanceMiles: Double = 2.4,
    val itemCount: Int = 5,
    val basePay: Double = 12.50,
    val tipAmount: Double = 8.00,
    val status: BatchStatus = BatchStatus.AVAILABLE,
    val itemsSummary: List<String> = listOf("Whole Vitamin D Milk", "Vital Farms Eggs", "Honeycrisp Apples", "Dawn Platinum Soap"),
    val foundItems: Set<String> = emptySet()
) {
    val totalEarnings: Double
        get() = basePay + tipAmount

    val formattedTotalEarnings: String
        get() = String.format("$%.2f", totalEarnings)

    val formattedDistance: String
        get() = String.format("%.1f mi", distanceMiles)
}
