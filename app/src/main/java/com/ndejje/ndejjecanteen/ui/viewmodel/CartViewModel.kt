package com.ndejje.ndejjecanteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.ndejjecanteen.data.model.MenuItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int
) {
    val subtotal: Double get() = menuItem.price * quantity
}

class CartViewModel : ViewModel() {
    private val _cartItemsMap = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    
    val cartItems: StateFlow<List<CartItem>> = _cartItemsMap
        .map { it.values.toList() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    val cartItemCount: StateFlow<Int> = _cartItemsMap
        .map { it.values.sumOf { it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    val totalPrice: StateFlow<Double> = _cartItemsMap
        .map { it.values.sumOf { it.menuItem.price * it.quantity } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0.0
        )

    fun addToCart(menuItem: MenuItem) {
        val currentMap = _cartItemsMap.value.toMutableMap()
        val existingItem = currentMap[menuItem.id]
        if (existingItem != null) {
            currentMap[menuItem.id] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentMap[menuItem.id] = CartItem(menuItem, 1)
        }
        _cartItemsMap.value = currentMap
    }

    fun removeFromCart(menuItemId: String) {
        val currentMap = _cartItemsMap.value.toMutableMap()
        val existingItem = currentMap[menuItemId] ?: return
        if (existingItem.quantity > 1) {
            currentMap[menuItemId] = existingItem.copy(quantity = existingItem.quantity - 1)
        } else {
            currentMap.remove(menuItemId)
        }
        _cartItemsMap.value = currentMap
    }

    fun deleteFromCart(menuItemId: String) {
        val currentMap = _cartItemsMap.value.toMutableMap()
        currentMap.remove(menuItemId)
        _cartItemsMap.value = currentMap
    }

    fun getItemQuantity(menuItemId: String): Int {
        return _cartItemsMap.value[menuItemId]?.quantity ?: 0
    }

    fun clearCart() {
        _cartItemsMap.value = emptyMap()
    }
}
