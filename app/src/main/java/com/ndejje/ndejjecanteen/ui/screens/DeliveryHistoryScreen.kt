package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.ui.viewmodel.ManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeliveryHistoryScreen(
    viewModel: ManagementViewModel,
    deliveryPersonId: String? = null
) {
    val orders by viewModel.allOrders.collectAsState()
    val historyOrders = remember(orders, deliveryPersonId) {
        orders.filter { 
            it.status == OrderStatus.DELIVERED.name &&
            (deliveryPersonId == null || it.deliveryPersonId == deliveryPersonId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Delivery History", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        if (historyOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No delivered orders yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(dimensionResource(R.dimen.screen_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_medium))
            ) {
                items(historyOrders) { order ->
                    ManagementOrderCard(order = order, onClick = {})
                }
            }
        }
    }
}
