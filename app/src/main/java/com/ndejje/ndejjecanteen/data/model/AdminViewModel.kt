package com.ndejje.ndejjecanteen.data.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class AdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val ordersCollection = db.collection("orders")

    var dailyRevenue by mutableStateOf(0.0)
    var weeklyRevenue by mutableStateOf(0.0)
    var monthlyRevenue by mutableStateOf(0.0)
    var isLoading by mutableStateOf(false)

    fun calculateAnalytics() {
        isLoading = true
        val now = System.currentTimeMillis()
        val oneDay = 24 * 60 * 60 * 1000L
        val oneWeek = 7 * oneDay
        val oneMonth = 30 * oneDay

        ordersCollection.get().addOnSuccessListener { snapshot ->
            val allOrders = snapshot.toObjects(Order::class.java)

            dailyRevenue = allOrders.filter { it.createdAt > (now - oneDay) }
                .sumOf { it.totalAmount }

            weeklyRevenue = allOrders.filter { it.createdAt > (now - oneWeek) }
                .sumOf { it.totalAmount }

            monthlyRevenue = allOrders.filter { it.createdAt > (now - oneMonth) }
                .sumOf { it.totalAmount }

            isLoading = false
        }
    }
}