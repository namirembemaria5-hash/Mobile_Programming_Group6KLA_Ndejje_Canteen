package com.ndejje.ndejjecanteen.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ndejje.ndejjecanteen.data.model.User
import com.ndejje.ndejjecanteen.data.repository.AuthRepository
import com.ndejje.ndejjecanteen.data.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(repository.isLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile: StateFlow<User?> = _userProfile.asStateFlow()

    val currentUserId: String? get() = repository.currentUser?.uid
    val currentUserEmail: String? get() = repository.currentUser?.email

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val user = repository.currentUser
            if (user != null) {
                _isLoggedIn.value = true
                loadUserProfile(user.uid)
            } else {
                _isLoggedIn.value = false
            }
        }
    }

    private suspend fun loadUserProfile(uid: String) {
        val profile = repository.getUserProfile(uid)
        _userProfile.value = profile
    }

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repository.signIn(email, password)) {
                is AuthResult.Success -> {
                    _isLoggedIn.value = true
                    loadUserProfile(result.user.uid)
                    _uiState.value = AuthUiState(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun signUp(email: String, password: String, name: String, phone: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            when (val result = repository.signUp(email, password, name, phone)) {
                is AuthResult.Success -> {
                    _isLoggedIn.value = true
                    loadUserProfile(result.user.uid)
                    _uiState.value = AuthUiState(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(isLoading = false, error = result.message)
                }
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _isLoggedIn.value = false
        _userProfile.value = null
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun updateProfile(name: String, phone: String) {
        val uid = currentUserId ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val success = repository.updateProfile(uid, name, phone)
            if (success) {
                loadUserProfile(uid)
                _uiState.value = _uiState.value.copy(isLoading = false, error = null)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Failed to update profile")
            }
        }
    }
}
