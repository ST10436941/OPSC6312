package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.dao.UserDao
import com.vetqueue.app.data.local.entity.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UserUiState(
    val user: UserEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class UserController(
    private val userDao: UserDao
) {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(UserUiState())
    val state: StateFlow<UserUiState> = _state.asStateFlow()

    // Load the user's profile
    fun loadProfile(userId: String) {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                val user = userDao.findById(userId)

                _state.value = _state.value.copy(
                    user = user,
                    isLoading = false,
                    errorMessage = if (user == null) {
                        "User profile not found."
                    } else {
                        ""
                    }
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load profile."
                )
            }
        }
    }

    // Change the user's preferred language
    fun updateLanguage(
        userId: String,
        language: String
    ) {
        scope.launch {
            try {
                userDao.updateLanguage(userId, language)

                val updatedUser = userDao.findById(userId)

                _state.value = _state.value.copy(
                    user = updatedUser,
                    successMessage = "Language preference updated.",
                    errorMessage = ""
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to update language."
                )
            }
        }
    }

    // Enable or disable notifications
    fun updateNotifications(
        userId: String,
        enabled: Boolean
    ) {
        scope.launch {
            try {
                userDao.updateNotifications(userId, enabled)

                val updatedUser = userDao.findById(userId)

                _state.value = _state.value.copy(
                    user = updatedUser,
                    successMessage = if (enabled) {
                        "Notifications enabled."
                    } else {
                        "Notifications disabled."
                    },
                    errorMessage = ""
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to update notification settings."
                )
            }
        }
    }

    // Clear displayed messages
    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    // Close controller
    fun close() {
        scope.cancel()
    }
}