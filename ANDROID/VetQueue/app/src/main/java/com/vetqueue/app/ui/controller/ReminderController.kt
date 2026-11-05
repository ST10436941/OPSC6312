package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.ReminderEntity
import com.vetqueue.app.data.repository.ReminderRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReminderUiState(
    val reminders: List<ReminderEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class ReminderController(
    private val reminderRepository: ReminderRepository
) {
    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(ReminderUiState())
    val state: StateFlow<ReminderUiState> = _state.asStateFlow()

    private var observationJob: Job? = null

    fun loadReminders(userId: String) {
        observationJob?.cancel()

        observationJob = scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                reminderRepository.observe(userId).collect { reminders ->
                    _state.value = _state.value.copy(
                        reminders = reminders,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load reminders."
                )
            }
        }
    }

    fun addReminder(
        userId: String,
        petId: String,
        type: String,
        dueDate: String
    ) {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = "",
                successMessage = ""
            )

            try {
                reminderRepository.add(
                    userId = userId,
                    petId = petId,
                    type = type,
                    dueDate = dueDate
                )

                _state.value = _state.value.copy(
                    isLoading = false,
                    successMessage = "Reminder added successfully."
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to add reminder."
                )
            }
        }
    }

    fun markReminderDone(reminder: ReminderEntity) {
        scope.launch {
            try {
                reminderRepository.markDone(reminder)

                _state.value = _state.value.copy(
                    successMessage = "Reminder marked as completed.",
                    errorMessage = ""
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = e.message
                        ?: "Unable to update reminder."
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