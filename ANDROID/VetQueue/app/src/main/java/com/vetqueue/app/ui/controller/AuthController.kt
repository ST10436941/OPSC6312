package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.repository.AuthRepository
import com.vetqueue.app.data.repository.AuthResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val name: String = "",
    val errorMessage: String = ""
)

class AuthController(
    private val authRepository: AuthRepository
) {

    private val controllerJob = SupervisorJob()

    private val scope = CoroutineScope(
        controllerJob + Dispatchers.IO
    )

    private val _state = MutableStateFlow(AuthUiState())

    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun register(
        firstName: String,
        surname: String,
        email: String,
        password: String
    ) {
        if (_state.value.isLoading) return

        scope.launch {
            _state.value = AuthUiState(isLoading = true)

            try {
                when (
                    val result = authRepository.register(
                        firstName,
                        surname,
                        email,
                        password
                    )
                ) {
                    is AuthResult.Success -> {
                        _state.value = AuthUiState(
                            isLoggedIn = true,
                            userId = result.userId,
                            name = result.name
                        )
                    }

                    is AuthResult.Error -> {
                        _state.value = AuthUiState(
                            errorMessage = result.message
                        )
                    }
                }
            } catch (exception: Exception) {
                _state.value = AuthUiState(
                    errorMessage = exception.message
                        ?: "Registration failed."
                )
            }
        }
    }

    fun login(email: String, password: String) {
        if (_state.value.isLoading) return

        scope.launch {
            _state.value = AuthUiState(isLoading = true)

            try {
                when (
                    val result = authRepository.login(email, password)
                ) {
                    is AuthResult.Success -> {
                        _state.value = AuthUiState(
                            isLoggedIn = true,
                            userId = result.userId,
                            name = result.name
                        )
                    }

                    is AuthResult.Error -> {
                        _state.value = AuthUiState(
                            errorMessage = result.message
                        )
                    }
                }
            } catch (exception: Exception) {
                _state.value = AuthUiState(
                    errorMessage = exception.message
                        ?: "Login failed."
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(
            errorMessage = ""
        )
    }

    fun logout() {
        _state.value = AuthUiState()
    }

    fun close() {
        scope.cancel()
    }
}