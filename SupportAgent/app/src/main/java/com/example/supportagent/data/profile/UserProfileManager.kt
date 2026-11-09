package com.example.supportagent.data.profile

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONObject

data class UserGroceryProfile(
    val addressLine: String = "1420 Ocean Ave, Apt 4B",
    val city: String = "Boca Raton",
    val state: String = "FL",
    val zipCode: String = "33431",
    val dietaryRestrictions: String = "Organic produce preferred; Whole milk only",
    val brandPreferences: Map<String, String> = mapOf(
        "milk" to "Great Value Whole Vitamin D Milk, 1 Gallon",
        "eggs" to "Vital Farms Pasture-Raised Grade A Large Eggs, 12ct",
        "bread" to "Nature's Own Honey Wheat Bread, 20 oz",
        "apples" to "Fresh Honeycrisp Apples, 3 lb bag",
        "bananas" to "Organic Bananas, bunch",
        "chicken" to "Perdue Fresh Boneless Skinless Chicken Breasts"
    )
) {
    val fullFormattedAddress: String
        get() = "$addressLine, $city, $state $zipCode"
}

class UserProfileManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("user_grocery_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ADDRESS_LINE = "pref_address_line"
        private const val KEY_CITY = "pref_city"
        private const val KEY_STATE = "pref_state"
        private const val KEY_ZIP = "pref_zip"
        private const val KEY_DIETARY = "pref_dietary"
        private const val KEY_BRAND_PREFS_JSON = "pref_brands_json"
    }

    fun getProfile(): UserGroceryProfile {
        val addressLine = prefs.getString(KEY_ADDRESS_LINE, "1420 Ocean Ave, Apt 4B") ?: "1420 Ocean Ave, Apt 4B"
        val city = prefs.getString(KEY_CITY, "Boca Raton") ?: "Boca Raton"
        val state = prefs.getString(KEY_STATE, "FL") ?: "FL"
        val zip = prefs.getString(KEY_ZIP, "33431") ?: "33431"
        val dietary = prefs.getString(KEY_DIETARY, "Organic produce preferred; Whole milk only") ?: ""

        val brandJsonStr = prefs.getString(KEY_BRAND_PREFS_JSON, null)
        val brandMap = mutableMapOf<String, String>()

        if (!brandJsonStr.isNullOrBlank()) {
            try {
                val json = JSONObject(brandJsonStr)
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    brandMap[k] = json.getString(k)
                }
            } catch (e: Exception) {
                // fallback to defaults
            }
        }

        if (brandMap.isEmpty()) {
            brandMap.putAll(
                mapOf(
                    "milk" to "Great Value Whole Vitamin D Milk, 1 Gallon",
                    "eggs" to "Vital Farms Pasture-Raised Grade A Large Eggs, 12ct",
                    "bread" to "Nature's Own Honey Wheat Bread, 20 oz",
                    "apples" to "Fresh Honeycrisp Apples, 3 lb bag"
                )
            )
        }

        return UserGroceryProfile(
            addressLine = addressLine,
            city = city,
            state = state,
            zipCode = zip,
            dietaryRestrictions = dietary,
            brandPreferences = brandMap
        )
    }

    fun saveAddress(addressLine: String, city: String, state: String, zipCode: String) {
        prefs.edit()
            .putString(KEY_ADDRESS_LINE, addressLine.trim())
            .putString(KEY_CITY, city.trim())
            .putString(KEY_STATE, state.trim())
            .putString(KEY_ZIP, zipCode.trim())
            .apply()
    }

    fun saveBrandPreference(itemCategory: String, preferredBrandTitle: String) {
        val profile = getProfile()
        val updatedMap = profile.brandPreferences.toMutableMap()
        updatedMap[itemCategory.lowercase().trim()] = preferredBrandTitle.trim()

        val json = JSONObject(updatedMap as Map<*, *>)
        prefs.edit().putString(KEY_BRAND_PREFS_JSON, json.toString()).apply()
    }
}
