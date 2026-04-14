package com.ndejje.ndejjecanteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.ndejjecanteen.data.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val subtotal: Double get() = menuItem.price * quantity
}

class CartViewModel : ViewModel() {
    private val _cartItems = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    
    val cartItems: StateFlow<List<CartItem>> = _cartItems
        .map { it.values.toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartItemCount: StateFlow<Int> = _cartItems
        .map { it.values.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalPrice: StateFlow<Double> = _cartItems
        .map { it.values.sumOf { it.menuItem.price * it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun addToCart(menuItem: MenuItem) {
        val currentMap = _cartItems.value.toMutableMap()
        val existingItem = currentMap[menuItem.id]
        if (existingItem != null) {
            currentMap[menuItem.id] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentMap[menuItem.id] = CartItem(menuItem, 1)
        }
        _cartItems.value = currentMap
    }

    fun removeFromCart(menuItemId: String) {
        val currentMap = _cartItems.value.toMutableMap()
        val existingItem = currentMap[menuItemId] ?: return
        if (existingItem.quantity > 1) {
            currentMap[menuItemId] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            currentMap.remove(menuItemId)
        }
        _cartItems.value = currentMap
    }

    fun deleteFromCart(menuItemId: String) {
        val currentMap = _cartItems.value.toMutableMap()
        currentMap.remove(menuItemId)
        _cartItems.value = currentMap
    }

    fun getItemQuantity(menuItemId: String): Int {
        return _cartItems.value[menuItemId]?.quantity ?: 0
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
    }
}
