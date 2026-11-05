package com.vetqueue.app.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.viewmodel.AppointmentViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentsScreen(navController: NavController, userId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: AppointmentViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))
    val appointments by viewModel.appointments.collectAsState()

    val tabs = listOf("Upcoming", "Completed", "Cancelled")
    var selectedTab by remember { mutableStateOf(0) }

    val filtered = appointments.filter {
        when (selectedTab) {
            0 -> it.status == "upcoming"
            1 -> it.status == "completed"
            else -> it.status == "cancelled"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Appointments") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No ${tabs[selectedTab].lowercase()} appointments.")
                }
            } else {
                LazyColumn(modifier = Modifier.padding(16.dp)) {
                    items(filtered, key = { it.appointmentId }) { appt ->
                        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                            Column(Modifier.padding(16.dp)) {
                                Text(appt.service, fontWeight = FontWeight.SemiBold)
                                Text("${appt.date} • ${appt.time}")
                                if (appt.status == "upcoming") {
                                    Spacer(Modifier.height(8.dp))
                                    TextButton(onClick = { viewModel.cancel(appt) }) { Text("Cancel") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
