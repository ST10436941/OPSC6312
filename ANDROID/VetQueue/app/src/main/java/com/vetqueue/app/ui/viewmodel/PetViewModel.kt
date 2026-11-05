package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.local.entity.PetEntity
import com.vetqueue.app.data.repository.PetRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PetViewModel(
    private val repository: PetRepository,
    private val userId: String
) : ViewModel() {

    val pets: StateFlow<List<PetEntity>> = repository.observePets(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addPet(name: String, species: String, breed: String, dob: String, gender: String, weight: Double, notes: String) {
        if (name.isBlank() || species.isBlank()) return
        viewModelScope.launch {
            repository.addPet(userId, name.trim(), species.trim(), breed.trim(), dob.trim(), gender.trim(), weight, notes.trim())
        }
    }

    fun deletePet(pet: PetEntity) {
        viewModelScope.launch { repository.deletePet(pet) }
    }
}
