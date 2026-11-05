package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.repository.AuthRepository
import com.vetqueue.app.data.repository.AuthResult
import com.vetqueue.app.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository,
    private val session: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun register(firstName: String, surname: String, email: String, password: String, confirmPassword: String) {
        val validationError = validateRegistration(firstName, surname, email, password, confirmPassword)
        if (validationError != null) {
            _uiState.value = _uiState.value.copy(errorMessage = validationError)
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.register(firstName.trim(), surname.trim(), email, password)) {
                is AuthResult.Success -> {
                    session.login(result.userId, email.trim().lowercase(), result.name)
                    _uiState.value = AuthUiState(isAuthenticated = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Please enter your email and password.")
            return
        }
        _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = repository.login(email, password)) {
                is AuthResult.Success -> {
                    session.login(result.userId, email.trim().lowercase(), result.name)
                    _uiState.value = AuthUiState(isAuthenticated = true)
                }
                is AuthResult.Error -> {
                    _uiState.value = AuthUiState(errorMessage = result.message)
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateRegistration(
        firstName: String, surname: String, email: String, password: String, confirmPassword: String
    ): String? {
        if (firstName.isBlank() || surname.isBlank()) return "First name and surname are required."
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Please enter a valid email address."
        if (password.length < 8) return "Password must be at least 8 characters."
        if (password != confirmPassword) return "Passwords do not match."
        return null
    }
}
