package com.ndejje.ndejjecanteen.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
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
    val context = LocalContext.current

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
                        .padding(dimensionResource(R.dimen.screen_padding)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Status hero
                    OrderStatusHero(order = ord)

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Map Section
                    ord.location?.let { location ->
                        if (location.latitude != 0.0 || location.longitude != 0.0) {
                            DeliveryMapCard(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                onOpenMap = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${location.latitude},${location.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(mapIntent)
                                }
                            )
                            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                        }
                    }

                    // Progress tracker
                    OrderProgressTracker(currentStatus = ord.status)

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    // Order details
                    OrderDetailsCard(order = ord)
                }
            }
        }
    }
}

@Composable
fun DeliveryMapCard(latitude: Double, longitude: Double, onOpenMap: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_medium))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, tint = CanteenGreen)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text("Delivery Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            
            // Static Map Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.box_size_status))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.radius_medium)))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(dimensionResource(R.dimen.icon_size_large)))
                    Text("Location Captured", style = MaterialTheme.typography.bodySmall)
                    Text(String.format(Locale.getDefault(), "%.4f, %.4f", latitude, longitude),
                         style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_padding)))
            
            Button(
                onClick = onOpenMap,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
            ) {
                Icon(Icons.Default.Navigation, contentDescription = null)
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                Text("Open Navigation")
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
        OrderStatus.IN_TRANSIT -> CanteenAmberContainer to "🚚"
        OrderStatus.DELIVERED -> CanteenGreenContainer to "🏁"
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to "❌"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_medium))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.screen_padding_extra_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = dimensionResource(R.dimen.text_size_emoji_large).value.sp)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            Text(
                status.displayName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = when (status) {
                    OrderStatus.PENDING -> CanteenBrown
                    OrderStatus.PREPARING -> CanteenGreen
                    OrderStatus.READY -> CanteenGreen
                    OrderStatus.IN_TRANSIT -> CanteenBrown
                    OrderStatus.DELIVERED -> CanteenGreen
                    OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
                }
            )
            Text(
                "Order #${order.orderId.take(8).uppercase()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_extra_small))
            )

            if (status == OrderStatus.READY) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Card(
                    shape = RoundedCornerShape(dimensionResource(R.dimen.spacing_small)),
                    colors = CardDefaults.cardColors(containerColor = CanteenGreen.copy(alpha = 0.2f))
                ) {
                    Text(
                        "🎉 Your order is ready! Head to the canteen.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CanteenGreen,
                        modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
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
        OrderStatus.READY to "Ready",
        OrderStatus.IN_TRANSIT to "In Transit",
        OrderStatus.DELIVERED to "Delivered"
    )
    val currentIndex = steps.indexOfFirst { it.first == status }.coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_small))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding_large))) {
            Text("Order Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_padding)))
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
                                .size(dimensionResource(R.dimen.icon_size_large))
                                .clip(CircleShape)
                                .background(
                                    if (isCurrent) CanteenGreen
                                    else if (isCompleted) CanteenGreenLight
                                    else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                stepStatus.emoji,
                                fontSize = dimensionResource(R.dimen.text_size_medium).value.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_small)))
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCompleted) CanteenGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                        )
                    }

                    if (index < steps.size - 1) {
                        HorizontalDivider(
                            modifier = Modifier
                                .weight(0.5f)
                                .padding(bottom = dimensionResource(R.dimen.spacing_extra_large)),
                            color = if (index < currentIndex) CanteenGreenLight
                            else MaterialTheme.colorScheme.surfaceVariant,
                            thickness = dimensionResource(R.dimen.border_width_thick)
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
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
        elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_small))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding_large))) {
            Text("Order Items", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            order.items.forEach { lineItem ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacing_extra_small)),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${lineItem.itemName} x ${lineItem.quantity}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        formatUGX(lineItem.subtotal),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_small)))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                Text(formatUGX(order.totalAmount), style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold, color = CanteenGreen)
            }
            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                Text("Note: ${order.notes}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            Text(
                "Placed: ${dateFormat.format(Date(order.createdAt))}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}
