package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.OrderViewModel
import com.ndejje.ndejjecanteen.utils.formatUGX
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderStatusScreen(
    orderId: String,
    orderViewModel: OrderViewModel,
    onBack: () -> Unit
) {
    val order by orderViewModel.currentOrder.collectAsState()

    LaunchedEffect(orderId) {
        orderViewModel.loadOrderById(orderId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Status", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Home, contentDescription = "Go Home")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (order == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            order?.let { ord ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status hero
                    OrderStatusHero(order = ord)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Progress tracker
                    OrderProgressTracker(currentStatus = ord.status)

                    Spacer(modifier = Modifier.height(20.dp))

                    // Order details
                    OrderDetailsCard(order = ord)
                }
            }
        }
    }
}

@Composable
fun OrderStatusHero(order: Order) {
    val status = try { OrderStatus.valueOf(order.status) } catch (e: Exception) { OrderStatus.PENDING }
    val (bgColor, emoji) = when (status) {
        OrderStatus.PENDING -> CanteenAmberContainer to "⏳"
        OrderStatus.PREPARING -> CanteenGreenContainer to "👨‍🍳"
        OrderStatus.READY -> CanteenGreen.copy(alpha = 0.15f) to "✅"
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to "❌"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 56.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                status.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = when (status) {
                    OrderStatus.PENDING -> CanteenBrown
                    OrderStatus.PREPARING -> CanteenGreen
                    OrderStatus.READY -> CanteenGreen
                    OrderStatus.CANCELLED -> MaterialTheme.error
                }
            )
            Text(
                "Order #${order.orderId.take(8).uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp)
            )

            if (status == OrderStatus.READY) {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = CanteenGreen.copy(alpha = 0.2f))
                ) {
                    Text(
                        "🎉 Your order is ready! Head to the canteen.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CanteenGreen,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderProgressTracker(currentStatus: String) {
    val status = try { OrderStatus.valueOf(currentStatus) } catch (e: Exception) { OrderStatus.PENDING }
    val steps = listOf(
        OrderStatus.PENDING to "Order Placed",
        OrderStatus.PREPARING to "Preparing",
        OrderStatus.READY to "Ready"
    )
    val currentIndex = steps.indexOfFirst { it.first == status }.coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Order Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, (stepStatus, label) ->
                    val isCompleted = index <= currentIndex && status != OrderStatus.CANCELLED
                    val isCurrent = index == currentIndex && status != OrderStatus.CANCELLED

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> CanteenGreen
                                        isCompleted -> CanteenGreenLight
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stepStatus.emoji,
                                fontSize = 18.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCompleted) CanteenGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }

                    if (index \u003c steps.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(bottom = 20.dp),
                            color = if (index \u003c currentIndex) CanteenGreenLight
                            else MaterialTheme.colorScheme.surfaceVariant,
                            thickness = 2.dp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderDetailsCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text("Order Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            order.items.forEach { lineItem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${lineItem.itemName} × ${lineItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        formatUGX(lineItem.subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(formatUGX(order.totalAmount), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = CanteenGreen)
            }
            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Note: ${order.notes}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            Text(
                "Placed: ${dateFormat.format(Date(order.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}
