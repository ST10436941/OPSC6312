package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.dao.AchievementDao
import com.vetqueue.app.data.local.entity.AchievementEntity
import com.vetqueue.app.data.local.entity.UserAchievementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AchievementUiState(
    val achievements: List<AchievementEntity> = emptyList(),
    val unlockedIds: Set<String> = emptySet(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class AchievementController(
    private val achievementDao: AchievementDao
) {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(AchievementUiState())
    val state: StateFlow<AchievementUiState> = _state.asStateFlow()

    private var achievementsJob: Job? = null
    private var unlockedJob: Job? = null

    fun loadAchievements(userId: String) {
        achievementsJob?.cancel()
        unlockedJob?.cancel()

        _state.value = _state.value.copy(
            isLoading = true,
            errorMessage = ""
        )

        achievementsJob = scope.launch {
            try {
                achievementDao.observeAll().collect { achievements ->
                    _state.value = _state.value.copy(
                        achievements = achievements,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load achievements."
                )
            }
        }

        unlockedJob = scope.launch {
            try {
                achievementDao.observeUnlockedIds(userId)
                    .collect { ids ->
                        _state.value = _state.value.copy(
                            unlockedIds = ids.toSet()
                        )
                    }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to load unlocked achievements."
                )
            }
        }
    }

    fun unlockAchievement(
        userId: String,
        achievementId: String
    ) {
        scope.launch {
            try {
                val userAchievement = UserAchievementEntity(
                    userId = userId,
                    achievementId = achievementId
                )

                achievementDao.unlock(userAchievement)

                _state.value = _state.value.copy(
                    successMessage = "Achievement unlocked!",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to unlock achievement."
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
        achievementsJob?.cancel()
        unlockedJob?.cancel()
        scope.cancel()
    }
}