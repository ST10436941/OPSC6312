package com.vetqueue.app.data.remote.dto

// These DTOs mirror the "Important API Endpoints" table in the Part 1 document
// (Section 5). Field names match what the hosted REST API is expected to
// accept/return as JSON. Update them if your actual hosted API schema differs.

data class RegisterRequest(
    val firstName: String,
    val surname: String,
    val email: String,
    val passwordHash: String
)

data class LoginRequest(
    val email: String,
    val passwordHash: String
)

data class AuthResponse(
    val userId: String,
    val firstName: String,
    val surname: String,
    val email: String,
    val token: String? = null
)

data class PetDto(
    val petId: String,
    val userId: String,
    val name: String,
    val species: String,
    val breed: String? = "",
    val dateOfBirth: String? = "",
    val gender: String? = "",
    val weight: Double? = 0.0,
    val notes: String? = ""
)

data class ClinicDto(
    val clinicId: String,
    val name: String,
    val address: String,
    val contact: String,
    val hours: String,
    val services: List<String> = emptyList(),
    val currentQueueCount: Int = 0,
    val estWaitMinutes: Int = 0
)

data class AppointmentDto(
    val appointmentId: String,
    val userId: String,
    val petId: String,
    val clinicId: String,
    val service: String,
    val date: String,
    val time: String,
    val status: String = "upcoming"
)

data class QueueJoinRequest(
    val clinicId: String,
    val userId: String,
    val petId: String
)

data class QueueEntryDto(
    val queueId: String,
    val clinicId: String,
    val userId: String,
    val petId: String,
    val queueNumber: Int,
    val position: Int,
    val estWaitMinutes: Int,
    val status: String
)

data class ReminderDto(
    val reminderId: String,
    val userId: String,
    val petId: String,
    val type: String,
    val dueDate: String,
    val status: String = "pending"
)

data class NotificationDto(
    val notificationId: String,
    val userId: String,
    val message: String,
    val type: String,
    val read: Boolean = false,
    val createdAt: Long = 0L
)
