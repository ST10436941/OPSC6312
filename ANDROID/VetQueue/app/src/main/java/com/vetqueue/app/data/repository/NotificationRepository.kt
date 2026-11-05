package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.NotificationDao
import com.vetqueue.app.data.local.dao.ReminderDao
import com.vetqueue.app.data.local.entity.NotificationEntity
import com.vetqueue.app.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/** Requirement 3.7 (Real-Time Notifications). Firebase Cloud Messaging pushes
 * arrive via VetQueueMessagingService and get written here so they show up in
 * the in-app Notifications screen even if the user was offline when they fired. */
class NotificationRepository(private val notificationDao: NotificationDao) {
    fun observe(userId: String): Flow<List<NotificationEntity>> = notificationDao.observeByUser(userId)

    suspend fun markAllRead(userId: String) = notificationDao.markAllRead(userId)

    suspend fun add(userId: String, message: String, type: String) {
        notificationDao.insert(
            NotificationEntity(UUID.randomUUID().toString(), userId, message, type)
        )
    }

    suspend fun markRead(id: String) = notificationDao.markRead(id)
}

/** Requirement 3.9 (Pet Health Reminders). */
class ReminderRepository(private val reminderDao: ReminderDao) {
    fun observe(userId: String): Flow<List<ReminderEntity>> = reminderDao.observeByUser(userId)

    suspend fun add(userId: String, petId: String, type: String, dueDate: String) {
        reminderDao.insert(
            ReminderEntity(UUID.randomUUID().toString(), userId, petId, type, dueDate, syncStatus = "pending")
        )
    }

    suspend fun markDone(reminder: ReminderEntity) {
        reminderDao.update(reminder.copy(status = "done", syncStatus = "pending"))
    }
}
