package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.ui.theme.CanteenGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(onNavigateBack: () -> Unit) {
    val faqCategories = listOf(
        FAQCategory(
            "1. Ordering & Account",
            listOf(
                FAQItem("How do I place an order?", "Browse menu categories, add item(s) to cart, then go to cart for delivery details and payments."),
                FAQItem("Do I need an account to order?", "You need an account to proceed to payment or view order history.")
            )
        ),
        FAQCategory(
            "2. Delivery",
            listOf(
                FAQItem("Where do you deliver?", "We mainly deliver at the campus and nearby areas."),
                FAQItem("How long does delivery take?", "In less than 30 minutes."),
                FAQItem("How much is the delivery fee?", "Depends on distance."),
                FAQItem("Can I track my order?", "Yes, we provide real-time tracking.")
            )
        ),
        FAQCategory(
            "3. Payment FAQs",
            listOf(
                FAQItem("What payment methods are accepted?", "Only mobile money."),
                FAQItem("Can I pay after delivery?", "No, only prepayments via mobile money are accepted.")
            )
        ),
        FAQCategory(
            "4. Order Changes, Cancellation & Refunds",
            listOf(
                FAQItem("Can I cancel my order?", "Yes, but only before a cutoff time or before preparation begins."),
                FAQItem("Can I edit my order after placing it?", "Only before processing starts."),
                FAQItem("What if I receive the wrong or missing items?", "You can request a refund or replacement."),
                FAQItem("Do I get a refund for delivery fees?", "No.")
            )
        ),
        FAQCategory(
            "5. Customer Support FAQs",
            listOf(
                FAQItem("How do I contact support?", "Phone: 0706430836\nEmail: canteen.ndejjeuniversity@gmail.com"),
                FAQItem("What are your operating hours?", "8AM – 8AM"),
                FAQItem("What if the delivery-person can’t find my location?", "You may be called for directions")
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Frequently Asked Questions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = CanteenGreen
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = dimensionResource(R.dimen.screen_padding)),
            contentPadding = PaddingValues(vertical = dimensionResource(R.dimen.screen_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
        ) {
            faqCategories.forEach { category ->
                item {
                    Text(
                        text = category.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = CanteenGreen,
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_small))
                    )
                }
                items(category.items) { faq ->
                    FAQCard(faq)
                }
            }
        }
    }
}

@Composable
fun FAQCard(faq: FAQItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_small)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Text(
                text = faq.question,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                text = faq.answer,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

data class FAQCategory(val title: String, val items: List<FAQItem>)
data class FAQItem(val question: String, val answer: String)
