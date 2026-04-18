package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.ui.theme.CanteenGreen

data class FAQItem(val question: String, val answer: String)
data class FAQCategory(val title: String, val items: List<FAQItem>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FAQScreen(onNavigateBack: () -> Unit) {
    val faqData = listOf(
        FAQCategory("1. Ordering & Account", listOf(
            FAQItem("How do I place an order?", "Browse menu categories, add item(s) to cart, then go to cart for delivery details and payments."),
            FAQItem("Do I need an account to order?", "You need an account to proceed to payment or view order history.")
        )),
        FAQCategory("2. Delivery", listOf(
            FAQItem("Where do you deliver?", "We mainly deliver at the campus and nearby areas."),
            FAQItem("How long does delivery take?", "In less than 30 minutes."),
            FAQItem("How much is the delivery fee?", "Depends on distance."),
            FAQItem("Can I track my order?", "Yes, we provide real-time tracking.")
        )),
        FAQCategory("3. Payment FAQs", listOf(
            FAQItem("What payment methods are accepted?", "Only mobile money."),
            FAQItem("Can I pay after delivery?", "No, cash on delivery option has been misused frequently.")
        )),
        FAQCategory("4. Order Changes, Cancellation & Refunds", listOf(
            FAQItem("Can I cancel my order?", "Yes, but only before a cutoff time or before preparation begins."),
            FAQItem("Can I edit my order after placing it?", "Only before processing starts."),
            FAQItem("What if I receive the wrong or missing items?", "You can request a refund or replacement."),
            FAQItem("Do I get a refund for delivery fees?", "No.")
        )),
        FAQCategory("5. Customer Support FAQs", listOf(
            FAQItem("How do I contact support?", "Phone: 0706430836\nEmail: canteen.ndejjeuniversity@gmail.com"),
            FAQItem("What are your operating hours?", "8AM – 8PM"),
            FAQItem("What if the delivery-person can’t find my location?", "You may be called for directions")
        ))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("FAQs", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(dimensionResource(R.dimen.screen_padding)),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(faqData) { category ->
                CategorySection(category)
            }
        }
    }
}

@Composable
fun CategorySection(category: FAQCategory) {
    Column {
        Text(
            text = category.title,
            style = MaterialTheme.typography.titleLarge,
            color = CanteenGreen,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        category.items.forEach { item ->
            FAQExpandableItem(item)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun FAQExpandableItem(item: FAQItem) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = item.question,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(
                    text = item.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
