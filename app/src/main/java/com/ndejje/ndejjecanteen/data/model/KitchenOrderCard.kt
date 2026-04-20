package com.ndejje.ndejjecanteen.data.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import com.ndejje.ndejjecanteen.R

@Composable
fun KitchenOrderCard(order: Order, onStatusChange: (OrderStatus) -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(dimensionResource(R.dimen.spacing_small))) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_large))) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Order: #${order.orderId.takeLast(5)}", fontWeight = FontWeight.Bold)
                Text(order.status)
            }

            order.items.forEach { item ->
                Text("• ${item.quantity}x ${item.itemName}")
            }

            Row(modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small))) {
                if (order.status == OrderStatus.PENDING.name) {
                    Button(onClick = { onStatusChange(OrderStatus.PREPARING) }) {
                        Text("Start Preparing")
                    }
                }
                if (order.status == OrderStatus.PREPARING.name) {
                    Button(onClick = { onStatusChange(OrderStatus.READY) }) {
                        Text("Mark Ready")
                    }
                }
            }
        }
    }
}