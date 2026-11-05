package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.AppointmentEntity
import com.vetqueue.app.data.repository.AppointmentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppointmentController(appointmentRepository: AppointmentRepository) {

    private val _appointments =
        MutableStateFlow<List<AppointmentEntity>>(emptyList())

    val appointments: StateFlow<List<AppointmentEntity>> =
        _appointments.asStateFlow()

    fun setAppointments(list: List<AppointmentEntity>) {
        _appointments.value = list
    }

    fun addAppointment(appointment: AppointmentEntity) {
        _appointments.value =
            _appointments.value + appointment
    }

    fun updateAppointment(appointment: AppointmentEntity) {
        _appointments.value = _appointments.value.map {
            if (it.appointmentId == appointment.appointmentId) {
                appointment
            } else {
                it
            }
        }
    }

    fun cancelAppointment(appointmentId: String) {
        _appointments.value = _appointments.value.map {
            if (it.appointmentId == appointmentId) {
                it.copy(status = "cancelled")
            } else {
                it
            }
        }
    }
}