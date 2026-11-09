package com.example.supportagent.network.catalog

import com.example.supportagent.data.profile.UserGroceryProfile
import com.example.supportagent.model.GroceryCartDeck
import com.example.supportagent.model.GroceryProductCard
import com.example.supportagent.model.ProductAlternative
import com.example.supportagent.model.StockStatus
import com.example.supportagent.model.StoreOrigin
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

object MultiStoreCatalogAggregator {

    private val walmartProvider = WalmartCatalogProvider()
    private val instacartProvider = InstacartCatalogProvider()

    suspend fun aggregateForQuery(
        rawQuery: String,
        profile: UserGroceryProfile,
        geminiApiKey: String = ""
    ): GroceryCartDeck = coroutineScope {
        // Global item extraction powered by Gemini NLP + Smart fallback
        val requestedItems = ItemQueryExtractor.extractItems(rawQuery, geminiApiKey)

        // Run both Walmart and Instacart providers concurrently across live endpoints
        val walmartDeferred = async { walmartProvider.searchProducts(requestedItems, profile) }
        val instacartDeferred = async { instacartProvider.searchProducts(requestedItems, profile) }

        val walmartMap = walmartDeferred.await()
        val instacartMap = instacartDeferred.await()

        val consolidatedCards = requestedItems.map { itemKey ->
            val wmtCard = walmartMap[itemKey]
            val instacartCard = instacartMap[itemKey]

            if (wmtCard != null && instacartCard != null) {
                // Build cross-store alternative from Instacart into the Walmart card
                val savings = kotlin.math.max(0.0, wmtCard.price - instacartCard.price)
                val savingsStr = if (savings > 0.05) " • Save $${String.format("%.2f", savings)}" else ""

                val crossStoreAlt = ProductAlternative(
                    storeOrigin = instacartCard.storeOrigin,
                    title = instacartCard.matchedTitle,
                    brand = instacartCard.brand,
                    price = instacartCard.price,
                    formattedPrice = instacartCard.formattedPrice,
                    reasonForMatch = "Available at ${instacartCard.storeOrigin.displayName}$savingsStr",
                    imageUrl = instacartCard.imageUrl,
                    stockStatus = instacartCard.stockStatus
                )

                val combinedAlternatives = (wmtCard.alternatives + crossStoreAlt + instacartCard.alternatives).distinctBy { it.title }

                wmtCard.copy(alternatives = combinedAlternatives)
            } else {
                wmtCard ?: instacartCard ?: GroceryProductCard(
                    storeOrigin = StoreOrigin.WALMART,
                    queryItem = itemKey.replaceFirstChar { it.uppercase() },
                    matchedTitle = itemKey,
                    brand = "Everyday Grocery",
                    price = 3.99,
                    formattedPrice = "$3.99",
                    imageUrl = "https://i5.walmartimages.com/seo/grocery-item.jpg"
                )
            }
        }

        GroceryCartDeck(
            storeName = "Multi-Store Smart Comparison",
            deliveryAddress = profile.fullFormattedAddress,
            cards = consolidatedCards
        )
    }
}
