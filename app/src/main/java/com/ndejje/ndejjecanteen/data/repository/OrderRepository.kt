package com.ndejje.ndejjecanteen.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class OrderRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val ordersCollection = firestore.collection("orders")

    suspend fun placeOrder(order: Order): Result<String> {
        return try {
            val orderId = UUID.randomUUID().toString()
            val newOrder = order.copy(orderId = orderId)
            ordersCollection.document(orderId).set(newOrder).await()
            Result.success(orderId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserOrders(userId: String): Flow<List<Order>> = callbackFlow {
        val listener = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val orders = snapshot?.documents?.mapNotNull {
                    it.toObject(Order::class.java)
                } ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCollection
            .document(orderId)
            .addSnapshotListener { snapshot, _ ->
                val order = snapshot?.toObject(Order::class.java)
                trySend(order)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
