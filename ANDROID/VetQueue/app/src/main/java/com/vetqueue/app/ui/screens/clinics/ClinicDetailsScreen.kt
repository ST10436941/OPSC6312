package com.vetqueue.app.ui.screens.clinics

import androidx.compose.foundation.layout.*
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
import com.vetqueue.app.data.local.entity.ClinicEntity
import com.vetqueue.app.ui.navigation.Screen
import com.vetqueue.app.ui.viewmodel.QueueViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicDetailsScreen(navController: NavController, userId: String, clinicId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    var clinic by remember { mutableStateOf<ClinicEntity?>(null) }
    var joinError by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(clinicId) {
        clinic = app.container.clinicRepository.getById(clinicId)
    }

    val queueViewModel: QueueViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinic Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            val c = clinic
            if (c == null) {
                Text("Loading clinic...")
            } else {
                Text(c.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Address: ${c.address}")
                Text("Contact: ${c.contact}")
                Text("Hours: ${c.hours}")
                Spacer(Modifier.height(16.dp))
                Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Current Queue", fontWeight = FontWeight.SemiBold)
                        Text("${c.currentQueueCount} pets waiting - est. ${c.estWaitMinutes} min")
                    }
                }
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { navController.navigate(Screen.BookAppointment.path(c.clinicId)) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Book Appointment") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val pet = app.container.petRepository.getFirstPet(userId)
                            if (pet == null) {
                                joinError = "Add a pet first (My Pets tab) before joining a queue."
                            } else {
                                queueViewModel.join(c.clinicId, pet.petId)
                                navController.navigate(Screen.LiveQueue.route)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Join Queue") }

                joinError?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
