package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.ClinicDao
import com.vetqueue.app.data.local.entity.ClinicEntity
import com.vetqueue.app.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

/** Requirement 3.4 (Veterinary Clinic Search). */
class ClinicRepository(
    private val api: ApiService,
    private val clinicDao: ClinicDao
) {
    fun observeClinics(): Flow<List<ClinicEntity>> = clinicDao.observeAll()

    suspend fun getById(clinicId: String): ClinicEntity? = clinicDao.findById(clinicId)

    fun search(query: String): Flow<List<ClinicEntity>> = clinicDao.search(query)

    /** Refreshes the local clinic cache from the REST API. Falls back to demo
     * seed data (offline) so the Clinic Search screen is never empty in the demo. */
    suspend fun refresh() {
        val remote = runCatching {
            api.getClinics().takeIf { it.isSuccessful }?.body()
        }.getOrNull()

        if (!remote.isNullOrEmpty()) {
            clinicDao.insertAll(remote.map {
                ClinicEntity(
                    clinicId = it.clinicId,
                    name = it.name,
                    address = it.address,
                    contact = it.contact,
                    hours = it.hours,
                    services = it.services.joinToString(","),
                    currentQueueCount = it.currentQueueCount,
                    estWaitMinutes = it.estWaitMinutes
                )
            })
        } else {
            seedDemoData()
        }
    }

    private suspend fun seedDemoData() {
        clinicDao.insertAll(
            listOf(
                ClinicEntity(
                    "clinic-1", "Happy Paws Vet", "123 Main St, Pretoria", "012 555 1234",
                    "Mon-Fri 08:00-17:00", "Check-up,Vaccination,Surgery", 5, 30
                ),
                ClinicEntity(
                    "clinic-2", "PetCare Clinic", "45 Church St, Pretoria", "012 555 5678",
                    "Mon-Sat 08:00-18:00", "Check-up,Grooming,Dental", 2, 15
                ),
                ClinicEntity(
                    "clinic-3", "Village Animal Hospital", "9 Oak Ave, Pretoria", "012 555 9012",
                    "Mon-Fri 07:30-16:30", "Emergency,Check-up,Surgery", 8, 45
                )
            )
        )
    }
}
