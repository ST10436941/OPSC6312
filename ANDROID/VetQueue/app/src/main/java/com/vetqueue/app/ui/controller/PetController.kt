package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.PetEntity
import com.vetqueue.app.data.repository.PetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PetUiState(
    val pets: List<PetEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String = ""
)

class PetController(
    private val petRepository: PetRepository
) {

    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    private val _state = MutableStateFlow(PetUiState())
    val state: StateFlow<PetUiState> = _state.asStateFlow()

    private var observationJob: Job? = null

    fun observePets(userId: String) {
        observationJob?.cancel()

        observationJob = scope.launch {
            try {
                petRepository.observePets(userId).collect { pets ->
                    _state.value = _state.value.copy(
                        pets = pets,
                        errorMessage = ""
                    )
                }
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = exception.message
                        ?: "Unable to load pets."
                )
            }
        }
    }

    fun addPet(
        userId: String,
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        gender: String,
        weight: Double,
        notes: String
    ) {
        if (_state.value.isLoading) return

        if (name.isBlank() || species.isBlank()) {
            _state.value = _state.value.copy(
                errorMessage = "Pet name and species are required."
            )
            return
        }

        if (weight < 0) {
            _state.value = _state.value.copy(
                errorMessage = "Weight cannot be negative."
            )
            return
        }

        scope.launch {
            _state.value = _state.value.copy(
                isLoading = true,
                errorMessage = ""
            )

            try {
                petRepository.addPet(
                    userId,
                    name.trim(),
                    species.trim(),
                    breed.trim(),
                    dateOfBirth,
                    gender,
                    weight,
                    notes.trim()
                )
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = exception.message
                        ?: "Unable to add pet."
                )
            } finally {
                _state.value = _state.value.copy(
                    isLoading = false
                )
            }
        }
    }

    fun deletePet(petId: String) {
        val pet = _state.value.pets.firstOrNull {
            it.petId == petId
        } ?: return

        scope.launch {
            try {
                petRepository.deletePet(pet)
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = exception.message
                        ?: "Unable to delete pet."
                )
            }
        }
    }

    fun syncPendingPets() {
        scope.launch {
            try {
                petRepository.syncPending()
            } catch (exception: Exception) {
                _state.value = _state.value.copy(
                    errorMessage = exception.message
                        ?: "Pet synchronisation failed."
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(
            errorMessage = ""
        )
    }

    fun close() {
        observationJob?.cancel()
        scope.cancel()
    }
}