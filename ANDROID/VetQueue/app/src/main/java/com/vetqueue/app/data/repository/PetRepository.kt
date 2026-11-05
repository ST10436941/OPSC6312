package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.PetDao
import com.vetqueue.app.data.local.entity.PetEntity
import com.vetqueue.app.data.remote.ApiService
import com.vetqueue.app.data.remote.dto.PetDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

/** Requirement 3.3 (Pet Management) + 3.8 (Offline Mode and Synchronisation). */
class PetRepository(
    private val api: ApiService,
    private val petDao: PetDao
) {
    fun observePets(userId: String): Flow<List<PetEntity>> = petDao.observeByUser(userId)

    /** One-shot lookup of the user's first pet - used to pre-fill quick actions like "Join Queue". */
    suspend fun getFirstPet(userId: String): PetEntity? = petDao.observeByUser(userId).first().firstOrNull()

    suspend fun addPet(
        userId: String,
        name: String,
        species: String,
        breed: String,
        dateOfBirth: String,
        gender: String,
        weight: Double,
        notes: String
    ) {
        val pet = PetEntity(
            petId = UUID.randomUUID().toString(),
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            dateOfBirth = dateOfBirth,
            gender = gender,
            weight = weight,
            notes = notes,
            syncStatus = "pending"
        )
        // Write locally first (offline-first), then try to sync immediately.
        petDao.insert(pet)
        syncPet(pet)
    }

    suspend fun updatePet(pet: PetEntity) {
        val updated = pet.copy(syncStatus = "pending")
        petDao.update(updated)
        syncPet(updated)
    }

    suspend fun deletePet(pet: PetEntity) {
        petDao.delete(pet)
        runCatching { api.deletePet(pet.petId) }
    }

    /** Pushes any pets created/edited offline up to the REST API. Call when connectivity returns. */
    suspend fun syncPending() {
        petDao.getPendingSync().forEach { syncPet(it) }
    }

    private suspend fun syncPet(pet: PetEntity) {
        val result = runCatching {
            api.createPet(pet.toDto()).takeIf { it.isSuccessful }
        }.getOrNull()
        if (result != null) {
            petDao.update(pet.copy(syncStatus = "synced"))
        }
    }

    private fun PetEntity.toDto() = PetDto(petId, userId, name, species, breed, dateOfBirth, gender, weight, notes)
}
