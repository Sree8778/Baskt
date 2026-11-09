package com.example.supportagent.data.stores

import com.example.supportagent.model.GroceryStore
import com.example.supportagent.model.StoreOrigin
import com.example.supportagent.model.StoreProduct
import com.example.supportagent.network.catalog.ProductImageResolver

object StoreRepository {

    val stores = listOf(
        GroceryStore(
            id = "wmt_supercenter",
            name = "Walmart Supercenter",
            chain = "Walmart",
            origin = StoreOrigin.WALMART,
            logoEmoji = "🛒",
            badge = "DIRECT SHELF PRICES",
            rating = 4.9,
            reviewCount = "12.4k",
            deliveryEta = "35-50 min",
            distanceMiles = "1.2 mi",
            deliveryFee = "$4.99 Flat",
            bannerColor = 0xFF0071DC,
            categories = listOf("All", "Produce", "Dairy & Eggs", "Bakery", "Meat", "Pantry", "Drinks", "Household"),
            highlights = "Exact in-store prices • $0 hidden markup"
        ),
        GroceryStore(
            id = "aldi_boca",
            name = "ALDI",
            chain = "ALDI",
            origin = StoreOrigin.INSTACART_ALDI,
            logoEmoji = "🥦",
            badge = "LOW PRICE LEADER",
            rating = 4.8,
            reviewCount = "8.1k",
            deliveryEta = "30-45 min",
            distanceMiles = "2.4 mi",
            deliveryFee = "$4.99 Flat",
            bannerColor = 0xFF001E82,
            categories = listOf("All", "Produce", "Dairy & Eggs", "Bakery", "Meat", "Snacks", "Drinks"),
            highlights = "Simply Nature & Friendly Farms • 0% Markup"
        ),
        GroceryStore(
            id = "target_boca",
            name = "Target Grocery",
            chain = "Target",
            origin = StoreOrigin.INSTACART_TARGET,
            logoEmoji = "🎯",
            badge = "SAME-DAY FRESH",
            rating = 4.8,
            reviewCount = "9.3k",
            deliveryEta = "35-50 min",
            distanceMiles = "2.9 mi",
            deliveryFee = "$4.99 Flat",
            bannerColor = 0xFFCC0000,
            categories = listOf("All", "Produce", "Dairy & Eggs", "Bakery", "Meat", "Snacks", "Household"),
            highlights = "Good & Gather & Favorite Day favorites"
        ),
        GroceryStore(
            id = "costco_boca",
            name = "Costco Wholesale",
            chain = "Costco",
            origin = StoreOrigin.INSTACART_KROGER,
            logoEmoji = "📦",
            badge = "BULK SAVINGS",
            rating = 4.9,
            reviewCount = "15.2k",
            deliveryEta = "45-60 min",
            distanceMiles = "3.8 mi",
            deliveryFee = "$5.99 Flat",
            bannerColor = 0xFFE31837,
            categories = listOf("All", "Meat", "Dairy & Eggs", "Produce", "Pantry", "Household"),
            highlights = "Kirkland Signature • Bulk family volume"
        ),
        GroceryStore(
            id = "trader_joes_boca",
            name = "Trader Joe's",
            chain = "Trader Joe's",
            origin = StoreOrigin.INSTACART_ALDI,
            logoEmoji = "🌺",
            badge = "ORGANIC & SPECIALTY",
            rating = 4.9,
            reviewCount = "14.1k",
            deliveryEta = "40-55 min",
            distanceMiles = "4.1 mi",
            deliveryFee = "$4.99 Flat",
            bannerColor = 0xFFBA1A1A,
            categories = listOf("All", "Entrees", "Produce", "Cheese", "Snacks", "Dessert", "Drinks"),
            highlights = "Cult-favorite specialties • Organic fresh goods"
        ),
        GroceryStore(
            id = "walgreens_boca",
            name = "Walgreens Pharmacy",
            chain = "Walgreens",
            origin = StoreOrigin.INSTACART_TARGET,
            logoEmoji = "💊",
            badge = "24/7 ESSENTIALS",
            rating = 4.7,
            reviewCount = "4.2k",
            deliveryEta = "20-35 min",
            distanceMiles = "0.8 mi",
            deliveryFee = "$3.99 Flat",
            bannerColor = 0xFFE31837,
            categories = listOf("All", "Pharmacy", "Snacks", "Drinks", "Personal Care", "Household"),
            highlights = "Late night snacks • Medicine & health"
        )
    )

    fun getProductsForStore(storeId: String): List<StoreProduct> {
        return when (storeId) {
            "wmt_supercenter" -> getWalmartProducts()
            "aldi_boca" -> getAldiProducts()
            "target_boca" -> getTargetProducts()
            "costco_boca" -> getCostcoProducts()
            "trader_joes_boca" -> getTraderJoesProducts()
            "walgreens_boca" -> getWalgreensProducts()
            else -> getWalmartProducts()
        }
    }

    fun getAllProducts(): List<StoreProduct> {
        return getWalmartProducts() + getAldiProducts() + getTargetProducts() +
                getCostcoProducts() + getTraderJoesProducts() + getWalgreensProducts()
    }

    // 1. WALMART SUPERCENTER
    private fun getWalmartProducts(): List<StoreProduct> {
        val sId = "wmt_supercenter"
        val sName = "Walmart Supercenter"
        val origin = StoreOrigin.WALMART

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value Whole Vitamin D Milk",
                brand = "Great Value", price = 3.88, formattedPrice = "$3.88",
                unitSize = "1 Gallon (128 fl oz)", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("whole milk"),
                upc = "078742351865", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value Grade A Large White Eggs",
                brand = "Great Value", price = 2.98, formattedPrice = "$2.98",
                unitSize = "18 Count Carton", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("eggs"),
                upc = "078742220451", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Marketside Fresh French Bread",
                brand = "Marketside", price = 1.98, formattedPrice = "$1.98",
                unitSize = "16 oz Loaf", category = "Bakery",
                imageUrl = ProductImageResolver.resolveImage("french bread"),
                upc = "078742091211", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Marketside Traditional Rotisserie Chicken",
                brand = "Marketside", price = 6.97, formattedPrice = "$6.97",
                unitSize = "Fresh Hot 2 lb", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("rotisserie"),
                upc = "078742118949", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value 80/20 Ground Beef Roll",
                brand = "Great Value", price = 12.48, formattedPrice = "$12.48",
                unitSize = "3 lb Chub Roll", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("ground beef"),
                upc = "078742051284"
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Marketside Caesar Salad Bowl",
                brand = "Marketside", price = 3.98, formattedPrice = "$3.98",
                unitSize = "6.25 oz Single Serve", category = "Produce",
                imageUrl = ProductImageResolver.resolveImage("salad")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Honeycrisp Apples",
                brand = "Fresh Farm", price = 4.48, formattedPrice = "$4.48",
                unitSize = "3 lb bag", category = "Produce",
                imageUrl = ProductImageResolver.resolveImage("honeycrisp apple")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value Pepperoni Thin Crust Pizza",
                brand = "Great Value", price = 3.88, formattedPrice = "$3.88",
                unitSize = "16.2 oz Frozen", category = "Pantry",
                imageUrl = ProductImageResolver.resolveImage("pizza")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value Purified Drinking Water",
                brand = "Great Value", price = 5.36, formattedPrice = "$5.36",
                unitSize = "40 x 16.9 fl oz Bottles", category = "Drinks",
                imageUrl = ProductImageResolver.resolveImage("water"),
                isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Great Value Ultra Paper Towels",
                brand = "Great Value", price = 8.98, formattedPrice = "$8.98",
                unitSize = "6 Double Rolls", category = "Household",
                imageUrl = ProductImageResolver.resolveImage("paper towels")
            )
        )
    }

    // 2. ALDI
    private fun getAldiProducts(): List<StoreProduct> {
        val sId = "aldi_boca"
        val sName = "ALDI"
        val origin = StoreOrigin.INSTACART_ALDI

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Simply Nature 100% Grass-Fed Ground Beef",
                brand = "Simply Nature", price = 5.49, formattedPrice = "$5.49",
                unitSize = "1 lb (85/15 Organic)", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("ground beef"),
                upc = "040991001284", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Friendly Farms Whole Vitamin D Milk",
                brand = "Friendly Farms", price = 3.19, formattedPrice = "$3.19",
                unitSize = "1 Gallon (128 fl oz)", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("milk"),
                upc = "040991003481", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Goldhen Grade A Large Eggs",
                brand = "Goldhen", price = 1.89, formattedPrice = "$1.89",
                unitSize = "12 Count Carton", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("eggs"),
                upc = "040991005819", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Clancy's Sea Salt Kettle Potato Chips",
                brand = "Clancy's", price = 1.99, formattedPrice = "$1.99",
                unitSize = "8.5 oz Bag", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("clancy"),
                upc = "040991000179", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Specially Selected Sourdough Round Loaf",
                brand = "Specially Selected", price = 3.49, formattedPrice = "$3.49",
                unitSize = "24 oz Artisan Loaf", category = "Bakery",
                imageUrl = ProductImageResolver.resolveImage("sourdough")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Park Street Deli Classic Hummus",
                brand = "Park Street Deli", price = 2.29, formattedPrice = "$2.29",
                unitSize = "10 oz Tub", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("hummus")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Simply Nature Organic Baby Spinach",
                brand = "Simply Nature", price = 3.89, formattedPrice = "$3.89",
                unitSize = "16 oz Clamshell", category = "Produce",
                imageUrl = ProductImageResolver.resolveImage("spinach")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Moser Roth 85% Dark Chocolate Bar",
                brand = "Moser Roth", price = 2.19, formattedPrice = "$2.19",
                unitSize = "4.4 oz (5 individually wrapped)", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("chocolate"),
                upc = "4061458020979"
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Appleton Farms Thick Cut Applewood Bacon",
                brand = "Appleton Farms", price = 3.99, formattedPrice = "$3.99",
                unitSize = "16 oz Vacuum Pack", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("bacon")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Friendly Farms Greek Yogurt Plain",
                brand = "Friendly Farms", price = 3.49, formattedPrice = "$3.49",
                unitSize = "32 oz Tub", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("yogurt")
            )
        )
    }

    // 3. TARGET GROCERY
    private fun getTargetProducts(): List<StoreProduct> {
        val sId = "target_boca"
        val sName = "Target Grocery"
        val origin = StoreOrigin.INSTACART_TARGET

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Good & Gather Organic Whole Milk",
                brand = "Good & Gather", price = 4.19, formattedPrice = "$4.19",
                unitSize = "64 fl oz (1/2 Gallon)", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("milk"),
                upc = "085239012845", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Favorite Day Bakery Chocolate Chip Cookies",
                brand = "Favorite Day", price = 3.99, formattedPrice = "$3.99",
                unitSize = "12 ct Fresh Baked", category = "Bakery",
                imageUrl = ProductImageResolver.resolveImage("cookies"),
                upc = "085239091823", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Good & Gather Cage-Free Large Grade A Eggs",
                brand = "Good & Gather", price = 2.99, formattedPrice = "$2.99",
                unitSize = "12 Count Carton", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("eggs"),
                upc = "085239045129", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Favorite Day Caramel Cold Brew Coffee",
                brand = "Favorite Day", price = 4.49, formattedPrice = "$4.49",
                unitSize = "48 fl oz Jug", category = "Drinks",
                imageUrl = ProductImageResolver.resolveImage("coffee"),
                isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Good & Gather Spring Mix Greens",
                brand = "Good & Gather", price = 4.29, formattedPrice = "$4.29",
                unitSize = "16 oz Tub", category = "Produce",
                imageUrl = ProductImageResolver.resolveImage("salad")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Good & Gather Naturally Hickory Smoked Bacon",
                brand = "Good & Gather", price = 4.99, formattedPrice = "$4.99",
                unitSize = "16 oz Pack", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("bacon")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Favorite Day Double Fudge Brownie Bites",
                brand = "Favorite Day", price = 4.29, formattedPrice = "$4.29",
                unitSize = "10 oz Tub", category = "Bakery",
                imageUrl = ProductImageResolver.resolveImage("cookies")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Good & Gather Raw Whole Natural Almonds",
                brand = "Good & Gather", price = 5.99, formattedPrice = "$5.99",
                unitSize = "16 oz Resealable Pouch", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("almonds")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Up & Up Disinfecting Bleach Wipes",
                brand = "Up & Up", price = 3.99, formattedPrice = "$3.99",
                unitSize = "75 ct Canister", category = "Household",
                imageUrl = ProductImageResolver.resolveImage("dawn")
            )
        )
    }

    // 4. COSTCO WHOLESALE
    private fun getCostcoProducts(): List<StoreProduct> {
        val sId = "costco_boca"
        val sName = "Costco Wholesale"
        val origin = StoreOrigin.INSTACART_KROGER

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Whole Rotisserie Chicken",
                brand = "Kirkland Signature", price = 4.99, formattedPrice = "$4.99",
                unitSize = "Fresh Hot 3 lb Wholesale", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("rotisserie"),
                upc = "096619114829", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Extra Virgin Olive Oil",
                brand = "Kirkland Signature", price = 18.99, formattedPrice = "$18.99",
                unitSize = "2 Liter Bottle", category = "Pantry",
                imageUrl = ProductImageResolver.resolveImage("olive oil"),
                upc = "096619234819", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Organic Cage-Free Eggs",
                brand = "Kirkland Signature", price = 5.99, formattedPrice = "$5.99",
                unitSize = "2 Dozen (24 Count Flat)", category = "Dairy & Eggs",
                imageUrl = ProductImageResolver.resolveImage("eggs"),
                upc = "096619881249", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Thick Sliced Bacon",
                brand = "Kirkland Signature", price = 15.99, formattedPrice = "$15.99",
                unitSize = "4 lb Bulk Pack", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("bacon")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature 100% Colombian Coffee",
                brand = "Kirkland Signature", price = 14.99, formattedPrice = "$14.99",
                unitSize = "3 lb Supremo Tin", category = "Pantry",
                imageUrl = ProductImageResolver.resolveImage("colombian"),
                isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Chewy Protein Bars",
                brand = "Kirkland Signature", price = 14.49, formattedPrice = "$14.49",
                unitSize = "20 Count Variety Box", category = "Pantry",
                imageUrl = ProductImageResolver.resolveImage("protein bar"),
                upc = "0096619248592"
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Create-A-Size Paper Towels",
                brand = "Kirkland Signature", price = 21.99, formattedPrice = "$21.99",
                unitSize = "12 Giant Rolls Bulk", category = "Household",
                imageUrl = ProductImageResolver.resolveImage("paper towels"),
                isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Atlantic Salmon Fillet",
                brand = "Kirkland Signature", price = 28.99, formattedPrice = "$28.99",
                unitSize = "approx 3 lb Fresh Cut", category = "Meat",
                imageUrl = ProductImageResolver.resolveImage("salmon")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Kirkland Signature Extra Fancy Mixed Nuts Salted",
                brand = "Kirkland Signature", price = 14.99, formattedPrice = "$14.99",
                unitSize = "2.5 lb Jar", category = "Pantry",
                imageUrl = ProductImageResolver.resolveImage("cashews")
            )
        )
    }

    // 5. TRADER JOE'S
    private fun getTraderJoesProducts(): List<StoreProduct> {
        val sId = "trader_joes_boca"
        val sName = "Trader Joe's"
        val origin = StoreOrigin.INSTACART_ALDI

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Mandarin Orange Chicken",
                brand = "Trader Joe's", price = 4.99, formattedPrice = "$4.99",
                unitSize = "22 oz Frozen Entree", category = "Entrees",
                imageUrl = ProductImageResolver.resolveImage("orange chicken"),
                upc = "0073535001000", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Everything But The Bagel Sesame Seasoning",
                brand = "Trader Joe's", price = 1.99, formattedPrice = "$1.99",
                unitSize = "2.3 oz Glass Shaker", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("everything bagel"),
                upc = "0000000059827", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Cauliflower Gnocchi",
                brand = "Trader Joe's", price = 2.99, formattedPrice = "$2.99",
                unitSize = "12 oz Frozen Bag", category = "Entrees",
                imageUrl = ProductImageResolver.resolveImage("gnocchi"),
                upc = "0000000060069", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Speculoos Creamy Cookie Butter",
                brand = "Trader Joe's", price = 3.99, formattedPrice = "$3.99",
                unitSize = "14.1 oz Jar", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("cookie butter"),
                upc = "00447948", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Hold the Cone! Mini Vanilla Cones",
                brand = "Trader Joe's", price = 3.99, formattedPrice = "$3.99",
                unitSize = "8 ct Box Frozen", category = "Dessert",
                imageUrl = ProductImageResolver.resolveImage("ice cream"),
                upc = "0000000059841"
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Unexpected Cheddar Cheese",
                brand = "Trader Joe's", price = 3.99, formattedPrice = "$3.99",
                unitSize = "7 oz Block", category = "Cheese",
                imageUrl = ProductImageResolver.resolveImage("cheese")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Teeny Tiny Avocados",
                brand = "Trader Joe's", price = 3.99, formattedPrice = "$3.99",
                unitSize = "6 pack mesh bag", category = "Produce",
                imageUrl = ProductImageResolver.resolveImage("avocado")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Organic Pasture-Raised Large Eggs",
                brand = "Trader Joe's", price = 4.49, formattedPrice = "$4.49",
                unitSize = "12 Count Carton", category = "Cheese",
                imageUrl = ProductImageResolver.resolveImage("eggs")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Trader Joe's Cold Pressed Green Juice",
                brand = "Trader Joe's", price = 3.99, formattedPrice = "$3.99",
                unitSize = "15.2 fl oz Bottle", category = "Drinks",
                imageUrl = ProductImageResolver.resolveImage("water")
            )
        )
    }

    // 6. WALGREENS PHARMACY
    private fun getWalgreensProducts(): List<StoreProduct> {
        val sId = "walgreens_boca"
        val sName = "Walgreens Pharmacy"
        val origin = StoreOrigin.INSTACART_TARGET

        return listOf(
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Walgreens Dye-Free Ibuprofen 200mg",
                brand = "Walgreens", price = 6.99, formattedPrice = "$6.99",
                unitSize = "100 Softgels", category = "Pharmacy",
                imageUrl = ProductImageResolver.resolveImage("ibuprofen"),
                upc = "311917128491", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Nature Made Vitamin C 1000mg with Rose Hips",
                brand = "Nature Made", price = 12.99, formattedPrice = "$12.99",
                unitSize = "100 Tablets", category = "Pharmacy",
                imageUrl = ProductImageResolver.resolveImage("vitamin c"),
                upc = "024208003194", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Nice! Deluxe Roasted Salted Whole Cashews",
                brand = "Nice!", price = 4.49, formattedPrice = "$4.49",
                unitSize = "8.5 oz Resealable Can", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("cashews"),
                upc = "049022648192", isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Colgate Total Deep Clean Toothpaste Twin Pack",
                brand = "Colgate", price = 7.99, formattedPrice = "$7.99",
                unitSize = "2 x 4.8 oz Tubes", category = "Personal Care",
                imageUrl = ProductImageResolver.resolveImage("toothpaste"),
                upc = "035000458129"
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Doritos Nacho Cheese Tortilla Chips Party Size",
                brand = "Doritos", price = 5.99, formattedPrice = "$5.99",
                unitSize = "14.5 oz Bag", category = "Snacks",
                imageUrl = ProductImageResolver.resolveImage("doritos"),
                isPopular = true
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Gatorade Frost Glacier Freeze Sports Drink",
                brand = "Gatorade", price = 2.49, formattedPrice = "$2.49",
                unitSize = "28 fl oz Bottle", category = "Drinks",
                imageUrl = ProductImageResolver.resolveImage("gatorade")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Tide PODS Clean Breeze Laundry Detergent",
                brand = "Tide", price = 13.99, formattedPrice = "$13.99",
                unitSize = "42 Count Tub", category = "Household",
                imageUrl = ProductImageResolver.resolveImage("tide")
            ),
            StoreProduct(
                storeId = sId, storeName = sName, origin = origin,
                name = "Band-Aid Flexible Fabric Adhesive Bandages",
                brand = "Band-Aid", price = 4.99, formattedPrice = "$4.99",
                unitSize = "30 ct Box", category = "Pharmacy",
                imageUrl = ProductImageResolver.resolveImage("dawn")
            )
        )
    }
}
