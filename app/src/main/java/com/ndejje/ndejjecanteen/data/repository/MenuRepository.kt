package com.ndejje.ndejjecanteen.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.ndejje.ndejjecanteen.data.model.DefaultMenuItems
import com.ndejje.ndejjecanteen.data.model.MenuCategory
import com.ndejje.ndejjecanteen.data.model.MenuItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class MenuRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val menuCollection = firestore.collection("menu_items")

    suspend fun seedMenuIfEmpty() {
        try {
            val snapshot = menuCollection.limit(1).get().await()
            if (snapshot.isEmpty) {
                val batch = firestore.batch()
                DefaultMenuItems.allItems().forEach { item ->
                    val ref = menuCollection.document(item.id)
                    batch.set(ref, item)
                }
                batch.commit().await()
            }
        } catch (_: Exception) {}
    }

    fun getAllMenuItems(): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val items = snapshot?.documents?.mapNotNull {
                it.toObject(MenuItem::class.java)
            } ?: emptyList()
            trySend(items)
        }
        awaitClose { listener.remove() }
    }

    fun getMenuItemsByCategory(category: MenuCategory): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuCollection
            .whereEqualTo("category", category.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val items = snapshot?.documents?.mapNotNull {
                    it.toObject(MenuItem::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    fun getDailySpecials(): Flow<List<MenuItem>> = callbackFlow {
        val listener = menuCollection
            .whereEqualTo("isAvailable", true)
            .limit(5)
            .addSnapshotListener { snapshot, _ ->
                val items = snapshot?.documents?.mapNotNull {
                    it.toObject(MenuItem::class.java)
                } ?: emptyList()
                trySend(items)
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateItemAvailability(itemId: String, isAvailable: Boolean): Boolean {
        return try {
            menuCollection.document(itemId)
                .update("isAvailable", isAvailable)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addMenuItem(item: MenuItem): Boolean {
        return try {
            val id = if (item.id.isEmpty()) menuCollection.document().id else item.id
            val finalItem = item.copy(id = id)
            menuCollection.document(id).set(finalItem).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun updateMenuItem(item: MenuItem): Boolean {
        return try {
            menuCollection.document(item.id).set(item).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteMenuItem(itemId: String): Boolean {
        return try {
            menuCollection.document(itemId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
