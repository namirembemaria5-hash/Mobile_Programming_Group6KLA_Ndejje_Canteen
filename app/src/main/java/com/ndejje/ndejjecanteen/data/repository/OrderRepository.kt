package com.ndejje.ndejjecanteen.data.repository

import android.util.Log
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
            Log.e("OrderRepository", "Error placing order", e)
            Result.failure(e)
        }
    }

    fun getUserOrders(userId: String): Flow<List<Order>> = callbackFlow {
        Log.d("OrderRepository", "Fetching orders for user: $userId")
        
        // This query requires a composite index: userId (ASC), createdAt (DESC)
        val query = ordersCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrderRepository", "Primary query failed: ${error.message}")
                
                // FALLBACK: If index is missing, try a simpler query and sort locally
                if (error.message?.contains("index") == true || error.code.name == "FAILED_PRECONDITION") {
                    Log.w("OrderRepository", "Missing index. Falling back to client-side sorting.")
                    ordersCollection.whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { fallbackSnapshot ->
                            val orders = fallbackSnapshot.documents.mapNotNull { doc ->
                                try {
                                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                                } catch (e: Exception) {
                                    Log.e("OrderRepository", "Error parsing order ${doc.id}", e)
                                    null
                                }
                            }.sortedByDescending { it.createdAt }
                            Log.d("OrderRepository", "Fallback returned ${orders.size} orders")
                            trySend(orders)
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderRepository", "Fallback query failed", e)
                            trySend(emptyList())
                        }
                } else {
                    trySend(emptyList())
                }
                return@addSnapshotListener
            }
            
            val orders = snapshot?.documents?.mapNotNull { doc ->
                try {
                    doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                } catch (e: Exception) {
                    Log.e("OrderRepository", "Error parsing order ${doc.id}", e)
                    null
                }
            } ?: emptyList()
            Log.d("OrderRepository", "Primary query returned ${orders.size} orders")
            trySend(orders)
        }
        awaitClose { listener.remove() }
    }

    fun getAllOrders(): Flow<List<Order>> = callbackFlow {
        val query = ordersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("OrderRepository", "Error fetching all orders: ${error.message}")
                
                // Fallback for missing index on createdAt (though usually not required for single field)
                ordersCollection.get().addOnSuccessListener { fallbackSnapshot ->
                    val orders = fallbackSnapshot.documents.mapNotNull { doc ->
                        doc.toObject(Order::class.java)?.copy(orderId = doc.id)
                    }.sortedByDescending { it.createdAt }
                    trySend(orders)
                }
                return@addSnapshotListener
            }
            val orders = snapshot?.documents?.mapNotNull { doc ->
                doc.toObject(Order::class.java)?.copy(orderId = doc.id)
            } ?: emptyList()
            trySend(orders)
        }
        awaitClose { listener.remove() }
    }

    fun getOrderById(orderId: String): Flow<Order?> = callbackFlow {
        val listener = ordersCollection
            .document(orderId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderRepository", "Error fetching order by ID: $orderId", error)
                    trySend(null)
                    return@addSnapshotListener
                }
                val order = snapshot?.toObject(Order::class.java)?.copy(orderId = snapshot.id)
                trySend(order)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateOrderStatus(orderId: String, status: String): Boolean {
        return try {
            ordersCollection.document(orderId)
                .update(
                    mapOf(
                        "status" to status,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            true
        } catch (e: Exception) {
            Log.e("OrderRepository", "Error updating order status", e)
            false
        }
    }
}
