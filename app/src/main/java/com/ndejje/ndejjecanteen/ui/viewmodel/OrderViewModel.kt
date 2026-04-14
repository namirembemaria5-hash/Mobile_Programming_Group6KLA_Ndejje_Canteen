package com.ndejje.canteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.canteen.data.model.Order
import com.ndejje.canteen.data.model.OrderStatus
import com.ndejje.canteen.data.model.OrderLocation
import com.ndejje.canteen.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OrderUiState {
    object Idle : OrderUiState()
    object Loading : OrderUiState()
    data class Success(val orderId: String) : OrderUiState()
    data class Error(val message: String) : OrderUiState()
}

class OrderViewModel : ViewModel() {
    private val repository = OrderRepository()

    private val _orderUiState = MutableStateFlow<OrderUiState>(OrderUiState.Idle)
    val orderUiState: StateFlow<OrderUiState> = _orderUiState.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _currentOrder = MutableStateFlow<Order?>(null)
    val currentOrder: StateFlow<Order?> = _currentOrder.asStateFlow()

    fun placeOrder(
        userId: String,
        userName: String,
        userPhone: String,
        cartItems: List<CartItem>,
        location: OrderLocation,
        isPreOrder: Boolean,
        preOrderDate: String,
        notes: String
    ) {
        viewModelScope.launch {
            _orderUiState.value = OrderUiState.Loading
            
            val order = Order(
                userId = userId,
                userName = userName,
                userPhone = userPhone,
                items = cartItems.map { it.menuItem.id }, // Simplification for Order model
                totalPrice = cartItems.sumOf { it.menuItem.price * it.quantity },
                location = location,
                isPreOrder = isPreOrder,
                preOrderDate = preOrderDate,
                notes = notes,
                status = OrderStatus.PENDING,
                createdAt = System.currentTimeMillis()
            )

            val result = repository.placeOrder(order)
            if (result.isSuccess) {
                _orderUiState.value = OrderUiState.Success(result.getOrNull() ?: "")
            } else {
                _orderUiState.value = OrderUiState.Error(result.exceptionOrNull()?.message ?: "Failed to place order")
            }
        }
    }

    fun loadUserOrders(userId: String) {
        viewModelScope.launch {
            repository.getUserOrders(userId).collect { items ->
                _orders.value = items
            }
        }
    }

    fun loadOrderById(orderId: String) {
        viewModelScope.launch {
            repository.getOrderById(orderId).collect { order ->
                _currentOrder.value = order
            }
        }
    }

    fun resetState() {
        _orderUiState.value = OrderUiState.Idle
    }
}
