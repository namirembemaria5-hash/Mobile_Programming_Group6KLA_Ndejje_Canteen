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
import androidx.compose.ui.unit.dp

@Composable
fun KitchenOrderCard(order: Order, onStatusChange: (OrderStatus) -> Unit) {
    Card(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
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

            Row(modifier = Modifier.padding(top = 8.dp)) {
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