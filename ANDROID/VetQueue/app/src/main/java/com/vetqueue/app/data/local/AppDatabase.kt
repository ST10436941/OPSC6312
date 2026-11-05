package com.vetqueue.app.data.local

import android.content.Context
import com.vetqueue.app.data.local.dao.AchievementDao
import com.vetqueue.app.data.local.dao.AppointmentDao
import com.vetqueue.app.data.local.dao.ClinicDao
import com.vetqueue.app.data.local.dao.NotificationDao
import com.vetqueue.app.data.local.dao.PetDao
import com.vetqueue.app.data.local.dao.QueueDao
import com.vetqueue.app.data.local.dao.ReminderDao
import com.vetqueue.app.data.local.dao.UserDao

class AppDatabase private constructor(context: Context) {

    private val helper = VetQueueSQLiteHelper(context)

    private val users = UserDao(helper)
    private val pets = PetDao(helper)
    private val clinics = ClinicDao(helper)
    private val appointments = AppointmentDao(helper)
    private val queues = QueueDao(helper)
    private val reminders = ReminderDao(helper)
    private val notifications = NotificationDao(helper)
    private val achievements = AchievementDao(helper)

    fun userDao(): UserDao = users

    fun petDao(): PetDao = pets

    fun clinicDao(): ClinicDao = clinics

    fun appointmentDao(): AppointmentDao = appointments

    fun queueDao(): QueueDao = queues

    fun reminderDao(): ReminderDao = reminders

    fun notificationDao(): NotificationDao = notifications

    fun achievementDao(): AchievementDao = achievements

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppDatabase(
                    context.applicationContext
                ).also { INSTANCE = it }
            }
        }
    }
}