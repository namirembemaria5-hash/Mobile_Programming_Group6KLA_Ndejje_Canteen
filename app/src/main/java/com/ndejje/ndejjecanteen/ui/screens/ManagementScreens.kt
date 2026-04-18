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
import androidx.compose.material.icons.automirrored.filled.Logout
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
import java.util.*
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.data.model.MenuItem
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.ManagementViewModel
import com.ndejje.ndejjecanteen.utils.formatUGX

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ManagementViewModel,
    onLogout: () -> Unit
) {
    val analytics by viewModel.analytics.collectAsState()
    val allOrders by viewModel.allOrders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showOrdersDialog by remember { mutableStateOf(false) }
    var dialogTitle by remember { mutableStateOf("") }
    var filteredOrders by remember { mutableStateOf<List<Order>>(emptyList()) }

    val todayMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val weekMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
    val monthMillis = remember {
        Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1); set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(dimensionResource(R.dimen.screen_padding)), 
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_large))
            ) {
                item {
                    AnalyticsSection(
                        title = "Daily Analytics",
                        orderCount = analytics.dailyCount,
                        revenue = analytics.dailyRevenue,
                        onOrdersClick = {
                            dialogTitle = "Orders - Today"
                            filteredOrders = allOrders.filter { it.createdAt >= todayMillis }
                            showOrdersDialog = true
                        },
                        onRevenueClick = {
                            dialogTitle = "Revenue - Today"
                            filteredOrders = allOrders.filter { it.createdAt >= todayMillis }
                            showOrdersDialog = true
                        }
                    )
                }
                
                item {
                    AnalyticsSection(
                        title = "Weekly Analytics",
                        orderCount = analytics.weeklyCount,
                        revenue = analytics.weeklyRevenue,
                        onOrdersClick = {
                            dialogTitle = "Orders - This Week"
                            filteredOrders = allOrders.filter { it.createdAt >= weekMillis }
                            showOrdersDialog = true
                        },
                        onRevenueClick = {
                            dialogTitle = "Revenue - This Week"
                            filteredOrders = allOrders.filter { it.createdAt >= weekMillis }
                            showOrdersDialog = true
                        }
                    )
                }

                item {
                    AnalyticsSection(
                        title = "Monthly Analytics",
                        orderCount = analytics.monthlyCount,
                        revenue = analytics.monthlyRevenue,
                        onOrdersClick = {
                            dialogTitle = "Orders - This Month"
                            filteredOrders = allOrders.filter { it.createdAt >= monthMillis }
                            showOrdersDialog = true
                        },
                        onRevenueClick = {
                            dialogTitle = "Revenue - This Month"
                            filteredOrders = allOrders.filter { it.createdAt >= monthMillis }
                            showOrdersDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showOrdersDialog) {
        AlertDialog(
            onDismissRequest = { showOrdersDialog = false },
            title = { Text("$dialogTitle (${filteredOrders.size})") },
            text = {
                Box(modifier = Modifier.heightIn(max = 450.dp)) {
                    if (filteredOrders.isEmpty()) {
                        Text("No data found for this period.")
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(filteredOrders.sortedByDescending { it.createdAt }) { order ->
                                ManagementOrderCard(order = order, onClick = {})
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOrdersDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun AnalyticsSection(
    title: String,
    orderCount: Int,
    revenue: Double,
    onOrdersClick: () -> Unit,
    onRevenueClick: () -> Unit
) {
    Column {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Row(
            Modifier.fillMaxWidth().padding(vertical = dimensionResource(R.dimen.spacing_small)), 
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
        ) {
            StatCard(
                label = "Orders", 
                value = orderCount.toString(), 
                icon = Icons.Default.Receipt, 
                color = Color.Blue, 
                modifier = Modifier.weight(1f).clickable { onOrdersClick() }
            )
            StatCard(
                label = "Revenue", 
                value = formatUGX(revenue), 
                icon = Icons.Default.Payments, 
                color = CanteenGreen, 
                modifier = Modifier.weight(1f).clickable { onRevenueClick() }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KitchenOrdersScreen(viewModel: ManagementViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val menuItems by viewModel.menuItems.collectAsState()
    val kitchenOrders = orders.filter { it.status == OrderStatus.PENDING.name || it.status == OrderStatus.PREPARING.name }
    
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kitchen Management", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("Orders (${kitchenOrders.size})", modifier = Modifier.padding(16.dp))
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("Inventory", modifier = Modifier.padding(16.dp))
                }
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding)), 
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
                ) {
                    if (kitchenOrders.isEmpty()) {
                        item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("No active orders") } }
                    }
                    items(kitchenOrders) { order ->
                        KitchenOrderCard(order, onStatusUpdate = { viewModel.updateStatus(order.orderId, it) })
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(dimensionResource(R.dimen.screen_padding)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                ) {
                    items(menuItems) { item ->
                        InventoryItemRow(
                            item = item,
                            onToggle = { isAvailable -> viewModel.toggleItemAvailability(item.id, isAvailable) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(item: MenuItem, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
    ) {
        Row(
            modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold)
                Text(item.category, style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (item.isAvailable) "Available" else "Out of Stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (item.isAvailable) CanteenGreen else Color.Red,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Switch(
                    checked = item.isAvailable,
                    onCheckedChange = onToggle
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryOrdersScreen(viewModel: ManagementViewModel) {
    val orders by viewModel.allOrders.collectAsState()
    val deliveryOrders = orders.filter { it.status == OrderStatus.READY.name || it.status == OrderStatus.IN_TRANSIT.name }
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("Delivery Queue", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(dimensionResource(R.dimen.screen_padding)), 
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
        ) {
            if (deliveryOrders.isEmpty()) {
                item { Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) { Text("Queue is empty") } }
            }
            items(deliveryOrders) { order ->
                DeliveryOrderCard(
                    order = order,
                    onNavigate = {
                        order.location?.let {
                            val uri = Uri.parse("google.navigation:q=${it.latitude},${it.longitude}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps"))
                        }
                    },
                    onStatusUpdate = { viewModel.updateStatus(order.orderId, it) }
                )
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))) {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Icon(icon, null, tint = color)
            Text(label, style = MaterialTheme.typography.labelMedium)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ManagementOrderCard(order: Order, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))) {
        Row(Modifier.padding(dimensionResource(R.dimen.screen_padding)), verticalAlignment = Alignment.CenterVertically) {
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
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))) {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("#${order.orderId.take(8).uppercase()}", fontWeight = FontWeight.Bold)
                val statusColor = when(order.status) {
                    OrderStatus.PENDING.name -> Color.Red
                    OrderStatus.PREPARING.name -> CanteenAmber
                    else -> CanteenGreen
                }
                Text(order.status, color = statusColor, fontWeight = FontWeight.Bold)
            }
            order.items.forEach { Text("• ${it.itemName} x${it.quantity}") }
            if (order.notes.isNotBlank()) Text("Note: ${order.notes}", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            
            Row(Modifier.fillMaxWidth().padding(top = dimensionResource(R.dimen.spacing_medium)), horizontalArrangement = Arrangement.End) {
                if (order.status == OrderStatus.PENDING.name) {
                    Button(onClick = { onStatusUpdate(OrderStatus.PREPARING) }) { Text("Start Preparing") }
                } else if (order.status == OrderStatus.PREPARING.name) {
                    Button(onClick = { onStatusUpdate(OrderStatus.READY) }, colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)) { Text("Ready for Pickup") }
                }
            }
        }
    }
}

@Composable
fun DeliveryOrderCard(order: Order, onNavigate: () -> Unit, onStatusUpdate: (OrderStatus) -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large))) {
        Column(Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Order for ${order.userName}", fontWeight = FontWeight.Bold)
                Text(order.status, color = CanteenAmber, fontWeight = FontWeight.Bold)
            }
            Text(order.userPhone, style = MaterialTheme.typography.bodySmall)
            Text("Items: ${order.items.joinToString { it.itemName }}")
            
            HorizontalDivider(Modifier.padding(vertical = dimensionResource(R.dimen.spacing_small)))
            
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                OutlinedButton(onClick = onNavigate, Modifier.weight(1f)) {
                    Icon(Icons.Default.Navigation, null)
                    Spacer(Modifier.width(dimensionResource(R.dimen.spacing_extra_small)))
                    Text("Map")
                }
                
                if (order.status == OrderStatus.READY.name) {
                    Button(onClick = { onStatusUpdate(OrderStatus.IN_TRANSIT) }, Modifier.weight(1f)) {
                        Text("Start Delivery")
                    }
                } else if (order.status == OrderStatus.IN_TRANSIT.name) {
                    Button(onClick = { onStatusUpdate(OrderStatus.DELIVERED) }, Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)) {
                        Text("Mark Delivered")
                    }
                }
            }
        }
    }
}