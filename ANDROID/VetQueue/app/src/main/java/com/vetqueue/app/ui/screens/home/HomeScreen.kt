package com.vetqueue.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.navigation.Screen
import com.vetqueue.app.ui.navigation.VetQueueBottomBar
import com.vetqueue.app.ui.theme.VetOrange
import com.vetqueue.app.ui.theme.VetPurpleLight
import com.vetqueue.app.ui.viewmodel.NotificationViewModel
import com.vetqueue.app.ui.viewmodel.ProfileViewModel
import com.vetqueue.app.ui.viewmodel.QueueViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, userId: String, onNavigateNotifications: () -> Unit) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val factory = VetQueueViewModelFactory(app, userId)
    val profileViewModel: ProfileViewModel = viewModel(factory = factory)
    val queueViewModel: QueueViewModel = viewModel(factory = factory)
    val notificationViewModel: NotificationViewModel = viewModel(factory = factory)

    val userName by profileViewModel.userName.collectAsState()
    val activeQueue by queueViewModel.activeEntry.collectAsState()
    val hasUnread by notificationViewModel.hasUnread.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    IconButton(onClick = onNavigateNotifications) {
                        BadgedBox(badge = { if (hasUnread) Badge() }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        bottomBar = { VetQueueBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text(
                "Good morning${userName?.let { ", ${it.substringBefore(' ')}" } ?: ""}! 👋",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(16.dp))

            if (activeQueue != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = VetPurpleLight),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("You're in the queue", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Queue #${activeQueue!!.position}")
                        Text("Est. wait: ${activeQueue!!.estWaitMinutes} min", color = VetOrange)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { navController.navigate(Screen.LiveQueue.route) }) {
                            Text("View live queue")
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            HomeActionButton("Find a Vet", Icons.Filled.LocalHospital) {
                navController.navigate(Screen.ClinicSearch.route)
            }
            Spacer(Modifier.height(12.dp))
            HomeActionButton("My Pets", Icons.Filled.Pets) {
                navController.navigate(Screen.MyPets.route)
            }
            Spacer(Modifier.height(12.dp))
            HomeActionButton("My Appointments", Icons.Filled.CalendarMonth) {
                navController.navigate(Screen.Appointments.route)
            }
        }
    }
}

@Composable
private fun HomeActionButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(12.dp))
        Text(label, modifier = Modifier.fillMaxWidth())
    }
}