package com.vetqueue.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object MyPets : Screen("my_pets")
    data object ClinicSearch : Screen("clinic_search")
    data object ClinicDetails : Screen("clinic_details/{clinicId}") {
        fun path(clinicId: String) = "clinic_details/$clinicId"
    }
    data object BookAppointment : Screen("book_appointment/{clinicId}") {
        fun path(clinicId: String) = "book_appointment/$clinicId"
    }
    data object LiveQueue : Screen("live_queue")
    data object Appointments : Screen("appointments")
    data object Notifications : Screen("notifications")
    data object Profile : Screen("profile")
}
