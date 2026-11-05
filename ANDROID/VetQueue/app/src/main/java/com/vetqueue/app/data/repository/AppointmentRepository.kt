package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.AppointmentDao
import com.vetqueue.app.data.local.entity.AppointmentEntity
import com.vetqueue.app.data.remote.ApiService
import com.vetqueue.app.data.remote.dto.AppointmentDto
import kotlinx.coroutines.flow.Flow
import java.util.UUID

sealed class BookingResult {
    data class Success(val appointment: AppointmentEntity) : BookingResult()
    data class Error(val message: String) : BookingResult()
}

/** Requirement 3.5 (Appointment Booking) - prevents double-booking of a slot. */
class AppointmentRepository(
    private val api: ApiService,
    private val appointmentDao: AppointmentDao
) {
    fun observeAppointments(userId: String): Flow<List<AppointmentEntity>> =
        appointmentDao.observeByUser(userId)

    suspend fun book(
        userId: String,
        petId: String,
        clinicId: String,
        service: String,
        date: String,
        time: String
    ): BookingResult {
        val conflict = appointmentDao.findConflict(clinicId, date, time)
        if (conflict != null) {
            return BookingResult.Error("That time slot is already booked. Please choose another time.")
        }

        val appointment = AppointmentEntity(
            appointmentId = UUID.randomUUID().toString(),
            userId = userId,
            petId = petId,
            clinicId = clinicId,
            service = service,
            date = date,
            time = time,
            status = "upcoming",
            syncStatus = "pending"
        )
        appointmentDao.insert(appointment)

        val synced = runCatching {
            api.createAppointment(
                AppointmentDto(
                    appointment.appointmentId, userId, petId, clinicId, service, date, time
                )
            ).isSuccessful
        }.getOrDefault(false)

        if (synced) {
            appointmentDao.update(appointment.copy(syncStatus = "synced"))
        }

        return BookingResult.Success(appointment)
    }

    suspend fun cancel(appointment: AppointmentEntity) {
        appointmentDao.update(appointment.copy(status = "cancelled", syncStatus = "pending"))
        runCatching { api.cancelAppointment(appointment.appointmentId) }
    }

    suspend fun syncPending() {
        appointmentDao.getPendingSync().forEach { appt ->
            val synced = runCatching {
                api.createAppointment(
                    AppointmentDto(appt.appointmentId, appt.userId, appt.petId, appt.clinicId, appt.service, appt.date, appt.time, appt.status)
                ).isSuccessful
            }.getOrDefault(false)
            if (synced) appointmentDao.update(appt.copy(syncStatus = "synced"))
        }
    }
}
