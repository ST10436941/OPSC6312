package com.vetqueue.app.data.local.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.vetqueue.app.data.local.VetQueueSQLiteHelper
import com.vetqueue.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// ==================================================
// SHARED DATABASE FUNCTIONS
// ==================================================

abstract class NativeDao(
    protected val helper: VetQueueSQLiteHelper
) {
    protected fun <T> observe(query: () -> T): Flow<T> = flow {
        emit(query())
        helper.changes.collect {
            emit(query())
        }
    }

    protected fun values(block: ContentValues.() -> Unit) =
        ContentValues().apply(block)
}

private fun Cursor.str(name: String): String =
    getString(getColumnIndexOrThrow(name)) ?: ""

private fun Cursor.intValue(name: String): Int =
    getInt(getColumnIndexOrThrow(name))

private fun Cursor.longValue(name: String): Long =
    getLong(getColumnIndexOrThrow(name))

private fun Cursor.doubleValue(name: String): Double =
    getDouble(getColumnIndexOrThrow(name))

private fun Cursor.boolValue(name: String): Boolean =
    intValue(name) != 0

// ==================================================
// 1. USER DAO
// ==================================================

class UserDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    suspend fun insert(user: UserEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "users", null,
            values {
                put("userId", user.userId)
                put("firstName", user.firstName)
                put("surname", user.surname)
                put("email", user.email)
                put("passwordHash", user.passwordHash)
                put("language", user.language)
                put("notificationsEnabled",
                    if (user.notificationsEnabled) 1 else 0)
            },
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun findByEmail(email: String): UserEntity? =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM users WHERE email = ? LIMIT 1",
            arrayOf(email)
        ).use { c ->
            if (c.moveToFirst()) UserEntity(
                c.str("userId"),
                c.str("firstName"),
                c.str("surname"),
                c.str("email"),
                c.str("passwordHash"),
                c.str("language"),
                c.boolValue("notificationsEnabled")
            ) else null
        }

    suspend fun findById(userId: String): UserEntity? =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM users WHERE userId = ? LIMIT 1",
            arrayOf(userId)
        ).use { c ->
            if (c.moveToFirst()) UserEntity(
                c.str("userId"),
                c.str("firstName"),
                c.str("surname"),
                c.str("email"),
                c.str("passwordHash"),
                c.str("language"),
                c.boolValue("notificationsEnabled")
            ) else null
        }

    suspend fun updateLanguage(userId: String, language: String) {
        helper.writableDatabase.update(
            "users",
            values { put("language", language) },
            "userId = ?", arrayOf(userId)
        )
        helper.notifyChanged()
    }

    suspend fun updateNotifications(userId: String, enabled: Boolean) {
        helper.writableDatabase.update(
            "users",
            values {
                put("notificationsEnabled", if (enabled) 1 else 0)
            },
            "userId = ?", arrayOf(userId)
        )
        helper.notifyChanged()
    }

    suspend fun updateProfile(
        userId: String,
        firstName: String,
        surname: String
    ) {
        helper.writableDatabase.update(
            "users",
            values {
                put("firstName", firstName)
                put("surname", surname)
            },
            "userId = ?", arrayOf(userId)
        )
        helper.notifyChanged()
    }
}

// ==================================================
// 2. PET DAO
// ==================================================

class PetDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun PetEntity.toValues() = values {
        put("petId", petId)
        put("userId", userId)
        put("name", name)
        put("species", species)
        put("breed", breed)
        put("dateOfBirth", dateOfBirth)
        put("gender", gender)
        put("weight", weight)
        put("notes", notes)
        put("syncStatus", syncStatus)
    }

    private fun Cursor.toPet() = PetEntity(
        str("petId"),
        str("userId"),
        str("name"),
        str("species"),
        str("breed"),
        str("dateOfBirth"),
        str("gender"),
        doubleValue("weight"),
        str("notes"),
        str("syncStatus")
    )

    suspend fun insert(pet: PetEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "pets", null, pet.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun update(pet: PetEntity) {
        helper.writableDatabase.update(
            "pets", pet.toValues(),
            "petId = ?", arrayOf(pet.petId)
        )
        helper.notifyChanged()
    }

    suspend fun delete(pet: PetEntity) {
        helper.writableDatabase.delete(
            "pets", "petId = ?", arrayOf(pet.petId)
        )
        helper.notifyChanged()
    }

    fun observeByUser(userId: String): Flow<List<PetEntity>> =
        observe {
            helper.readableDatabase.rawQuery(
                "SELECT * FROM pets WHERE userId = ? ORDER BY name",
                arrayOf(userId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) add(c.toPet())
                }
            }
        }

    suspend fun findById(petId: String): PetEntity? =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM pets WHERE petId = ? LIMIT 1",
            arrayOf(petId)
        ).use { c ->
            if (c.moveToFirst()) c.toPet() else null
        }

    suspend fun getPendingSync(): List<PetEntity> =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM pets WHERE syncStatus = 'pending'",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(c.toPet())
            }
        }
}

// ==================================================
// 3. CLINIC DAO
// ==================================================

class ClinicDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun ClinicEntity.toValues() = values {
        put("clinicId", clinicId)
        put("name", name)
        put("address", address)
        put("contact", contact)
        put("hours", hours)
        put("services", services)
        put("currentQueueCount", currentQueueCount)
        put("estWaitMinutes", estWaitMinutes)
    }

    private fun Cursor.toClinic() = ClinicEntity(
        str("clinicId"),
        str("name"),
        str("address"),
        str("contact"),
        str("hours"),
        str("services"),
        intValue("currentQueueCount"),
        intValue("estWaitMinutes")
    )

    suspend fun insertAll(clinics: List<ClinicEntity>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            clinics.forEach {
                db.insertWithOnConflict(
                    "clinics", null, it.toValues(),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
        helper.notifyChanged()
    }

    private fun readClinics(
        sql: String,
        args: Array<String>?
    ): List<ClinicEntity> =
        helper.readableDatabase.rawQuery(sql, args).use { c ->
            buildList {
                while (c.moveToNext()) add(c.toClinic())
            }
        }

    fun observeAll(): Flow<List<ClinicEntity>> =
        observe {
            readClinics("SELECT * FROM clinics ORDER BY name", null)
        }

    fun search(query: String): Flow<List<ClinicEntity>> =
        observe {
            readClinics(
                "SELECT * FROM clinics WHERE name LIKE ? ORDER BY name",
                arrayOf("%$query%")
            )
        }

    suspend fun findById(clinicId: String): ClinicEntity? =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM clinics WHERE clinicId = ? LIMIT 1",
            arrayOf(clinicId)
        ).use { c ->
            if (c.moveToFirst()) c.toClinic() else null
        }
}

// ==================================================
// 4. APPOINTMENT DAO
// ==================================================

class AppointmentDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun AppointmentEntity.toValues() = values {
        put("appointmentId", appointmentId)
        put("userId", userId)
        put("petId", petId)
        put("clinicId", clinicId)
        put("service", service)
        put("date", date)
        put("time", time)
        put("status", status)
        put("syncStatus", syncStatus)
    }

    private fun Cursor.toAppointment() = AppointmentEntity(
        str("appointmentId"),
        str("userId"),
        str("petId"),
        str("clinicId"),
        str("service"),
        str("date"),
        str("time"),
        str("status"),
        str("syncStatus")
    )

    suspend fun insert(appointment: AppointmentEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "appointments", null, appointment.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun update(appointment: AppointmentEntity) {
        helper.writableDatabase.update(
            "appointments", appointment.toValues(),
            "appointmentId = ?", arrayOf(appointment.appointmentId)
        )
        helper.notifyChanged()
    }

    fun observeByUser(userId: String): Flow<List<AppointmentEntity>> =
        observe {
            helper.readableDatabase.rawQuery(
                """
                SELECT * FROM appointments
                WHERE userId = ? ORDER BY date, time
                """.trimIndent(),
                arrayOf(userId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) add(c.toAppointment())
                }
            }
        }

    suspend fun findConflict(
        clinicId: String,
        date: String,
        time: String
    ): AppointmentEntity? =
        helper.readableDatabase.rawQuery(
            """
            SELECT * FROM appointments
            WHERE clinicId = ? AND date = ? AND time = ?
            AND status = 'upcoming' LIMIT 1
            """.trimIndent(),
            arrayOf(clinicId, date, time)
        ).use { c ->
            if (c.moveToFirst()) c.toAppointment() else null
        }

    suspend fun getPendingSync(): List<AppointmentEntity> =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM appointments WHERE syncStatus = 'pending'",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(c.toAppointment())
            }
        }
}

// ==================================================
// 5. QUEUE DAO
// ==================================================

class QueueDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun QueueEntryEntity.toValues() = values {
        put("queueId", queueId)
        put("clinicId", clinicId)
        put("userId", userId)
        put("petId", petId)
        put("queueNumber", queueNumber)
        put("position", position)
        put("estWaitMinutes", estWaitMinutes)
        put("status", status)
    }

    private fun Cursor.toQueue() = QueueEntryEntity(
        str("queueId"),
        str("clinicId"),
        str("userId"),
        str("petId"),
        intValue("queueNumber"),
        intValue("position"),
        intValue("estWaitMinutes"),
        str("status")
    )

    suspend fun insert(entry: QueueEntryEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "queue_entries", null, entry.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun update(entry: QueueEntryEntity) {
        helper.writableDatabase.update(
            "queue_entries", entry.toValues(),
            "queueId = ?", arrayOf(entry.queueId)
        )
        helper.notifyChanged()
    }

    fun observeActiveForUser(
        userId: String
    ): Flow<QueueEntryEntity?> = observe {
        helper.readableDatabase.rawQuery(
            """
            SELECT * FROM queue_entries
            WHERE userId = ? AND status = 'waiting' LIMIT 1
            """.trimIndent(),
            arrayOf(userId)
        ).use { c ->
            if (c.moveToFirst()) c.toQueue() else null
        }
    }

    suspend fun countWaiting(clinicId: String): Int =
        helper.readableDatabase.rawQuery(
            """
            SELECT COUNT(*) FROM queue_entries
            WHERE clinicId = ? AND status = 'waiting'
            """.trimIndent(),
            arrayOf(clinicId)
        ).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
}

// ==================================================
// 6. REMINDER DAO
// ==================================================

class ReminderDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun ReminderEntity.toValues() = values {
        put("reminderId", reminderId)
        put("userId", userId)
        put("petId", petId)
        put("type", type)
        put("dueDate", dueDate)
        put("status", status)
        put("syncStatus", syncStatus)
    }

    private fun Cursor.toReminder() = ReminderEntity(
        str("reminderId"),
        str("userId"),
        str("petId"),
        str("type"),
        str("dueDate"),
        str("status"),
        str("syncStatus")
    )

    suspend fun insert(reminder: ReminderEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "reminders", null, reminder.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun update(reminder: ReminderEntity) {
        helper.writableDatabase.update(
            "reminders", reminder.toValues(),
            "reminderId = ?", arrayOf(reminder.reminderId)
        )
        helper.notifyChanged()
    }

    fun observeByUser(userId: String): Flow<List<ReminderEntity>> =
        observe {
            helper.readableDatabase.rawQuery(
                "SELECT * FROM reminders WHERE userId = ? ORDER BY dueDate",
                arrayOf(userId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) add(c.toReminder())
                }
            }
        }

    suspend fun getDue(
        userId: String,
        today: String
    ): List<ReminderEntity> =
        helper.readableDatabase.rawQuery(
            """
            SELECT * FROM reminders
            WHERE userId = ? AND status = 'pending' AND dueDate <= ?
            """.trimIndent(),
            arrayOf(userId, today)
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(c.toReminder())
            }
        }

    suspend fun getPendingSync(): List<ReminderEntity> =
        helper.readableDatabase.rawQuery(
            "SELECT * FROM reminders WHERE syncStatus = 'pending'",
            null
        ).use { c ->
            buildList {
                while (c.moveToNext()) add(c.toReminder())
            }
        }
}

// ==================================================
// 7. NOTIFICATION DAO
// ==================================================

class NotificationDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    private fun NotificationEntity.toValues() = values {
        put("notificationId", notificationId)
        put("userId", userId)
        put("message", message)
        put("type", type)
        put("read", if (read) 1 else 0)
        put("createdAt", createdAt)
    }

    private fun Cursor.toNotification() = NotificationEntity(
        str("notificationId"),
        str("userId"),
        str("message"),
        str("type"),
        boolValue("read"),
        longValue("createdAt")
    )

    suspend fun insert(notification: NotificationEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "notifications", null, notification.toValues(),
            SQLiteDatabase.CONFLICT_REPLACE
        )
        helper.notifyChanged()
    }

    suspend fun insertIgnore(notification: NotificationEntity): Long {
        val result = helper.writableDatabase.insertWithOnConflict(
            "notifications", null, notification.toValues(),
            SQLiteDatabase.CONFLICT_IGNORE
        )
        if (result != -1L) helper.notifyChanged()
        return result
    }

    fun observeByUser(userId: String): Flow<List<NotificationEntity>> =
        observe {
            helper.readableDatabase.rawQuery(
                """
                SELECT * FROM notifications
                WHERE userId = ? ORDER BY createdAt DESC
                """.trimIndent(),
                arrayOf(userId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) add(c.toNotification())
                }
            }
        }

    suspend fun markRead(id: String) {
        helper.writableDatabase.update(
            "notifications",
            values { put("read", 1) },
            "notificationId = ?", arrayOf(id)
        )
        helper.notifyChanged()
    }

    suspend fun markAllRead(userId: String) {
        helper.writableDatabase.update(
            "notifications",
            values { put("read", 1) },
            "userId = ?", arrayOf(userId)
        )
        helper.notifyChanged()
    }
}

// ==================================================
// 8. ACHIEVEMENT DAO
// ==================================================

class AchievementDao(helper: VetQueueSQLiteHelper) : NativeDao(helper) {

    suspend fun insertAll(achievements: List<AchievementEntity>) {
        val db = helper.writableDatabase
        db.beginTransaction()

        try {
            achievements.forEach { item ->
                db.insertWithOnConflict(
                    "achievements",
                    null,
                    values {
                        put("achievementId", item.achievementId)
                        put("title", item.title)
                        put("description", item.description)
                    },
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        helper.notifyChanged()
    }

    fun observeAll(): Flow<List<AchievementEntity>> =
        observe {
            helper.readableDatabase.rawQuery(
                "SELECT * FROM achievements ORDER BY title",
                null
            ).use { c ->
                buildList {
                    while (c.moveToNext()) {
                        add(
                            AchievementEntity(
                                c.str("achievementId"),
                                c.str("title"),
                                c.str("description")
                            )
                        )
                    }
                }
            }
        }

    suspend fun unlock(userAchievement: UserAchievementEntity) {
        helper.writableDatabase.insertWithOnConflict(
            "user_achievements",
            null,
            values {
                put("userId", userAchievement.userId)
                put("achievementId", userAchievement.achievementId)
                put("unlockedAt", userAchievement.unlockedAt)
            },
            SQLiteDatabase.CONFLICT_IGNORE
        )
        helper.notifyChanged()
    }

    suspend fun isUnlocked(
        userId: String,
        achievementId: String
    ): Int =
        helper.readableDatabase.rawQuery(
            """
            SELECT COUNT(*) FROM user_achievements
            WHERE userId = ? AND achievementId = ?
            """.trimIndent(),
            arrayOf(userId, achievementId)
        ).use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }

    fun observeUnlockedIds(userId: String): Flow<List<String>> =
        observe {
            helper.readableDatabase.rawQuery(
                """
                SELECT achievementId FROM user_achievements
                WHERE userId = ?
                """.trimIndent(),
                arrayOf(userId)
            ).use { c ->
                buildList {
                    while (c.moveToNext()) add(c.getString(0))
                }
            }
        }
}