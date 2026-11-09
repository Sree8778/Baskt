package com.example.supportagent.ui.stores

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.supportagent.data.stores.StoreRepository
import com.example.supportagent.model.CartItem
import com.example.supportagent.model.GroceryStore
import com.example.supportagent.model.StoreProduct
import com.example.supportagent.model.UserStoreCart
import com.example.supportagent.network.catalog.LiveGroceryCatalogService

@Composable
fun StoresScreen(
    cart: UserStoreCart,
    isDarkTheme: Boolean,
    onAddToCart: (StoreProduct) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit,
    onSwitchToDriver: () -> Unit,
    onToggleTheme: () -> Unit,
    onEditAddressClick: () -> Unit,
    onViewCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStore by remember { mutableStateOf<GroceryStore?>(null) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var liveSearchResults by remember { mutableStateOf<List<StoreProduct>>(emptyList()) }
    var isSearchingProducts by remember { mutableStateOf(false) }
    var inspectingSearchProduct by remember { mutableStateOf<StoreProduct?>(null) }

    LaunchedEffect(searchQuery) {
        val query = searchQuery.trim()
        if (query.length >= 2) {
            isSearchingProducts = true
            kotlinx.coroutines.delay(200)
            val results = LiveGroceryCatalogService.searchLiveProducts(query)
            liveSearchResults = results
            isSearchingProducts = false
        } else {
            liveSearchResults = emptyList()
            isSearchingProducts = false
        }
    }

    val bgColor = if (isDarkTheme) Color(0xFF000000) else Color(0xFFF2F2F7)
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // Top App Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEditAddressClick() }
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DELIVERING TO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A84FF),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Edit Address",
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = cart.deliveryAddress.take(28) + if (cart.deliveryAddress.length > 28) "..." else "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = primaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Switch to Driver Mode pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isDarkTheme) Color(0xFF1C1D26) else Color(0xFFE5E5EA))
                        .border(1.dp, borderCol, RoundedCornerShape(20.dp))
                        .clickable { onSwitchToDriver() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeliveryDining,
                            contentDescription = "Driver",
                            tint = Color(0xFF30D158),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Driver Mode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Theme Toggle
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = primaryText,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        if (selectedStore == null) {
            // Main Stores Browse Feed
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text("Search live groceries, snacks, stores...", fontSize = 13.sp, color = Color(0xFF8E8E93))
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF8E8E93))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = subText, modifier = Modifier.size(14.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = borderCol,
                            focusedTextColor = primaryText,
                            unfocusedTextColor = primaryText
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                }

                if (searchQuery.isNotBlank()) {
                    // Live Search Results Active Mode
                    if (isSearchingProducts) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF0A84FF)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Querying live grocery catalogs across stores...",
                                    fontSize = 12.sp,
                                    color = subText
                                )
                            }
                        }
                    } else if (liveSearchResults.isNotEmpty()) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 8.dp)
                            ) {
                                Text(
                                    text = "Live Groceries (${liveSearchResults.size})",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primaryText
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF0D2818))
                                        .border(1.dp, Color(0xFF30D158).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "LIVE RETAILER API",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF30D158)
                                    )
                                }
                            }
                        }

                        val chunked = liveSearchResults.chunked(2)
                        items(chunked) { pair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                for (prod in pair) {
                                    val cartItem = cart.items.find { it.title.equals(prod.name, ignoreCase = true) }
                                    val qty = cartItem?.quantity ?: 0
                                    Box(modifier = Modifier.weight(1f)) {
                                        ProductGridCard(
                                            product = prod,
                                            quantityInCart = qty,
                                            isDarkTheme = isDarkTheme,
                                            onClick = { inspectingSearchProduct = prod },
                                            onAdd = { onAddToCart(prod) },
                                            onIncrement = {
                                                if (cartItem != null) onUpdateQuantity(cartItem.id, qty + 1)
                                                else onAddToCart(prod)
                                            },
                                            onDecrement = {
                                                if (cartItem != null) {
                                                    if (qty > 1) onUpdateQuantity(cartItem.id, qty - 1)
                                                    else onRemoveFromCart(cartItem.id)
                                                }
                                            }
                                        )
                                    }
                                }
                                if (pair.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    // Matching Stores
                    val filteredStores = StoreRepository.stores.filter {
                        it.name.contains(searchQuery, ignoreCase = true) || it.chain.contains(searchQuery, ignoreCase = true)
                    }

                    if (filteredStores.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Stores Matching \"$searchQuery\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                        items(filteredStores, key = { it.id }) { store ->
                            StoreCard(
                                store = store,
                                isDarkTheme = isDarkTheme,
                                onClick = { selectedStore = store }
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    } else if (liveSearchResults.isEmpty() && !isSearchingProducts) {
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            ) {
                                Text(
                                    text = "No matching groceries or stores found for \"$searchQuery\"",
                                    fontSize = 13.sp,
                                    color = subText
                                )
                            }
                        }
                    }
                } else {
                    // Default Stores & Category Browse Feed
                    item {
                        Spacer(modifier = Modifier.height(10.dp))

                        // Provi Zero Markup Hero Card
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDarkTheme) Color(0xFF0E1A14) else Color(0xFFE8F8EE)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF30D158).copy(alpha = 0.35f),
                                    shape = RoundedCornerShape(18.dp)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF30D158).copy(alpha = 0.2f))
                                ) {
                                    Text("0%", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF30D158))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Zero Hidden Markups Guaranteed",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = primaryText
                                    )
                                    Text(
                                        text = "Pay exact in-store shelf prices. Flat \$4.99 driver fee with 100% of fees and tips paid to local drivers.",
                                        fontSize = 11.sp,
                                        color = subText,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    item {
                        // Category Filter Chips
                        val categories = listOf("All Stores", "Grocery", "Wholesale", "Organic", "Pharmacy")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            items(categories) { category ->
                                val isSelected = selectedCategoryFilter == category
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Color(0xFF0A84FF) else cardBg)
                                        .border(1.dp, if (isSelected) Color.Transparent else borderCol, RoundedCornerShape(16.dp))
                                        .clickable { selectedCategoryFilter = category }
                                        .padding(horizontal = 14.dp, vertical = 7.dp)
                                ) {
                                    Text(
                                        text = category,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else primaryText
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Stores Near Boca Raton",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }

                    items(StoreRepository.stores, key = { it.id }) { store ->
                        StoreCard(
                            store = store,
                            isDarkTheme = isDarkTheme,
                            onClick = { selectedStore = store }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        } else {
            // Selected Store Catalog / Product Shelf
            StoreCatalogView(
                store = selectedStore!!,
                cart = cart,
                isDarkTheme = isDarkTheme,
                onBack = { selectedStore = null },
                onAddToCart = onAddToCart,
                onRemoveFromCart = onRemoveFromCart,
                onUpdateQuantity = onUpdateQuantity
            )
        }

        // Floating Bottom Cart Bar if items exist
        if (cart.totalItemCount > 0) {
            Surface(
                color = if (isDarkTheme) Color(0xFF141520) else Color.White,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, borderCol)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = "${cart.totalItemCount} items in Cart",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = subText
                        )
                        Text(
                            text = cart.formattedFinalTotal,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                    }

                    Button(
                        onClick = onViewCartClick,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("View Cart", fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        if (inspectingSearchProduct != null) {
            val prod = inspectingSearchProduct!!
            val cartItem = cart.items.find { it.title.equals(prod.name, ignoreCase = true) }
            val qty = cartItem?.quantity ?: 0

            ProductQuickDetailModal(
                product = prod,
                quantityInCart = qty,
                isDarkTheme = isDarkTheme,
                onDismiss = { inspectingSearchProduct = null },
                onAdd = { onAddToCart(prod) },
                onIncrement = {
                    if (cartItem != null) onUpdateQuantity(cartItem.id, qty + 1)
                    else onAddToCart(prod)
                },
                onDecrement = {
                    if (cartItem != null) {
                        if (qty > 1) onUpdateQuantity(cartItem.id, qty - 1)
                        else onRemoveFromCart(cartItem.id)
                    }
                }
            )
        }
    }
}

@Composable
fun StoreCard(
    store: GroceryStore,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column {
            // Header Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(store.bannerColor),
                                Color(store.bannerColor).copy(alpha = 0.65f)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color.White)
                    ) {
                        Text(store.logoEmoji, fontSize = 28.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = store.badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                }
            }

            // Store Info
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = store.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryText
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFD60A), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${store.rating}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryText
                        )
                        Text(
                            text = " (${store.reviewCount})",
                            fontSize = 11.sp,
                            color = subText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⚡ ${store.deliveryEta}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF30D158)
                    )
                    Text("•", fontSize = 12.sp, color = subText)
                    Text(
                        text = "${store.distanceMiles} away",
                        fontSize = 12.sp,
                        color = subText
                    )
                    Text("•", fontSize = 12.sp, color = subText)
                    Text(
                        text = store.deliveryFee,
                        fontSize = 12.sp,
                        color = subText
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = store.highlights,
                    fontSize = 11.sp,
                    color = Color(0xFF0A84FF),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun StoreCatalogView(
    store: GroceryStore,
    cart: UserStoreCart,
    isDarkTheme: Boolean,
    onBack: () -> Unit,
    onAddToCart: (StoreProduct) -> Unit,
    onRemoveFromCart: (String) -> Unit,
    onUpdateQuantity: (String, Int) -> Unit
) {
    val products = remember(store.id) { StoreRepository.getProductsForStore(store.id) }
    var selectedCategory by remember { mutableStateOf("All") }
    var inspectingProduct by remember { mutableStateOf<StoreProduct?>(null) }

    var storeSearchQuery by remember { mutableStateOf("") }
    var storeLiveProducts by remember { mutableStateOf<List<StoreProduct>>(emptyList()) }
    var isSearchingStore by remember { mutableStateOf(false) }

    LaunchedEffect(storeSearchQuery) {
        val q = storeSearchQuery.trim()
        if (q.length >= 2) {
            isSearchingStore = true
            kotlinx.coroutines.delay(200)
            storeLiveProducts = LiveGroceryCatalogService.searchLiveProducts(q, targetStore = store)
            isSearchingStore = false
        } else {
            storeLiveProducts = emptyList()
            isSearchingStore = false
        }
    }

    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    val displayedProducts = if (storeSearchQuery.isNotBlank()) {
        storeLiveProducts
    } else if (selectedCategory == "All") {
        products
    } else {
        products.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = primaryText)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = store.name,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )
                Text(
                    text = "${store.deliveryEta} • Exact Shelf Prices",
                    fontSize = 11.sp,
                    color = Color(0xFF30D158)
                )
            }
        }

        // Store Search Bar
        OutlinedTextField(
            value = storeSearchQuery,
            onValueChange = { storeSearchQuery = it },
            placeholder = {
                Text("Search in ${store.name}...", fontSize = 12.sp, color = subText)
            },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = subText, modifier = Modifier.size(16.dp))
            },
            trailingIcon = {
                if (storeSearchQuery.isNotBlank()) {
                    IconButton(onClick = { storeSearchQuery = "" }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = subText, modifier = Modifier.size(14.dp))
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = cardBg,
                unfocusedContainerColor = cardBg,
                focusedBorderColor = Color(0xFF0A84FF),
                unfocusedBorderColor = borderCol,
                focusedTextColor = primaryText,
                unfocusedTextColor = primaryText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Store Category Tabs (shown when not searching)
        if (storeSearchQuery.isBlank()) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                items(store.categories) { cat ->
                    val isSelected = selectedCategory == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF0A84FF) else cardBg)
                            .border(1.dp, if (isSelected) Color.Transparent else borderCol, RoundedCornerShape(16.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else primaryText
                        )
                    }
                }
            }
        } else if (isSearchingStore) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF0A84FF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Searching live ${store.name} inventory...",
                    fontSize = 11.sp,
                    color = subText
                )
            }
        }

        // 2-Column Product Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayedProducts, key = { it.id }) { product ->
                val cartItem = cart.items.find { it.title.equals(product.name, ignoreCase = true) }
                val quantityInCart = cartItem?.quantity ?: 0

                ProductGridCard(
                    product = product,
                    quantityInCart = quantityInCart,
                    isDarkTheme = isDarkTheme,
                    onClick = { inspectingProduct = product },
                    onAdd = { onAddToCart(product) },
                    onIncrement = {
                        if (cartItem != null) {
                            onUpdateQuantity(cartItem.id, quantityInCart + 1)
                        } else {
                            onAddToCart(product)
                        }
                    },
                    onDecrement = {
                        if (cartItem != null) {
                            if (quantityInCart > 1) {
                                onUpdateQuantity(cartItem.id, quantityInCart - 1)
                            } else {
                                onRemoveFromCart(cartItem.id)
                            }
                        }
                    }
                )
            }
        }
    }

    if (inspectingProduct != null) {
        val prod = inspectingProduct!!
        val cartItem = cart.items.find { it.title.equals(prod.name, ignoreCase = true) }
        val qty = cartItem?.quantity ?: 0

        ProductQuickDetailModal(
            product = prod,
            quantityInCart = qty,
            isDarkTheme = isDarkTheme,
            onDismiss = { inspectingProduct = null },
            onAdd = { onAddToCart(prod) },
            onIncrement = {
                if (cartItem != null) {
                    onUpdateQuantity(cartItem.id, qty + 1)
                } else {
                    onAddToCart(prod)
                }
            },
            onDecrement = {
                if (cartItem != null) {
                    if (qty > 1) {
                        onUpdateQuantity(cartItem.id, qty - 1)
                    } else {
                        onRemoveFromCart(cartItem.id)
                    }
                }
            }
        )
    }
}

@Composable
fun ProductGridCard(
    product: StoreProduct,
    quantityInCart: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit = {},
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Product Photo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDarkTheme) Color(0xFF1C1D28) else Color(0xFFF9F9FA))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp)
                )

                if (product.isPopular) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF30D158))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("POPULAR", fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Shelf Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = product.formattedPrice,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = primaryText
                )
                Text(
                    text = "Shelf Price",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF30D158)
                )
            }

            // Title & Unit Size
            Text(
                text = product.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = primaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
            Text(
                text = product.unitSize,
                fontSize = 10.sp,
                color = subText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart / Quantity Stepper
            if (quantityInCart == 0) {
                Button(
                    onClick = onAdd,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0A84FF))
                        .padding(horizontal = 6.dp)
                ) {
                    IconButton(
                        onClick = onDecrement,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "$quantityInCart",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = onIncrement,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductQuickDetailModal(
    product: StoreProduct,
    quantityInCart: Int,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onAdd: () -> Unit,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val cardBg = if (isDarkTheme) Color(0xFF141520) else Color(0xFFFFFFFF)
    val primaryText = if (isDarkTheme) Color.White else Color(0xFF000000)
    val subText = if (isDarkTheme) Color(0xFF8E8E93) else Color(0xFF6C6C70)
    val borderCol = if (isDarkTheme) Color(0x1FFFFFFF) else Color(0x1F000000)

    val competitorPrice = product.price * 1.35
    val competitorSavings = competitorPrice - product.price

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = cardBg,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(product.origin.brandColorHex))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = product.storeName.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(product.origin.accentColorHex)
                    )
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = subText, modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Product Hero Image
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isDarkTheme) Color(0xFF1C1D28) else Color(0xFFF9F9FA))
                        .border(1.dp, borderCol, RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = product.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (product.brand.isNotBlank()) {
                    Text(
                        text = product.brand.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0A84FF),
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = product.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryText
                )

                Text(
                    text = "${product.unitSize} • Category: ${product.category}",
                    fontSize = 11.sp,
                    color = subText
                )

                if (product.upc.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "UPC Barcode: ${product.upc} • Live Sourced",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0A84FF)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Row & Zero Markup Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = product.formattedPrice,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF30D158)
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0D2818))
                            .border(1.dp, Color(0xFF30D158).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "0% MARKUP GUARANTEE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF30D158)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Competitor Comparison Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) Color(0xFF1B1C2A) else Color(0xFFF2F2F7))
                        .border(1.dp, borderCol, RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Other Delivery Apps:", fontSize = 11.sp, color = subText)
                            Text(
                                text = String.format(java.util.Locale.US, "$%.2f", competitorPrice),
                                fontSize = 11.sp,
                                color = subText,
                                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Provi Shelf Price:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = primaryText)
                            Text(
                                text = product.formattedPrice,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF30D158)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎉 You save $${String.format(java.util.Locale.US, "%.2f", competitorSavings)} per item!",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (quantityInCart == 0) {
                Button(
                    onClick = {
                        onAdd()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add to Cart • ${product.formattedPrice}", fontWeight = FontWeight.Bold, color = Color.White)
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A84FF))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = onDecrement, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$quantityInCart in cart",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = onIncrement, modifier = Modifier.size(30.dp)) {
                            Icon(Icons.Default.Add, contentDescription = "Increase", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF30D158)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Done", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }
        }
    )
}
