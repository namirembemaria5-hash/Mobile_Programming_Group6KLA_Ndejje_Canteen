package com.ndejje.ndejjecanteen.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.data.model.PaymentMethod
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.ManagementViewModel
import com.ndejje.ndejjecanteen.utils.formatUGX
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(viewModel: ManagementViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                item {
                    Text("Order Statistics", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Total", orders.size.toString(), Icons.Default.Receipt, Color.Blue, Modifier.weight(1f))
                        StatCard("Pending", orders.count { it.status == OrderStatus.PENDING.name }.toString(), Icons.Default.Timer, CanteenAmber, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatCard("Revenue", formatUGX(orders.sumOf { it.totalAmount }), Icons.Default.Payments, CanteenGreen, Modifier.weight(1f))
                    }
                }
                item { Text("Recent Activity", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
                items(orders.take(10)) { order -> ManagementOrderCard(order) { /* Navigate to detail */ } }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenOrdersScreen(viewModel: ManagementViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val kitchenOrders = orders.filter { it.status == OrderStatus.PENDING.name || it.status == OrderStatus.PREPARING.name }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Kitchen Orders", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(kitchenOrders) { order ->
                KitchenOrderCard(order, onStatusUpdate = { viewModel.updateStatus(order.orderId, it) })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOrdersScreen(viewModel: ManagementViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val deliveryOrders = orders.filter { it.status == OrderStatus.READY.name }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Delivery Queue", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(deliveryOrders) { order ->
                DeliveryOrderCard(
                    order = order,
                    onNavigate = {
                        order.location?.let {
                            val uri = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps"))
                        }
                    },
                    onComplete = { viewModel.updateStatus(order.orderId, OrderStatus.READY) } // Assuming ready is a state, maybe add COMPLETED
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = color)
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManagementOrderCard(order: Order, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(12.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("#${order.orderId.take(8).uppercase()}", fontWeight = FontWeight.Bold)
                Text(order.userName, style = MaterialTheme.typography.bodySmall)
            }
            Text(order.status, color = CanteenGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun KitchenOrderCard(order: Order, onStatusUpdate: (OrderStatus) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#${order.orderId.take(8).uppercase()}", fontWeight = FontWeight.Bold)
                Text(if (order.status == OrderStatus.PENDING.name) "NEW" else "PREPARING", 
                    color = if (order.status == OrderStatus.PENDING.name) Color.Red else CanteenAmber)
            }
            order.items.forEach { Text("• ${it.itemName} x${it.quantity}") }
            if (order.notes.isNotBlank()) Text("Note: ${order.notes}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            
            Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.End) {
                if (order.status == OrderStatus.PENDING.name) {
                    Button(onClick = { onStatusUpdate(OrderStatus.PREPARING) }) { Text("Start Preparing") }
                } else {
                    Button(onClick = { onStatusUpdate(OrderStatus.READY) }, colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)) { Text("Ready for Pickup") }
                }
            }
        }
    }
}

@Composable
fun DeliveryOrderCard(order: Order, onNavigate: () -> Unit, onComplete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text("Order for ${order.userName}", fontWeight = FontWeight.Bold)
            Text(order.userPhone, style = MaterialTheme.typography.bodySmall)
            Text("Items: ${order.items.joinToString { it.itemName }}")
            
            Divider(Modifier.padding(vertical = 8.dp))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onNavigate, Modifier.weight(1f)) {
                    Icon(Icons.Default.Navigation, null)
                    Spacer(Modifier.width(4.dp))
                    Text("Map")
                }
                Button(onClick = onComplete, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)) {
                    Text("Mark Delivered")
                }
            }
        }
    }
}
