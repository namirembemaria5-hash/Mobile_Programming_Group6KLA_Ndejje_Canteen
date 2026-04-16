package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.data.model.MenuCategory
import com.ndejje.ndejjecanteen.data.model.MenuItem
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.AuthViewModel
import com.ndejje.ndejjecanteen.ui.viewmodel.MenuViewModel
import com.ndejje.ndejjecanteen.utils.formatUGX
import java.util.Calendar

data class CategoryCardData(
    val category: MenuCategory,
    val emoji: String,
    val color: Color,
    val description: String
)

val categoryCards = listOf(
    CategoryCardData(MenuCategory.SNACKS, "🥟", SnackColor, "Quick bites"),
    CategoryCardData(MenuCategory.DRINKS, "🥤", DrinkColor, "Stay hydrated"),
    CategoryCardData(MenuCategory.TEA_COFFEE, "☕", TeaColor, "Hot beverages"),
    CategoryCardData(MenuCategory.BUFFET, "🍛", BuffetColor, "Full meals"),
    CategoryCardData(MenuCategory.SPECIAL_ORDERS, "🔥", SpecialColor, "Weekend specials")
)

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    menuViewModel: MenuViewModel,
    onCategoryClick: (String) -> Unit
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val dailySpecials by menuViewModel.dailySpecials.collectAsState()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        // Top header with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CanteenGreen, CanteenGreenLight)
                    )
                )
                .padding(
                    horizontal = dimensionResource(R.dimen.screen_padding_large),
                    vertical = dimensionResource(R.dimen.spacing_extra_large)
                )
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$greeting,",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Text(
                            text = userProfile?.name?.split(" ")?.firstOrNull() ?: "Student",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "🏫 Ndejje Guild Canteen",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(dimensionResource(R.dimen.box_size_large))
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍽️", fontSize = dimensionResource(R.dimen.text_size_heading_large).value.sp)
                    }
                }
            }
        }

        // Daily Specials Banner
        if (dailySpecials.isNotEmpty()) {
            DailySpecialsBanner(specials = dailySpecials)
        }

        // Section: Menu Categories
        Text(
            text = "What are you craving?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.screen_padding_large),
                top = dimensionResource(R.dimen.spacing_extra_large),
                bottom = dimensionResource(R.dimen.spacing_extra_small)
            )
        )
        Text(
            text = "Browse our menu categories",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
            modifier = Modifier.padding(
                start = dimensionResource(R.dimen.screen_padding_large),
                bottom = dimensionResource(R.dimen.spacing_large)
            )
        )

        // Category grid — 2 columns
        Column(
            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.screen_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            categoryCards.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                ) {
                    rowItems.forEach { card ->
                        CategoryCard(
                            cardData = card,
                            modifier = Modifier.weight(1f),
                            onClick = { onCategoryClick(card.category.name) }
                        )
                    }
                    // Fill remaining space if odd number
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Featured snacks
        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = dimensionResource(R.dimen.screen_padding_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, contentDescription = null,
                tint = CanteenAmber, modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small)))
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = "Student Favourites",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        LazyRow(
            contentPadding = PaddingValues(
                horizontal = dimensionResource(R.dimen.screen_padding),
                vertical = dimensionResource(R.dimen.spacing_medium)
            ),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            val favourites = listOf(
                Triple("Kikomando", "🫓", "UGX 2,000"),
                Triple("Mandaazi", "🍩", "UGX 500"),
                Triple("African Tea", "🍵", "UGX 1,000"),
                Triple("Samosas", "🥟", "UGX 1,000"),
                Triple("Rice + Beans", "🍚", "UGX 3,500")
            )
            items(favourites) { (name, emoji, price) ->
                FavouriteItemCard(name = name, emoji = emoji, price = price)
            }
        }

        // Weekend notice for Lusaniya
        val isWeekend = remember {
            val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }
        if (isWeekend) {
            WeekendSpecialBanner(onClick = { onCategoryClick(MenuCategory.SPECIAL_ORDERS.name) })
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_huge)))
    }
}

@Composable
fun CategoryCard(
    cardData: CategoryCardData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .aspectRatio(1.1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardData.color.copy(alpha = 0.12f)
        ),
        border = BorderStroke(1.5.dp, cardData.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(dimensionResource(R.dimen.spacing_large)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.box_size_large))
                    .clip(CircleShape)
                    .background(cardData.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(cardData.emoji, fontSize = dimensionResource(R.dimen.text_size_display).value.sp)
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = cardData.category.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = cardData.color
            )
            Text(
                text = cardData.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
    }
}

@Composable
fun DailySpecialsBanner(specials: List<MenuItem>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.screen_padding),
                vertical = dimensionResource(R.dimen.spacing_medium)
            ),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        colors = CardDefaults.cardColors(containerColor = CanteenAmberContainer),
        border = BorderStroke(1.5.dp, CanteenAmber.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⭐", fontSize = dimensionResource(R.dimen.text_size_heading_small).value.sp)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text(
                    "Today's Specials",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = CanteenBrown
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            specials.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.spacing_extra_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "• ${item.name}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CanteenBrown
                    )
                    Text(
                        formatUGX(item.price),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CanteenGreen
                    )
                }
            }
        }
    }
}

@Composable
fun FavouriteItemCard(name: String, emoji: String, price: String) {
    Card(
        modifier = Modifier.width(120.dp),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        elevation = CardDefaults.cardElevation(3.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(R.dimen.radius_button)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = dimensionResource(R.dimen.box_size_large).value.sp)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(name, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(price, style = MaterialTheme.typography.labelSmall, color = CanteenGreen)
        }
    }
}

@Composable
fun WeekendSpecialBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = dimensionResource(R.dimen.screen_padding),
                vertical = dimensionResource(R.dimen.spacing_small)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        colors = CardDefaults.cardColors(containerColor = SpecialColor.copy(alpha = 0.1f)),
        border = BorderStroke(2.dp, SpecialColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔥", fontSize = dimensionResource(R.dimen.text_size_display).value.sp)
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Weekend Special — Lusaniya!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SpecialColor
                )
                Text(
                    "Available today only. Tap to pre-order now!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
                )
            }
            Icon(
                Icons.Default.Restaurant,
                contentDescription = null,
                tint = SpecialColor,
                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small))
            )
        }
    }
}
