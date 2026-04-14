package com.ndejje.ndejjecanteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.ndejjecanteen.data.model.MenuCategory
import com.ndejje.ndejjecanteen.data.model.MenuItem
import com.ndejje.ndejjecanteen.data.repository.MenuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MenuViewModel : ViewModel() {
    private val repository = MenuRepository()

    private val _menuItems = MutableStateFlow<List<MenuItem>>(emptyList())
    val menuItems: StateFlow<List<MenuItem>> = _menuItems.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val dailySpecials: StateFlow<List<MenuItem>> = repository.getDailySpecials()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        seedMenu()
    }

    private fun seedMenu() {
        viewModelScope.launch {
            repository.seedMenuIfEmpty()
        }
    }

    fun loadMenuByCategory(category: MenuCategory) {
        viewModelScope.launch {
            _isLoading.value = true
            repository.getMenuItemsByCategory(category).collect { items ->
                _menuItems.value = items
                _isLoading.value = false
            }
        }
    }
}
