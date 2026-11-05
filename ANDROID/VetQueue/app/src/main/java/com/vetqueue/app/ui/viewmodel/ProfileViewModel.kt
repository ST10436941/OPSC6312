package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.local.entity.NotificationEntity
import com.vetqueue.app.data.repository.NotificationRepository
import com.vetqueue.app.util.SessionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.map

/** Requirement 3.2 (User Settings). */
class ProfileViewModel(private val session: SessionManager) : ViewModel() {

    val userName: StateFlow<String?> = session.userNameFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val language: StateFlow<String> = session.languageFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "English")
    val notificationsEnabled: StateFlow<Boolean> = session.notificationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    fun setLanguage(language: String) {
        viewModelScope.launch { session.setLanguage(language) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { session.setNotificationsEnabled(enabled) }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            session.logout()
            onDone()
        }
    }
}

/** Requirement 3.7 (Real-Time Notifications) - in-app inbox. */
class NotificationViewModel(
    private val repository: NotificationRepository,
    private val userId: String
) : ViewModel() {
    val notifications: StateFlow<List<NotificationEntity>> = repository.observe(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val hasUnread: StateFlow<Boolean> = notifications
        .map { list -> list.any { !it.read } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun markAllRead() {
        viewModelScope.launch { repository.markAllRead(userId) }
    }
}