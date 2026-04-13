package com.ndejje.canteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.canteen.data.model.MenuCategory
import com.ndejje.canteen.data.model.MenuItem
import com.ndejje.canteen.ui.theme.*
import com.ndejje.canteen.ui.viewmodel.CartViewModel
import com.ndejje.canteen.ui.viewmodel.MenuViewModel
import com.ndejje.canteen.utils.formatUGX
import com.ndejje.canteen.utils.isWeekend
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen(
    category: String,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    onBack: () -> Unit
) {
    val menuCategory = remember { MenuCategory.valueOf(category) }
    val menuItems by menuViewModel.menuItems.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()
    val todayIsWeekend = remember { isWeekend() }

    // Filter for special orders: show Lusaniya any time, but mark weekend-only
    val displayItems = remember(menuItems) {
        if (menuCategory == MenuCategory.SPECIAL_ORDERS) {
            menuItems
        } else {
            menuItems.filter { !it.isWeekendOnly }
        }
    }

    LaunchedEffect(menuCategory) {
        menuViewModel.loadMenuByCategory(menuCategory)
    }

    // Group buffet items
    val mainItems = remember(displayItems) { displayItems.filter { it.subCategory == "main" || (!it.isSauceOption && menuCategory == MenuCategory.BUFFET) } }
    val sauceItems = remember(displayItems) { displayItems.filter { it.isSauceOption } }

    // For drinks, group by sub-category
    val drinkGroups = remember(displayItems) {
        if (menuCategory == MenuCategory.DRINKS) {
            displayItems.groupBy { it.subCategory }
        } else emptyMap()
    }

    val cardData = categoryCards.find { it.category == menuCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cardData?.emoji ?: "🍽️", fontSize = 22.sp,
                            modifier = Modifier.padding(end = 8.dp))
                        Text(
                            menuCategory.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = cardData?.color?.copy(alpha = 0.15f)
                        ?: MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (displayItems.isEmpty()) {
                EmptyMenuState(categoryName = menuCategory.displayName)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Special weekday notice for Special Orders
                    if (menuCategory == MenuCategory.SPECIAL_ORDERS && !todayIsWeekend) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = CanteenAmberContainer
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CanteenAmber)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("📅", fontSize = 22.sp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "Pre-Order Available!",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = CanteenBrown
                                        )
                                        Text(
                                            "Lusaniya is a weekend special. Pre-order now and pick up on Saturday or Sunday.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = CanteenBrown.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    when (menuCategory) {
                        MenuCategory.DRINKS -> {
                            val groupOrder = listOf("energy", "water", "soda", "juice")
                            val groupLabels = mapOf(
                                "energy" to "⚡ Energy Drinks",
                                "water" to "💧 Water",
                                "soda" to "🥤 Sodas",
                                "juice" to "🍹 Juices"
                            )
                            groupOrder.forEach { key ->
                                val group = drinkGroups[key] ?: return@forEach
                                item {
                                    Text(
                                        groupLabels[key] ?: key,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                        color = DrinkColor
                                    )
                                }
                                items(group, key = { it.id }) { item ->
                                    MenuItemCard(
                                        item = item,
                                        cartViewModel = cartViewModel,
                                        accentColor = DrinkColor,
                                        isWeekendOnly = item.isWeekendOnly,
                                        todayIsWeekend = todayIsWeekend
                                    )
                                }
                            }
                        }

                        MenuCategory.BUFFET -> {
                            if (mainItems.isNotEmpty()) {
                                item {
                                    Text(
                                        "🍚 Choose Your Main",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                        color = BuffetColor
                                    )
                                }
                                items(mainItems, key = { it.id }) { item ->
                                    MenuItemCard(item, cartViewModel, accentColor = BuffetColor,
                                        isWeekendOnly = false, todayIsWeekend = todayIsWeekend)
                                }
                            }
                            if (sauceItems.isNotEmpty()) {
                                item {
                                    Text(
                                        "🥘 Add a Sauce / Side",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                                        color = BuffetColor
                                    )
                                }
                                items(sauceItems, key = { it.id }) { item ->
                                    MenuItemCard(item, cartViewModel, accentColor = CanteenAmber,
                                        isWeekendOnly = false, todayIsWeekend = todayIsWeekend)
                                }
                            }
                        }

                        else -> {
                            items(displayItems, key = { it.id }) { item ->
                                MenuItemCard(
                                    item = item,
                                    cartViewModel = cartViewModel,
                                    accentColor = cardData?.color ?: CanteenGreen,
                                    isWeekendOnly = item.isWeekendOnly,
                                    todayIsWeekend = todayIsWeekend
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuItemCard(
    item: MenuItem,
    cartViewModel: CartViewModel,
    accentColor: Color,
    isWeekendOnly: Boolean,
    todayIsWeekend: Boolean
) {
    val quantity = cartViewModel.getItemQuantity(item.id)
    val isUnavailable = isWeekendOnly && !todayIsWeekend && !item.isWeekendOnly.not()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnavailable)
                MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(getItemEmoji(item), fontSize = 26.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (item.isWeekendOnly) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SpecialColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                "Weekend",
                                style = MaterialTheme.typography.labelSmall,
                                color = SpecialColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                if (item.description.isNotBlank()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                        maxLines = 2,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = formatUGX(item.price),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Quantity controls
            if (!isUnavailable) {
                if (quantity == 0) {
                    FilledTonalButton(
                        onClick = { cartViewModel.addToCart(item) },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = accentColor.copy(alpha = 0.15f),
                            contentColor = accentColor
                        )
                    ) {
                        Text("Add", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilledIconButton(
                            onClick = { cartViewModel.removeFromCart(item.id) },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor.copy(alpha = 0.15f),
                                contentColor = accentColor
                            )
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease",
                                modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = quantity.toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = accentColor
                        )
                        FilledIconButton(
                            onClick = { cartViewModel.addToCart(item) },
                            modifier = Modifier.size(32.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = accentColor,
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase",
                                modifier = Modifier.size(16.dp))
                        }
                    }
                }
            } else {
                Text(
                    "Weekends\nonly",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun EmptyMenuState(categoryName: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍽️", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No $categoryName items yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Check back soon!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            )
        }
    }
}

fun getItemEmoji(item: MenuItem): String {
    return when {
        item.name.contains("Mandaazi", ignoreCase = true) -> "🍩"
        item.name.contains("Samosa", ignoreCase = true) -> "🥟"
        item.name.contains("Cassava", ignoreCase = true) -> "🍟"
        item.name.contains("Kikomando", ignoreCase = true) -> "🫓"
        item.name.contains("Water", ignoreCase = true) -> "💧"
        item.name.contains("Energy", ignoreCase = true) || item.name.contains("Bull", ignoreCase = true) || item.name.contains("Monster", ignoreCase = true) || item.name.contains("Sting", ignoreCase = true) -> "⚡"
        item.name.contains("Juice", ignoreCase = true) -> "🍹"
        item.name.contains("Soda", ignoreCase = true) || item.name.contains("Cola", ignoreCase = true) || item.name.contains("Pepsi", ignoreCase = true) || item.name.contains("Mirinda", ignoreCase = true) || item.name.contains("Sprite", ignoreCase = true) -> "🥤"
        item.name.contains("Coffee", ignoreCase = true) -> "☕"
        item.name.contains("Tea", ignoreCase = true) -> "🍵"
        item.name.contains("Rice", ignoreCase = true) -> "🍚"
        item.name.contains("Matooke", ignoreCase = true) -> "🍌"
        item.name.contains("Posho", ignoreCase = true) -> "🫓"
        item.name.contains("Chapati", ignoreCase = true) -> "🫓"
        item.name.contains("Beef", ignoreCase = true) -> "🥩"
        item.name.contains("Chicken", ignoreCase = true) -> "🍗"
        item.name.contains("Beans", ignoreCase = true) -> "🫘"
        item.name.contains("Egg", ignoreCase = true) -> "🍳"
        item.name.contains("Lusaniya", ignoreCase = true) -> "🔥"
        item.name.contains("Pasta", ignoreCase = true) -> "🍝"
        else -> "🍽️"
    }
}