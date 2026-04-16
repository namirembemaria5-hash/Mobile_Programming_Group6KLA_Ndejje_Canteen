package com.ndejje.ndejjecanteen.data.model

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

class KitchenViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // Stream of active orders (Pending or Preparing)
    val activeOrders = callbackFlow<List<Order>> {
        val listener = db.collection("orders")
            .whereIn("status", listOf(OrderStatus.PENDING.name, OrderStatus.PREPARING.name))
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let { trySend(it.toObjects(Order::class.java)) }
            }
        awaitClose { listener.remove() }
    }

    fun updateStatus(orderId: String, newStatus: OrderStatus) {
        db.collection("orders").document(orderId)
            .update("status", newStatus.name, "updatedAt", System.currentTimeMillis())
    }

    // Toggle item availability (Assumes a "menu" collection exists)
    fun toggleMenuStock(itemId: String, isAvailable: Boolean) {
        db.collection("menu").document(itemId)
            .update("isAvailable", isAvailable)
    }
}