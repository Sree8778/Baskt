package com.example.supportagent.network

import com.example.supportagent.data.profile.UserGroceryProfile
import com.example.supportagent.model.GroceryCartDeck
import com.example.supportagent.model.GroceryProductCard
import com.example.supportagent.model.ProductAlternative
import com.example.supportagent.model.StockStatus

object WalmartCatalogResolver {

    fun buildDeckForQuery(rawQuery: String, profile: UserGroceryProfile): GroceryCartDeck {
        val queryLower = rawQuery.lowercase()
        val requestedItems = extractItemsFromQuery(queryLower)

        val productCards = requestedItems.map { itemKey ->
            resolveProductForQuery(itemKey, profile)
        }

        return GroceryCartDeck(
            storeName = "Walmart Supercenter",
            deliveryAddress = profile.fullFormattedAddress,
            cards = productCards
        )
    }

    private fun extractItemsFromQuery(query: String): List<String> {
        val items = mutableListOf<String>()

        // Check for common grocery keywords
        if (query.contains("milk")) items.add("milk")
        if (query.contains("egg")) items.add("eggs")
        if (query.contains("bread")) items.add("bread")
        if (query.contains("apple")) items.add("apples")
        if (query.contains("banana")) items.add("bananas")
        if (query.contains("chicken") || query.contains("meat")) items.add("chicken")
        if (query.contains("coffee")) items.add("coffee")
        if (query.contains("cereal")) items.add("cereal")
        if (query.contains("paper towel") || query.contains("bounty")) items.add("paper towels")

        // If no keyword was detected, parse comma-separated or words
        if (items.isEmpty()) {
            val cleaned = query.replace("put", "")
                .replace("in my walmart cart", "")
                .replace("in cart", "")
                .replace("restock", "")
                .replace("groceries", "")
                .replace("and", ",")
                .trim()

            val split = cleaned.split(",").map { it.trim() }.filter { it.length > 2 }
            if (split.isNotEmpty()) {
                items.addAll(split)
            } else {
                items.addAll(listOf("milk", "eggs", "apples", "bread"))
            }
        }

        return items
    }

    private fun resolveProductForQuery(item: String, profile: UserGroceryProfile): GroceryProductCard {
        val lower = item.lowercase()

        return when {
            lower.contains("milk") -> {
                val pref = profile.brandPreferences["milk"] ?: "Great Value Whole Vitamin D Milk, 1 Gallon"
                GroceryProductCard(
                    queryItem = "Milk",
                    matchedTitle = pref,
                    brand = "Great Value",
                    department = "Dairy & Chilled",
                    price = 3.18,
                    formattedPrice = "$3.18",
                    unitPrice = "2.5¢/fl oz",
                    imageUrl = "https://i5.walmartimages.com/seo/Great-Value-Whole-Vitamin-D-Milk-Gallon-128-fl-oz_4ac5a0fb-d446-4a47-a84a-fa07f87f9d86.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock at local store",
                    userPreferenceNote = "Matches your previous brand preference",
                    alternatives = listOf(
                        ProductAlternative(
                            title = "Horizon Organic Whole High Vitamin D Milk, 1 Gallon",
                            brand = "Horizon Organic",
                            price = 5.98,
                            formattedPrice = "$5.98",
                            reasonForMatch = "Organic alternative • In Stock",
                            imageUrl = "https://i5.walmartimages.com/seo/Horizon-Organic-Whole-High-Vitamin-D-Milk-Gallon-128-fl-oz_83cefaee-a760-4966-bd95-2c9ff506a7df.jpeg"
                        ),
                        ProductAlternative(
                            title = "Great Value 2% Reduced Fat Milk, 1 Gallon",
                            brand = "Great Value",
                            price = 3.18,
                            formattedPrice = "$3.18",
                            reasonForMatch = "Lower calorie • In Stock",
                            imageUrl = "https://i5.walmartimages.com/seo/Great-Value-2-Reduced-Fat-Milk-Gallon-128-fl-oz_31cb21b0-9cfb-4a57-b089-130ab6aa7005.jpeg"
                        )
                    )
                )
            }
            lower.contains("egg") -> {
                val pref = profile.brandPreferences["eggs"] ?: "Vital Farms Pasture-Raised Grade A Large Eggs, 12ct"
                GroceryProductCard(
                    queryItem = "Eggs",
                    matchedTitle = pref,
                    brand = "Vital Farms",
                    department = "Dairy & Eggs",
                    price = 5.98,
                    formattedPrice = "$5.98",
                    unitPrice = "49.8¢/ea",
                    imageUrl = "https://i5.walmartimages.com/seo/Vital-Farms-Pasture-Raised-Grade-A-Large-Eggs-12-Count_484638a1-c632-474c-897e-1283363ceea4.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock (Only 4 left)",
                    userPreferenceNote = "Pasture-raised favorite",
                    alternatives = listOf(
                        ProductAlternative(
                            title = "Great Value Large White Grade A Eggs, 18 Count",
                            brand = "Great Value",
                            price = 3.24,
                            formattedPrice = "$3.24",
                            reasonForMatch = "Budget staple • 18 count in stock",
                            imageUrl = "https://i5.walmartimages.com/seo/Great-Value-Large-White-Grade-A-Eggs-18-Count_c030d9ea-056a-4ae9-a414-b15392cf9968.jpeg"
                        ),
                        ProductAlternative(
                            title = "Eggland's Best Cage Free Grade A Large Eggs, 12 Count",
                            brand = "Eggland's Best",
                            price = 4.18,
                            formattedPrice = "$4.18",
                            reasonForMatch = "Cage-free alternative",
                            imageUrl = "https://i5.walmartimages.com/seo/Eggland-s-Best-Cage-Free-Grade-A-Large-White-Eggs-12-Count_3a1d9536-a36c-4861-a53b-e7b39970868f.jpeg"
                        )
                    )
                )
            }
            lower.contains("apple") -> {
                // Out of stock demonstration so the user immediately gets to test the alternative switcher!
                GroceryProductCard(
                    queryItem = "Apples",
                    matchedTitle = "Fresh Honeycrisp Apples, 3 lb Bag",
                    brand = "Fresh Produce",
                    department = "Fresh Fruit",
                    price = 4.98,
                    formattedPrice = "$4.98",
                    unitPrice = "$1.66/lb",
                    imageUrl = "https://i5.walmartimages.com/seo/Fresh-Honeycrisp-Apples-3-lb-Bag_95cb73b3-1cb0-4fdf-b9fb-1cce8a6e974e.jpeg",
                    stockStatus = StockStatus.OUT_OF_STOCK,
                    stockNotice = "Out of Stock at local store",
                    userPreferenceNote = "Previously ordered Honeycrisp",
                    alternatives = listOf(
                        ProductAlternative(
                            title = "Fresh Organic Gala Apples, 3 lb Bag",
                            brand = "Organic Produce",
                            price = 4.28,
                            formattedPrice = "$4.28",
                            reasonForMatch = "Sweet & crisp • Fresh in stock",
                            imageUrl = "https://i5.walmartimages.com/seo/Fresh-Organic-Gala-Apples-3-lb-Bag_39622d10-86b2-4d51-837b-99d7a22ae7fc.jpeg"
                        ),
                        ProductAlternative(
                            title = "Fresh Fuji Apples, 3 lb Bag",
                            brand = "Fresh Produce",
                            price = 3.98,
                            formattedPrice = "$3.98",
                            reasonForMatch = "Crisp texture • Lower price",
                            imageUrl = "https://i5.walmartimages.com/seo/Fresh-Fuji-Apples-3-lb-Bag_2dfdb709-6799-4623-bcad-bf2477a3d906.jpeg"
                        )
                    )
                )
            }
            lower.contains("bread") -> {
                val pref = profile.brandPreferences["bread"] ?: "Nature's Own Honey Wheat Bread, 20 oz"
                GroceryProductCard(
                    queryItem = "Bread",
                    matchedTitle = pref,
                    brand = "Nature's Own",
                    department = "Bakery & Bread",
                    price = 3.48,
                    formattedPrice = "$3.48",
                    unitPrice = "17.4¢/oz",
                    imageUrl = "https://i5.walmartimages.com/seo/Nature-s-Own-Honey-Wheat-Bread-20-oz-Loaf_d07525ec-8480-469b-8105-0a06148fe3a1.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock at local store",
                    userPreferenceNote = "Your standard pantry bread",
                    alternatives = listOf(
                        ProductAlternative(
                            title = "Dave's Killer Bread 21 Whole Grains & Seeds Organic Loaf, 27 oz",
                            brand = "Dave's Killer Bread",
                            price = 6.24,
                            formattedPrice = "$6.24",
                            reasonForMatch = "Organic whole grain alternative",
                            imageUrl = "https://i5.walmartimages.com/seo/Dave-s-Killer-Bread-21-Whole-Grains-and-Seeds-Organic-Bread-27-oz-Loaf_11eb4c58-86d1-42cb-b703-a261a8a25c3f.jpeg"
                        )
                    )
                )
            }
            lower.contains("banana") -> {
                GroceryProductCard(
                    queryItem = "Bananas",
                    matchedTitle = "Organic Bananas, Bunch (approx. 2.5 lb)",
                    brand = "Fresh Organic",
                    department = "Fresh Fruit",
                    price = 1.78,
                    formattedPrice = "$1.78",
                    unitPrice = "69¢/lb",
                    imageUrl = "https://i5.walmartimages.com/seo/Fresh-Organic-Bananas-Bunch_4f7bb400-0196-41f3-8b7c-87d8126b864a.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock"
                )
            }
            lower.contains("chicken") -> {
                GroceryProductCard(
                    queryItem = "Chicken",
                    matchedTitle = "Perdue Fresh Boneless Skinless Chicken Breasts, 2.5 lb",
                    brand = "Perdue",
                    department = "Meat & Seafood",
                    price = 8.98,
                    formattedPrice = "$8.98",
                    unitPrice = "$3.59/lb",
                    imageUrl = "https://i5.walmartimages.com/seo/PERDUE-Fresh-Boneless-Skinless-Chicken-Breasts-2-25-2-75-lb_014e7a83-aeb0-4dbf-8186-508be6fa5243.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock (Family pack available)"
                )
            }
            else -> {
                // Fallback realistic generic grocery card
                val titleCap = item.replaceFirstChar { it.uppercase() }
                GroceryProductCard(
                    queryItem = titleCap,
                    matchedTitle = "Great Value $titleCap, Family Size",
                    brand = "Great Value",
                    department = "Pantry Essentials",
                    price = 3.98,
                    formattedPrice = "$3.98",
                    unitPrice = "$0.24/oz",
                    imageUrl = "https://i5.walmartimages.com/seo/Great-Value-Pantry-Staple_default.jpeg",
                    stockStatus = StockStatus.IN_STOCK,
                    stockNotice = "In Stock"
                )
            }
        }
    }
}
