package com.example.supportagent.data.cart

import android.content.Context
import android.content.SharedPreferences
import com.example.supportagent.model.CartItem
import com.example.supportagent.model.GroceryProductCard
import com.example.supportagent.model.StoreOrigin
import com.example.supportagent.model.UserStoreCart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

class CartManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_store_cart_prefs", Context.MODE_PRIVATE)

    private val _cartFlow = MutableStateFlow(loadCartFromPrefs())
    val cartFlow: StateFlow<UserStoreCart> = _cartFlow.asStateFlow()

    companion object {
        private const val KEY_CART_ITEMS_JSON = "pref_cart_items_json"
        private const val KEY_STORE_NAME = "pref_store_name"
        private const val KEY_DELIVERY_ADDRESS = "pref_delivery_address"
    }

    private fun loadCartFromPrefs(): UserStoreCart {
        val store = prefs.getString(KEY_STORE_NAME, "Multi-Store Smart Cart") ?: "Multi-Store Smart Cart"
        val address = prefs.getString(KEY_DELIVERY_ADDRESS, "1420 Ocean Ave, Apt 4B, Boca Raton, FL 33431")
            ?: "1420 Ocean Ave, Apt 4B, Boca Raton, FL 33431"
        val itemsJson = prefs.getString(KEY_CART_ITEMS_JSON, null)
        val items = mutableListOf<CartItem>()

        if (!itemsJson.isNullOrBlank()) {
            try {
                val array = JSONArray(itemsJson)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val originStr = obj.optString("storeOrigin", StoreOrigin.WALMART.name)
                    val storeOrigin = try {
                        StoreOrigin.valueOf(originStr)
                    } catch (e: Exception) {
                        StoreOrigin.WALMART
                    }

                    items.add(
                        CartItem(
                            id = obj.getString("id"),
                            store = obj.optString("store", storeOrigin.displayName),
                            storeOrigin = storeOrigin,
                            title = obj.getString("title"),
                            brand = obj.optString("brand", ""),
                            price = obj.getDouble("price"),
                            formattedPrice = obj.optString("formattedPrice", "$0.00"),
                            unitPrice = obj.optString("unitPrice", ""),
                            quantity = obj.getInt("quantity"),
                            imageUrl = obj.optString("imageUrl", ""),
                            deliveryAddress = obj.optString("deliveryAddress", address),
                            addedTimestamp = obj.optLong("addedTimestamp", System.currentTimeMillis())
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return UserStoreCart(
            storeName = store,
            deliveryAddress = address,
            items = items
        )
    }

    private fun persistCart(cart: UserStoreCart) {
        val array = JSONArray()
        for (item in cart.items) {
            val obj = JSONObject().apply {
                put("id", item.id)
                put("store", item.store)
                put("storeOrigin", item.storeOrigin.name)
                put("title", item.title)
                put("brand", item.brand)
                put("price", item.price)
                put("formattedPrice", item.formattedPrice)
                put("unitPrice", item.unitPrice)
                put("quantity", item.quantity)
                put("imageUrl", item.imageUrl)
                put("deliveryAddress", item.deliveryAddress)
                put("addedTimestamp", item.addedTimestamp)
            }
            array.put(obj)
        }

        prefs.edit()
            .putString(KEY_CART_ITEMS_JSON, array.toString())
            .putString(KEY_STORE_NAME, cart.storeName)
            .putString(KEY_DELIVERY_ADDRESS, cart.deliveryAddress)
            .apply()

        _cartFlow.value = cart
    }

    fun addItem(item: CartItem) {
        val current = _cartFlow.value
        val updatedItems = current.items.toMutableList()
        val existingIndex = updatedItems.indexOfFirst { it.title.equals(item.title, ignoreCase = true) }
        if (existingIndex >= 0) {
            val existing = updatedItems[existingIndex]
            updatedItems[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
        } else {
            updatedItems.add(item)
        }
        persistCart(current.copy(items = updatedItems))
    }

    fun addDeckCards(
        cards: List<GroceryProductCard>,
        storeName: String = "Multi-Store Smart Cart",
        deliveryAddress: String
    ) {
        val current = _cartFlow.value
        val updatedItems = current.items.toMutableList()

        for (card in cards) {
            val activeAlt = card.alternatives.find { it.id == card.activeAlternativeId }
            val itemTitle = activeAlt?.title ?: card.matchedTitle
            val itemBrand = activeAlt?.brand ?: card.brand
            val itemPrice = activeAlt?.price ?: card.price
            val formattedPrice = activeAlt?.formattedPrice ?: card.formattedPrice
            val itemImage = activeAlt?.imageUrl ?: card.imageUrl
            val itemStoreOrigin = activeAlt?.storeOrigin ?: card.storeOrigin

            val existingIndex = updatedItems.indexOfFirst { it.title.equals(itemTitle, ignoreCase = true) }
            if (existingIndex >= 0) {
                val existing = updatedItems[existingIndex]
                updatedItems[existingIndex] = existing.copy(quantity = existing.quantity + card.quantity)
            } else {
                updatedItems.add(
                    CartItem(
                        store = itemStoreOrigin.displayName,
                        storeOrigin = itemStoreOrigin,
                        title = itemTitle,
                        brand = itemBrand,
                        price = itemPrice,
                        formattedPrice = formattedPrice,
                        unitPrice = card.unitPrice,
                        quantity = card.quantity,
                        imageUrl = itemImage,
                        deliveryAddress = deliveryAddress
                    )
                )
            }
        }

        persistCart(current.copy(storeName = storeName, deliveryAddress = deliveryAddress, items = updatedItems))
    }

    fun updateQuantity(itemId: String, newQty: Int) {
        val current = _cartFlow.value
        val updated = if (newQty <= 0) {
            current.items.filterNot { it.id == itemId }
        } else {
            current.items.map { if (it.id == itemId) it.copy(quantity = newQty) else it }
        }
        persistCart(current.copy(items = updated))
    }

    fun removeItem(itemId: String) {
        val current = _cartFlow.value
        val updated = current.items.filterNot { it.id == itemId }
        persistCart(current.copy(items = updated))
    }

    fun clearCart() {
        val current = _cartFlow.value
        persistCart(current.copy(items = emptyList()))
    }

    fun updateDeliveryAddress(newAddress: String) {
        val current = _cartFlow.value
        persistCart(current.copy(deliveryAddress = newAddress))
    }
}
