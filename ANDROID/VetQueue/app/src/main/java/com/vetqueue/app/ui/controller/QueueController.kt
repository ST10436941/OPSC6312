package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.QueueEntryEntity
import com.vetqueue.app.data.repository.QueueRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QueueUiState(
    val queueEntry: QueueEntryEntity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val successMessage: String = ""
)

class QueueController(
    private val queueRepository: QueueRepository
) {

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow(QueueUiState())
    val state: StateFlow<QueueUiState> = _state.asStateFlow()

    private var observationJob: Job? = null

    // Observe the user's active queue
    fun observeQueue(userId: String) {
        observationJob?.cancel()

        observationJob = scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                queueRepository.observeActiveQueue(userId)
                    .collect { entry ->

                        _state.value = _state.value.copy(
                            queueEntry = entry,
                            isLoading = false,
                            errorMessage = ""
                        )
                    }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to load your queue."
                )
            }
        }
    }

    // Join a veterinary clinic queue
    fun joinQueue(
        clinicId: String,
        userId: String,
        petId: String
    ) {
        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = "",
                successMessage = ""
            )

            try {
                val entry = queueRepository.join(
                    clinicId = clinicId,
                    userId = userId,
                    petId = petId
                )

                _state.value = _state.value.copy(
                    queueEntry = entry,
                    isLoading = false,
                    successMessage = "You have joined the queue."
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to join the queue."
                )
            }
        }
    }

    // Leave the current queue
    fun leaveQueue() {
        val entry = _state.value.queueEntry ?: return

        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = "",
                successMessage = ""
            )

            try {
                queueRepository.leave(entry)

                _state.value = _state.value.copy(
                    queueEntry = null,
                    isLoading = false,
                    successMessage = "You have left the queue."
                )

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to leave the queue."
                )
            }
        }
    }

    // Refresh the locally observed queue
    fun refreshQueue(userId: String) {
        observationJob?.cancel()

        observationJob = scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                queueRepository.observeActiveQueue(userId)
                    .collect { entry ->

                        _state.value = _state.value.copy(
                            queueEntry = entry,
                            isLoading = false,
                            errorMessage = ""
                        )
                    }

            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = e.message
                        ?: "Unable to refresh the queue."
                )
            }
        }
    }

    // Clear success and error messages
    fun clearMessages() {
        _state.value = _state.value.copy(
            errorMessage = "",
            successMessage = ""
        )
    }

    // Close controller and cancel background tasks
    fun close() {
        observationJob?.cancel()
        scope.cancel()
    }
}