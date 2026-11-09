package com.example.supportagent.network.catalog

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LiveGroceryCatalogServiceTest {

    @Test
    fun testLiveProductSearch() = runBlocking {
        val results = LiveGroceryCatalogService.searchLiveProducts("cheerios", limit = 5)
        println("=== LIVE GROCERY API TEST RESULTS ===")
        println("Fetched ${results.size} live items for 'cheerios':")
        results.forEachIndexed { i, p ->
            println("${i + 1}. [${p.storeName}] ${p.name} | Brand: ${p.brand} | UPC: ${p.upc} | Price: ${p.formattedPrice} | Img: ${p.imageUrl.take(50)}...")
        }
        assertFalse("Results should not be empty", results.isEmpty())
        val first = results.first()
        assertNotNull("Product should have a title", first.name)
        assertTrue("Product price should be > 0", first.price > 0.0)
    }
}
