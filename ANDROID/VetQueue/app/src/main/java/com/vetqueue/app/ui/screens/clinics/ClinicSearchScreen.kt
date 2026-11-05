package com.vetqueue.app.ui.screens.clinics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.navigation.Screen
import com.vetqueue.app.ui.navigation.VetQueueBottomBar
import com.vetqueue.app.ui.theme.VetGreen
import com.vetqueue.app.ui.theme.VetOrange
import com.vetqueue.app.ui.viewmodel.ClinicViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicSearchScreen(navController: NavController, userId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: ClinicViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))
    val clinics by viewModel.clinics.collectAsState()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Find a Clinic") }) },
        bottomBar = { VetQueueBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it; viewModel.onQueryChange(it) },
                placeholder = { Text("Search clinics...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            LazyColumn {
                items(clinics, key = { it.clinicId }) { clinic ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(Screen.ClinicDetails.path(clinic.clinicId)) }
                                .padding(16.dp)
                        ) {
                            Text(clinic.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(4.dp))
                            Text("Open • Queue: ${clinic.currentQueueCount}", color = VetGreen)
                            Text("~${clinic.estWaitMinutes} min wait", color = VetOrange)
                        }
                    }
                }
            }
        }
    }
}
