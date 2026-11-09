package com.example.supportagent.network.catalog

import com.example.supportagent.data.profile.UserGroceryProfile
import com.example.supportagent.model.GroceryProductCard
import com.example.supportagent.model.ProductAlternative
import com.example.supportagent.model.StockStatus
import com.example.supportagent.model.StoreOrigin

interface GroceryCatalogProvider {
    val storeOrigin: StoreOrigin
    suspend fun searchProducts(
        items: List<String>,
        profile: UserGroceryProfile
    ): Map<String, GroceryProductCard>
}
