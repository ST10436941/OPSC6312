package com.vetqueue.app.data

import android.content.Context

import com.vetqueue.app.data.local.AppDatabase
import com.vetqueue.app.data.remote.RetrofitClient
import com.vetqueue.app.data.repository.*

import com.vetqueue.app.ui.controller.AuthController
import com.vetqueue.app.ui.controller.PetController
import com.vetqueue.app.ui.controller.ClinicController
import com.vetqueue.app.ui.controller.AppointmentController
import com.vetqueue.app.ui.controller.QueueController
import com.vetqueue.app.ui.controller.ReminderController
import com.vetqueue.app.ui.controller.NotificationController
import com.vetqueue.app.ui.controller.AchievementController
import com.vetqueue.app.ui.controller.UserController

class AppContainer(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val api = RetrofitClient.api

    // Repositories
    val authRepository = AuthRepository(api, db.userDao())
    val petRepository = PetRepository(api, db.petDao())
    val clinicRepository = ClinicRepository(api, db.clinicDao())
    val appointmentRepository = AppointmentRepository(api, db.appointmentDao())
    val queueRepository = QueueRepository(api, db.queueDao(), clinicRepository)
    val notificationRepository = NotificationRepository(db.notificationDao())
    val reminderRepository = ReminderRepository(db.reminderDao())

    // Controllers already confirmed to use repositories
    val authController = AuthController(authRepository)
    val petController = PetController(petRepository)
    val clinicController = ClinicController(clinicRepository)
    val appointmentController = AppointmentController(appointmentRepository)

    // These controllers currently have no repository constructor arguments
    val queueController = QueueController(queueRepository)
    val reminderController = ReminderController(reminderRepository)
    val notificationController = NotificationController(notificationRepository)
    val achievementController = AchievementController(db.achievementDao())
    val userController = UserController(db.userDao())
}