package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vetqueue.app.VetQueueApp

/** Small factory so screens can request a ViewModel that needs the logged-in
 * userId or a repository from AppContainer, without pulling in Hilt/Koin. */
class VetQueueViewModelFactory(
    private val app: VetQueueApp,
    private val userId: String = ""
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val c = app.container
        return when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(c.authRepository, app.session) as T
            PetViewModel::class.java -> PetViewModel(c.petRepository, userId) as T
            ClinicViewModel::class.java -> ClinicViewModel(c.clinicRepository) as T
            AppointmentViewModel::class.java -> AppointmentViewModel(c.appointmentRepository, userId) as T
            QueueViewModel::class.java -> QueueViewModel(c.queueRepository, c.notificationRepository, userId) as T
            ProfileViewModel::class.java -> ProfileViewModel(app.session) as T
            NotificationViewModel::class.java -> NotificationViewModel(c.notificationRepository, userId) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
