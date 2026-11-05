package com.vetqueue.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.screens.appointments.AppointmentsScreen
import com.vetqueue.app.ui.screens.appointments.BookAppointmentScreen
import com.vetqueue.app.ui.screens.auth.LoginScreen
import com.vetqueue.app.ui.screens.auth.RegisterScreen
import com.vetqueue.app.ui.screens.clinics.ClinicDetailsScreen
import com.vetqueue.app.ui.screens.clinics.ClinicSearchScreen
import com.vetqueue.app.ui.screens.home.HomeScreen
import com.vetqueue.app.ui.screens.notifications.NotificationsScreen
import com.vetqueue.app.ui.screens.pets.MyPetsScreen
import com.vetqueue.app.ui.screens.profile.ProfileScreen
import com.vetqueue.app.ui.screens.queue.LiveQueueScreen
import com.vetqueue.app.ui.screens.splash.SplashScreen
import com.vetqueue.app.ui.screens.welcome.WelcomeScreen

@Composable
fun VetQueueNavHost() {
    val navController: NavHostController = rememberNavController()
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp

    // Restored across process death via SessionManager/DataStore.
    var userId by remember { mutableStateOf<String?>(null) }
    var sessionChecked by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        userId = app.session.currentUserId()
        sessionChecked = true
    }

    if (!sessionChecked) return

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(onFinished = {
                val destination = if (userId != null) Screen.Home.route else Screen.Welcome.route
                navController.navigate(destination) { popUpTo(Screen.Splash.route) { inclusive = true } }
            })
        }

        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onLoginClick = { navController.navigate(Screen.Login.route) },
                onRegisterClick = { navController.navigate(Screen.Register.route) },
                onGoogleClick = { /* Stub: plug in Google Identity Services for the final PoE */ }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.Welcome.route) { inclusive = true } }
                }
            )
        }

        composable(Screen.Home.route) {
            CurrentUserGate(app) { uid ->
                HomeScreen(navController, uid, onNavigateNotifications = { navController.navigate(Screen.Notifications.route) })
            }
        }

        composable(Screen.MyPets.route) {
            CurrentUserGate(app) { uid -> MyPetsScreen(navController, uid) }
        }

        composable(Screen.ClinicSearch.route) {
            CurrentUserGate(app) { uid -> ClinicSearchScreen(navController, uid) }
        }

        composable(
            Screen.ClinicDetails.route,
            arguments = listOf(navArgument("clinicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clinicId = backStackEntry.arguments?.getString("clinicId") ?: return@composable
            CurrentUserGate(app) { uid -> ClinicDetailsScreen(navController, uid, clinicId) }
        }

        composable(
            Screen.BookAppointment.route,
            arguments = listOf(navArgument("clinicId") { type = NavType.StringType })
        ) { backStackEntry ->
            val clinicId = backStackEntry.arguments?.getString("clinicId") ?: return@composable
            CurrentUserGate(app) { uid -> BookAppointmentScreen(navController, uid, clinicId) }
        }

        composable(Screen.LiveQueue.route) {
            CurrentUserGate(app) { uid -> LiveQueueScreen(navController, uid) }
        }

        composable(Screen.Appointments.route) {
            CurrentUserGate(app) { uid -> AppointmentsScreen(navController, uid) }
        }

        composable(Screen.Notifications.route) {
            CurrentUserGate(app) { uid -> NotificationsScreen(navController, uid) }
        }

        composable(Screen.Profile.route) {
            CurrentUserGate(app) { uid ->
                ProfileScreen(navController, uid, onLoggedOut = {
                    navController.navigate(Screen.Welcome.route) { popUpTo(0) { inclusive = true } }
                })
            }
        }
    }
}

/** Small helper: most authenticated screens need the current userId from DataStore. */
@Composable
private fun CurrentUserGate(app: VetQueueApp, content: @Composable (String) -> Unit) {
    val userId by app.session.userIdFlow.collectAsState(initial = null)
    userId?.let { content(it) }
}
