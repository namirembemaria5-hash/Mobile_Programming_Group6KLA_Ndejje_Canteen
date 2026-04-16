package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.AuthViewModel
import com.ndejje.ndejjecanteen.ui.viewmodel.OrderViewModel
import com.ndejje.ndejjecanteen.utils.formatUGX
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderHistoryScreen(
    authViewModel: AuthViewModel,
    orderViewModel: OrderViewModel,
    onOrderClick: (String) -> Unit
) {
    val userOrders by orderViewModel.orders.collectAsState()
    val userId = authViewModel.currentUserId

    LaunchedEffect(userId) {
        userId?.let { orderViewModel.loadUserOrders(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📋 My Orders",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        if (userOrders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📋", fontSize = dimensionResource(R.dimen.text_size_emoji_large).value.sp)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    Text("No orders yet", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Your order history will appear here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                items(userOrders, key = { it.orderId }) { order ->
                    OrderHistoryCard(order = order, onClick = { onOrderClick(order.orderId) })
                }
            }
        }
    }
}

@Composable
fun OrderHistoryCard(order: Order, onClick: () -> Unit) {
    val status = try { OrderStatus.valueOf(order.status) } catch (e: Exception) { OrderStatus.PENDING }
    val statusColor = when (status) {
        OrderStatus.PENDING -> CanteenAmber
        OrderStatus.PREPARING -> CanteenGreenLight
        OrderStatus.READY -> CanteenGreen
        OrderStatus.IN_TRANSIT -> CanteenAmber
        OrderStatus.DELIVERED -> CanteenGreen
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
    }
    val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(dimensionResource(R.dimen.screen_padding)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "#${order.orderId.take(8).uppercase()}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_small)),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "${status.emoji} ${status.displayName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.spacing_small), vertical = dimensionResource(R.dimen.spacing_extra_small))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))
                Text(
                    "${order.items.size} item${if (order.items.size != 1) "s" else ""}: ${order.items.take(2).joinToString(", ") { it.itemName }}${if (order.items.size > 2) "..." else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        dateFormat.format(Date(order.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                    )
                    Text(
                        formatUGX(order.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = CanteenGreen
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_small))
            )
        }
    }
}
