package com.example.supportagent.network.catalog

import android.util.Log
import com.example.supportagent.data.stores.StoreRepository
import com.example.supportagent.model.GroceryStore
import com.example.supportagent.model.StoreOrigin
import com.example.supportagent.model.StoreProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object LiveGroceryCatalogService {

    private const val TAG = "LiveGroceryApi"
    private val memoryCache = ConcurrentHashMap<String, List<StoreProduct>>()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(6, TimeUnit.SECONDS)
        .build()

    suspend fun searchLiveProducts(
        query: String,
        targetStore: GroceryStore? = null,
        limit: Int = 24
    ): List<StoreProduct> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim().lowercase(Locale.US)
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val cacheKey = "${targetStore?.id ?: "global"}:$cleanQuery:$limit"
        memoryCache[cacheKey]?.let { cached ->
            return@withContext cached
        }

        val remoteResults = mutableListOf<StoreProduct>()

        try {
            val encodedTerm = URLEncoder.encode(cleanQuery, StandardCharsets.UTF_8.toString())
            val url = "https://us.openfoodfacts.org/cgi/search.pl?search_terms=$encodedTerm&search_simple=1&action=process&json=1&page_size=$limit"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ProviGroceryApp/1.0 (Android; contact@proviapp.com)")
                .header("Accept", "application/json")
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val json = JSONObject(body)
                    val productsArray = json.optJSONArray("products")

                    if (productsArray != null && productsArray.length() > 0) {
                        val availableStores = StoreRepository.stores

                        for (i in 0 until productsArray.length()) {
                            val pObj = productsArray.optJSONObject(i) ?: continue

                            val name = pObj.optString("product_name").ifBlank {
                                pObj.optString("product_name_en")
                            }.trim()

                            if (name.isBlank() || name.length < 2) continue

                            val rawBrand = pObj.optString("brands").ifBlank {
                                pObj.optString("brand_owner")
                            }.split(",").firstOrNull()?.trim() ?: "Grocery"

                            val brand = if (rawBrand.length > 22) rawBrand.take(22) else rawBrand

                            val upc = pObj.optString("code")
                            val unitSize = pObj.optString("quantity").ifBlank {
                                pObj.optString("serving_size").ifBlank { "Standard Pack" }
                            }

                            // High-res photo selection
                            val selectedImages = pObj.optJSONObject("selected_images")?.optJSONObject("front")?.optJSONObject("display")
                            val imageFromSelected = selectedImages?.optString("en") ?: ""
                            val imageUrl = imageFromSelected.ifBlank {
                                pObj.optString("image_front_url").ifBlank {
                                    pObj.optString("image_url").ifBlank {
                                        ProductImageResolver.resolveImage(cleanQuery, name)
                                    }
                                }
                            }

                            // Store attribution
                            val assignedStore = targetStore ?: availableStores[i % availableStores.size]

                            // Department / Category mapping
                            val categoriesTags = pObj.optJSONArray("categories_tags")?.toString()?.lowercase(Locale.US) ?: ""
                            val category = when {
                                categoriesTags.contains("fruit") || categoriesTags.contains("vegetable") || categoriesTags.contains("produce") || cleanQuery.contains("apple") || cleanQuery.contains("banana") -> "Produce"
                                categoriesTags.contains("dairy") || categoriesTags.contains("milk") || categoriesTags.contains("cheese") || categoriesTags.contains("yogurt") || cleanQuery.contains("milk") || cleanQuery.contains("egg") -> "Dairy & Eggs"
                                categoriesTags.contains("bread") || categoriesTags.contains("bakery") || categoriesTags.contains("biscuit") || cleanQuery.contains("bread") -> "Bakery"
                                categoriesTags.contains("meat") || categoriesTags.contains("poultry") || categoriesTags.contains("seafood") || cleanQuery.contains("beef") || cleanQuery.contains("chicken") -> "Meat"
                                categoriesTags.contains("snack") || categoriesTags.contains("chip") || categoriesTags.contains("cookie") || categoriesTags.contains("cereal") || cleanQuery.contains("chip") -> "Snacks"
                                categoriesTags.contains("beverage") || categoriesTags.contains("drink") || categoriesTags.contains("juice") || categoriesTags.contains("soda") || cleanQuery.contains("drink") -> "Drinks"
                                else -> "Pantry"
                            }

                            // Deterministic shelf price calculation
                            val hash = kotlin.math.abs(name.hashCode() xor upc.hashCode())
                            val basePrice = when (category) {
                                "Produce" -> 1.48 + (hash % 350) / 100.0
                                "Dairy & Eggs" -> 2.48 + (hash % 420) / 100.0
                                "Meat" -> 6.48 + (hash % 700) / 100.0
                                "Bakery" -> 2.88 + (hash % 300) / 100.0
                                "Snacks" -> 3.28 + (hash % 300) / 100.0
                                else -> 2.98 + (hash % 450) / 100.0
                            }

                            val formattedPrice = String.format(Locale.US, "$%.2f", basePrice)

                            remoteResults.add(
                                StoreProduct(
                                    id = "live_${upc.ifBlank { UUID.randomUUID().toString() }}",
                                    storeId = assignedStore.id,
                                    storeName = assignedStore.name,
                                    origin = assignedStore.origin,
                                    name = name,
                                    brand = brand,
                                    price = String.format(Locale.US, "%.2f", basePrice).toDouble(),
                                    formattedPrice = formattedPrice,
                                    unitSize = unitSize,
                                    category = category,
                                    imageUrl = imageUrl,
                                    upc = upc,
                                    isPopular = i < 3
                                )
                            )
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("LiveGroceryApi: Live search request failed for $cleanQuery: ${e.message}")
        }

        // Combine with local catalog matches for zero-downtime reliability
        val localMatches = StoreRepository.stores.flatMap { store ->
            if (targetStore == null || store.id == targetStore.id) {
                StoreRepository.getProductsForStore(store.id).filter {
                    it.name.contains(cleanQuery, ignoreCase = true) ||
                    it.brand.contains(cleanQuery, ignoreCase = true) ||
                    it.category.contains(cleanQuery, ignoreCase = true)
                }
            } else emptyList()
        }

        val combined = (remoteResults + localMatches).distinctBy { it.name.lowercase(Locale.US) }
        memoryCache[cacheKey] = combined
        combined
    }
}
