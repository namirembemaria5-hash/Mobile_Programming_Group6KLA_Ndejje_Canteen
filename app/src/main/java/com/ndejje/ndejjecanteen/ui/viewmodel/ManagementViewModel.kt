package com.ndejje.ndejjecanteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.ndejjecanteen.data.model.MenuItem
import com.ndejje.ndejjecanteen.data.model.Order
import com.ndejje.ndejjecanteen.data.model.OrderStatus
import com.ndejje.ndejjecanteen.data.repository.MenuRepository
import com.ndejje.ndejjecanteen.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

data class OrderAnalytics(
    val dailyRevenue: Double = 0.0,
    val weeklyRevenue: Double = 0.0,
    val monthlyRevenue: Double = 0.0,
    val dailyCount: Int = 0,
    val weeklyCount: Int = 0,
    val monthlyCount: Int = 0
)

class ManagementViewModel : ViewModel() {
    private val orderRepository = OrderRepository()
    private val menuRepository = MenuRepository()

    private val _allOrders = MutableStateFlow<List<Order>>(emptyList())
    val allOrders: StateFlow<List<Order>> = _allOrders.asStateFlow()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _analytics = MutableStateFlow(OrderAnalytics())
    val analytics: StateFlow<OrderAnalytics> = _analytics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Load Orders
            launch {
                orderRepository.getAllOrders().collect { orders ->
                    _allOrders.value = orders
                    calculateAnalytics(orders)
                }
            }
            
            // Load Menu Items for Kitchen Toggle
            launch {
                menuRepository.getAllMenuItems().collect { items ->
                    _menuItems.value = items
                }
            }
            
            _isLoading.value = false
        }
    }

    private fun calculateAnalytics(orders: List<Order>) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val thisWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val thisMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val daily = orders.filter { it.createdAt >= today }
        val weekly = orders.filter { it.createdAt >= thisWeek }
        val monthly = orders.filter { it.createdAt >= thisMonth }

        _analytics.value = OrderAnalytics(
            dailyRevenue = daily.sumOf { it.totalAmount },
            weeklyRevenue = weekly.sumOf { it.totalAmount },
            monthlyRevenue = monthly.sumOf { it.totalAmount },
            dailyCount = daily.size,
            weeklyCount = weekly.size,
            monthlyCount = monthly.size
        )
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status.name)
        }
    }

    fun toggleItemAvailability(itemId: String, isAvailable: Boolean) {
        viewModelScope.launch {
            menuRepository.updateItemAvailability(itemId, isAvailable)
        }
    }
}
