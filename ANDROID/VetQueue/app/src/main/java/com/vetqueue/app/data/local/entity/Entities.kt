package com.vetqueue.app.data.local.entity

/**
 * Native Kotlin data classes for VetQueue's local SQLite database.
 * These replace the previous AndroidX Room entities.
 */

data class UserEntity(
    val userId: String,
    val firstName: String,
    val surname: String,
    val email: String,
    val passwordHash: String,
    val language: String = "English",
    val notificationsEnabled: Boolean = true
)

data class PetEntity(
    val petId: String,
    val userId: String,
    val name: String,
    val species: String,
    val breed: String = "",
    val dateOfBirth: String = "",
    val gender: String = "",
    val weight: Double = 0.0,
    val notes: String = "",
    val syncStatus: String = "synced"
)

data class ClinicEntity(
    val clinicId: String,
    val name: String,
    val address: String,
    val contact: String,
    val hours: String,
    val services: String,
    val currentQueueCount: Int = 0,
    val estWaitMinutes: Int = 0
)

data class AppointmentEntity(
    val appointmentId: String,
    val userId: String,
    val petId: String,
    val clinicId: String,
    val service: String,
    val date: String,
    val time: String,
    val status: String = "upcoming",
    val syncStatus: String = "pending"
)

data class QueueEntryEntity(
    val queueId: String,
    val clinicId: String,
    val userId: String,
    val petId: String,
    val queueNumber: Int,
    val position: Int,
    val estWaitMinutes: Int,
    val status: String = "waiting"
)

data class ReminderEntity(
    val reminderId: String,
    val userId: String,
    val petId: String,
    val type: String,
    val dueDate: String,
    val status: String = "pending",
    val syncStatus: String = "pending"
)

data class NotificationEntity(
    val notificationId: String,
    val userId: String,
    val message: String,
    val type: String,
    val read: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class AchievementEntity(
    val achievementId: String,
    val title: String,
    val description: String
)

data class UserAchievementEntity(
    val userId: String,
    val achievementId: String,
    val unlockedAt: Long = System.currentTimeMillis()
)