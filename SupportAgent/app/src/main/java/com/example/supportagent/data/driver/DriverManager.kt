package com.example.supportagent.data.driver

import android.content.Context
import android.content.SharedPreferences
import com.example.supportagent.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DriverManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("provi_driver_prefs", Context.MODE_PRIVATE)

    private val _driverFlow = MutableStateFlow(loadDriverProfile())
    val driverFlow: StateFlow<DriverProfile> = _driverFlow.asStateFlow()

    private val _batchesFlow = MutableStateFlow(getInitialBatches())
    val batchesFlow: StateFlow<List<DeliveryBatch>> = _batchesFlow.asStateFlow()

    private fun loadDriverProfile(): DriverProfile {
        val name = prefs.getString("driver_name", "Alex Rivera") ?: "Alex Rivera"
        val phone = prefs.getString("driver_phone", "+1 (555) 234-5678") ?: "+1 (555) 234-5678"
        val license = prefs.getString("driver_license", "FL-R9204-8172") ?: "FL-R9204-8172"
        val vehicle = prefs.getString("driver_vehicle", "Toyota RAV4 (2022) • Silver") ?: "Toyota RAV4 (2022)"
        val isOnline = prefs.getBoolean("driver_is_online", true)

        return DriverProfile(
            fullName = name,
            phone = phone,
            driversLicenseNumber = license,
            vehicleModel = vehicle,
            isOnline = isOnline,
            verificationStatus = VerificationStatus.VERIFIED,
            subscriptionTier = DriverSubscriptionTier.ZERO_COMMISSION_PRO
        )
    }

    private fun getInitialBatches(): List<DeliveryBatch> {
        return listOf(
            DeliveryBatch(
                orderNumber = "#BATCH-8492",
                storeName = "Walmart Supercenter",
                storeAddress = "22100 S State Rd 7, Boca Raton",
                deliveryAddress = "1420 Ocean Ave, Apt 4B, Boca Raton",
                distanceMiles = 2.1,
                itemCount = 6,
                basePay = 14.50,
                tipAmount = 7.50,
                itemsSummary = listOf("Great Value Milk", "Vital Farms Eggs", "Honeycrisp Apples", "Dawn Soap")
            ),
            DeliveryBatch(
                orderNumber = "#BATCH-5190",
                storeName = "ALDI (via Instacart Network)",
                storeAddress = "1250 W Palmetto Park Rd, Boca Raton",
                deliveryAddress = "950 Glades Rd, Boca Raton",
                distanceMiles = 3.4,
                itemCount = 4,
                basePay = 11.00,
                tipAmount = 9.00,
                itemsSummary = listOf("Simply Nature Milk", "Organic Brown Eggs", "Organic Bananas")
            )
        )
    }

    fun toggleOnlineStatus() {
        val current = _driverFlow.value
        val newStatus = !current.isOnline
        prefs.edit().putBoolean("driver_is_online", newStatus).apply()
        _driverFlow.update { it.copy(isOnline = newStatus) }
    }

    fun acceptBatch(batchId: String) {
        _batchesFlow.update { list ->
            list.map { if (it.id == batchId) it.copy(status = BatchStatus.SHOPPING) else it }
        }
    }

    fun toggleBatchItemFound(batchId: String, item: String) {
        _batchesFlow.update { list ->
            list.map { batch ->
                if (batch.id == batchId) {
                    val newFound = if (batch.foundItems.contains(item)) {
                        batch.foundItems - item
                    } else {
                        batch.foundItems + item
                    }
                    batch.copy(foundItems = newFound)
                } else batch
            }
        }
    }

    fun advanceBatchStatus(batchId: String) {
        _batchesFlow.update { list ->
            list.map { batch ->
                if (batch.id == batchId) {
                    val nextStatus = when (batch.status) {
                        BatchStatus.AVAILABLE -> BatchStatus.ACCEPTED
                        BatchStatus.ACCEPTED -> BatchStatus.SHOPPING
                        BatchStatus.SHOPPING -> BatchStatus.EN_ROUTE
                        BatchStatus.EN_ROUTE -> BatchStatus.DELIVERED
                        BatchStatus.DELIVERED -> BatchStatus.DELIVERED
                    }
                    batch.copy(status = nextStatus)
                } else batch
            }
        }
    }

    fun dispatchBatchFromOrder(orderNumber: String, storeName: String, deliveryAddress: String, items: List<CartItem>, tip: Double) {
        val newBatch = DeliveryBatch(
            orderNumber = orderNumber,
            storeName = storeName,
            storeAddress = "22100 S State Rd 7, Boca Raton",
            deliveryAddress = deliveryAddress,
            distanceMiles = 2.4,
            itemCount = items.sumOf { it.quantity }.coerceAtLeast(1),
            basePay = 9.50 + (items.size * 0.75),
            tipAmount = tip,
            status = BatchStatus.AVAILABLE,
            itemsSummary = if (items.isNotEmpty()) items.map { "${it.quantity}x ${it.title}" } else listOf("Milk", "Eggs", "Bread")
        )
        _batchesFlow.update { listOf(newBatch) + it }
    }

    fun cashOutEarnings() {
        _driverFlow.update {
            it.copy(todayEarnings = 0.0)
        }
    }

    fun completeBatch(batchId: String) {
        val batch = _batchesFlow.value.find { it.id == batchId }
        val earned = batch?.totalEarnings ?: 20.0

        _batchesFlow.update { list ->
            list.filterNot { it.id == batchId }
        }

        _driverFlow.update {
            it.copy(
                todayEarnings = it.todayEarnings + earned,
                weeklyEarnings = it.weeklyEarnings + earned,
                completedTrips = it.completedTrips + 1
            )
        }
    }

    fun updateProfile(profile: DriverProfile) {
        updateProfile(profile.fullName, profile.driversLicenseNumber, profile.vehicleModel)
    }

    fun updateProfile(name: String, license: String, vehicle: String) {
        prefs.edit()
            .putString("driver_name", name)
            .putString("driver_license", license)
            .putString("driver_vehicle", vehicle)
            .apply()

        _driverFlow.update {
            it.copy(
                fullName = name,
                driversLicenseNumber = license,
                vehicleModel = vehicle,
                verificationStatus = VerificationStatus.VERIFIED
            )
        }
    }
}
