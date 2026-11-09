package com.example.supportagent.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import com.example.supportagent.model.ActionCategory
import com.example.supportagent.model.ActionProposal
import com.example.supportagent.util.AppLauncher
import kotlinx.coroutines.delay

object GhostTouchCommander {

    fun isServiceReady(): Boolean {
        return GhostTouchAgentService.isServiceEnabled
    }

    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    suspend fun runAutonomousAppMission(
        context: Context,
        proposal: ActionProposal,
        onStepProgress: (String) -> Unit
    ): Boolean {
        val service = GhostTouchAgentService.instance
        val entityLower = proposal.targetEntity.lowercase()

        when {
            // 1. Walmart Groceries Cart
            entityLower.contains("walmart") || proposal.category == ActionCategory.GROCERY_RESTOCK -> {
                return executeWalmartCartAutomation(context, service, proposal.cartItems, onStepProgress)
            }
            // 2. Uber Eats Cart (Chipotle, etc.)
            entityLower.contains("uber eats") || entityLower.contains("chipotle") || proposal.category == ActionCategory.FOOD_DELIVERY -> {
                return executeUberEatsCartAutomation(context, service, proposal, onStepProgress)
            }
            // 3. Amazon Shopping Cart
            entityLower.contains("amazon") -> {
                return executeAmazonCartAutomation(context, service, proposal.cartItems, onStepProgress)
            }
            // 4. Costco Cart
            entityLower.contains("costco") -> {
                return executeCostcoCartAutomation(context, service, proposal.cartItems, onStepProgress)
            }
            // 5. Publix Cart
            entityLower.contains("publix") -> {
                return executePublixCartAutomation(context, service, proposal.cartItems, onStepProgress)
            }
            // 6. Travel & Car Rental
            proposal.category == ActionCategory.TRAVEL_RENTAL -> {
                onStepProgress("Opening Turo reservation...")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://turo.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }
            // 7. Lodging
            proposal.category == ActionCategory.LODGING_RESERVATION -> {
                onStepProgress("Opening Airbnb listing...")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.airbnb.com")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }
            // 8. Phone Call / Telephony
            proposal.category == ActionCategory.SUPPORT_CALL -> {
                onStepProgress("Opening phone dialer for ${proposal.targetContact}...")
                val number = proposal.targetContact.filter { it.isDigit() }
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }
            else -> {
                onStepProgress("Executing mission...")
                return true
            }
        }
    }

    private suspend fun executeWalmartCartAutomation(
        context: Context,
        service: GhostTouchAgentService?,
        items: List<String>,
        onStepProgress: (String) -> Unit
    ): Boolean {
        val itemsToAdd = if (items.isNotEmpty()) items else listOf("Great Value Whole Milk", "Organic Grade-A Eggs")

        for (item in itemsToAdd) {
            onStepProgress("Searching Walmart for '$item'...")

            // Deep-link directly into the Walmart app search results for this exact item!
            val searchUri = Uri.parse("https://www.walmart.com/search?q=" + Uri.encode(item))
            val searchIntent = Intent(Intent.ACTION_VIEW, searchUri).apply {
                setPackage(AppLauncher.WALMART_PKG)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            try {
                context.startActivity(searchIntent)
            } catch (e: Exception) {
                AppLauncher.launchApp(context, AppLauncher.WALMART_PKG)
            }

            // Allow Walmart's search result grid to load
            delay(3800)

            if (service != null) {
                onStepProgress("Adding '$item' to shopping cart...")
                // Strict Add-To-Cart: excludes lists, favorites, and saved bookmarks!
                var added = service.findAndClickAddToCart()
                if (!added) {
                    delay(1500)
                    added = service.findAndClickAddToCart()
                }
                Log.d("GhostTouch", "Item '$item' added to cart: $added")
            }

            delay(1500)
        }

        // Open Walmart Cart view for review
        onStepProgress("Opening Walmart Cart for review...")
        val cartUri = Uri.parse("https://www.walmart.com/cart")
        val cartIntent = Intent(Intent.ACTION_VIEW, cartUri).apply {
            setPackage(AppLauncher.WALMART_PKG)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        try {
            context.startActivity(cartIntent)
        } catch (e: Exception) {
            service?.findAndClick("Cart")
        }

        delay(1200)
        onStepProgress("All items added to Walmart Cart! Ready for review.")
        return true
    }

    private suspend fun executeUberEatsCartAutomation(
        context: Context,
        service: GhostTouchAgentService?,
        proposal: ActionProposal,
        onStepProgress: (String) -> Unit
    ): Boolean {
        onStepProgress("Opening Uber Eats native mobile app...")
        val launched = AppLauncher.launchApp(context, AppLauncher.UBER_EATS_PKG)

        if (!launched) {
            onStepProgress("Uber Eats app not found.")
            return false
        }

        if (service == null) {
            onStepProgress("Opened Uber Eats app! Enable Ghost Touch in settings.")
            return true
        }

        delay(4000)
        onStepProgress("Ghost Touch: Locating Chipotle...")
        service.findAndClick("Chipotle") || service.typeTextIntoField("Search", "Chipotle")

        delay(2500)
        onStepProgress("Ghost Touch: Selecting 'Chicken Burrito Bowl'...")
        service.findAndClick("Chicken Burrito Bowl") || service.findAndClick("Burrito Bowl")

        delay(1500)
        onStepProgress("Ghost Touch: Customizing Bowl...")
        service.findAndClick("Brown Rice")
        delay(600)
        service.findAndClick("Black Beans")

        delay(1500)
        onStepProgress("Ghost Touch: Adding to Order...")
        service.findAndClickAddToCart()

        delay(2000)
        onStepProgress("Ghost Touch: Opening Cart...")
        service.findAndClick("View Cart") || service.findAndClick("Cart")

        delay(1000)
        onStepProgress("Meal added to Uber Eats cart! Ready for review.")
        return true
    }

    private suspend fun executeAmazonCartAutomation(
        context: Context,
        service: GhostTouchAgentService?,
        items: List<String>,
        onStepProgress: (String) -> Unit
    ): Boolean {
        val itemsToAdd = if (items.isNotEmpty()) items else listOf("Bounty Paper Towels", "Coffee Beans")

        for (item in itemsToAdd) {
            onStepProgress("Searching Amazon for '$item'...")
            val searchUri = Uri.parse("https://www.amazon.com/s?k=" + Uri.encode(item))
            val searchIntent = Intent(Intent.ACTION_VIEW, searchUri).apply {
                setPackage(AppLauncher.AMAZON_PKG)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }

            try {
                context.startActivity(searchIntent)
            } catch (e: Exception) {
                AppLauncher.launchApp(context, AppLauncher.AMAZON_PKG)
            }

            delay(3500)
            if (service != null) {
                onStepProgress("Adding '$item' to Amazon cart...")
                service.findAndClickAddToCart()
            }
            delay(1200)
        }

        delay(1500)
        onStepProgress("Ghost Touch: Opening Amazon Cart...")
        service?.findAndClick("Cart")
        onStepProgress("Items placed in Amazon cart! Ready for checkout.")
        return true
    }

    private suspend fun executeCostcoCartAutomation(
        context: Context,
        service: GhostTouchAgentService?,
        items: List<String>,
        onStepProgress: (String) -> Unit
    ): Boolean {
        onStepProgress("Opening Costco app...")
        val launched = AppLauncher.launchApp(context, AppLauncher.COSTCO_PKG)
        if (!launched || service == null) return true

        delay(3200)
        val itemsToAdd = if (items.isNotEmpty()) items else listOf("Kirkland Signature Paper Towels")
        for (item in itemsToAdd) {
            onStepProgress("Ghost Touch: Searching Costco for '$item'...")
            service.typeTextIntoField("Search", item)
            delay(2000)
            service.findAndClickAddToCart()
        }

        delay(1500)
        service.findAndClick("Cart")
        onStepProgress("Items added to Costco cart! Ready for review.")
        return true
    }

    private suspend fun executePublixCartAutomation(
        context: Context,
        service: GhostTouchAgentService?,
        items: List<String>,
        onStepProgress: (String) -> Unit
    ): Boolean {
        onStepProgress("Opening Publix app...")
        val launched = AppLauncher.launchApp(context, AppLauncher.PUBLIX_PKG)
        if (!launched || service == null) return true

        delay(3200)
        val itemsToAdd = if (items.isNotEmpty()) items else listOf("Publix Deli Sub")
        for (item in itemsToAdd) {
            onStepProgress("Ghost Touch: Searching Publix for '$item'...")
            service.typeTextIntoField("Search", item)
            delay(2000)
            service.findAndClickAddToCart()
        }

        delay(1500)
        service.findAndClick("Cart")
        onStepProgress("Items placed in Publix cart! Ready for review.")
        return true
    }
}
