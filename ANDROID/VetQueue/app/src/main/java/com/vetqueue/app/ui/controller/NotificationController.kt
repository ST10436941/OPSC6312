package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.NotificationEntity
import com.vetqueue.app.data.repository.NotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class NotificationController(
    private val notificationRepository: NotificationRepository
) {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(NotificationUiState())
    val state: StateFlow<NotificationUiState> = _state.asStateFlow()

    private var observationJob: Job? = null

    fun loadNotifications(userId: String) {
        observationJob?.cancel()

        observationJob = scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                notificationRepository.observe(userId).collect { notifications ->
                    _state.value = _state.value.copy(
                        notifications = notifications,
                        unreadCount = notifications.count { !it.read },
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load notifications."
                )
            }
        }
    }

    fun addNotification(
        userId: String,
        message: String,
        type: String
    ) {
        scope.launch {
            try {
                notificationRepository.add(
                    userId = userId,
                    message = message,
                    type = type
                )

                _state.value = _state.value.copy(
                    successMessage = "Notification created.",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to create notification."
                )
            }
        }
    }

    fun markAsRead(notificationId: String) {
        scope.launch {
            try {
                notificationRepository.markRead(notificationId)

                _state.value = _state.value.copy(
                    successMessage = "Notification marked as read.",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to update notification."
                )
            }
        }
    }

    fun markAllAsRead(userId: String) {
        scope.launch {
            try {
                notificationRepository.markAllRead(userId)

                _state.value = _state.value.copy(
                    successMessage = "All notifications marked as read.",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to update notifications."
                )
            }
        }
    }

    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    fun close() {
        observationJob?.cancel()
        scope.cancel()
    }
}