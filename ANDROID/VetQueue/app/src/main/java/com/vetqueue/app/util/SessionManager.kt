
package com.vetqueue.app.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SessionManager(context: Context) {

    private val preferences = context.applicationContext
        .getSharedPreferences("vetqueue_session", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "SessionManager"
        private const val USER_ID = "user_id"
        private const val USER_EMAIL = "user_email"
        private const val USER_NAME = "user_name"
        private const val LANGUAGE = "language"
        private const val NOTIFICATIONS = "notifications_enabled"
    }

    @Volatile
    var cachedUserId: String? =
        preferences.getString(USER_ID, null)
        private set

    private val _userIdFlow =
        MutableStateFlow(cachedUserId)

    private val _userNameFlow =
        MutableStateFlow(preferences.getString(USER_NAME, null))

    private val _languageFlow =
        MutableStateFlow(preferences.getString(LANGUAGE, "English") ?: "English")

    private val _notificationsFlow =
        MutableStateFlow(preferences.getBoolean(NOTIFICATIONS, true))

    val userIdFlow: StateFlow<String?> = _userIdFlow
    val userNameFlow: StateFlow<String?> = _userNameFlow
    val languageFlow: StateFlow<String> = _languageFlow
    val notificationsFlow: StateFlow<Boolean> = _notificationsFlow

    suspend fun currentUserId(): String? {
        val id = preferences.getString(USER_ID, null)
        cachedUserId = id
        _userIdFlow.value = id
        return id
    }

    suspend fun login(userId: String, email: String, name: String) {
        preferences.edit()
            .putString(USER_ID, userId)
            .putString(USER_EMAIL, email)
            .putString(USER_NAME, name)
            .apply()

        cachedUserId = userId
        _userIdFlow.value = userId
        _userNameFlow.value = name

        Log.d(TAG, "Session started for user $userId")
    }

    suspend fun updateName(name: String) {
        preferences.edit().putString(USER_NAME, name).apply()
        _userNameFlow.value = name
    }

    suspend fun logout() {
        preferences.edit()
            .remove(USER_ID)
            .remove(USER_EMAIL)
            .remove(USER_NAME)
            .apply()

        cachedUserId = null
        _userIdFlow.value = null
        _userNameFlow.value = null

        Log.d(TAG, "Session cleared")
    }

    suspend fun setLanguage(language: String) {
        preferences.edit().putString(LANGUAGE, language).apply()
        _languageFlow.value = language
        Log.d(TAG, "Language saved: $language")
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(NOTIFICATIONS, enabled).apply()
        _notificationsFlow.value = enabled
        Log.d(TAG, "Notifications enabled = $enabled")
    }
}
