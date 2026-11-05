package com.vetqueue.app.ui.screens.appointments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.data.local.entity.PetEntity
import com.vetqueue.app.ui.viewmodel.AppointmentViewModel
import com.vetqueue.app.ui.viewmodel.PetViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory
import com.vetqueue.app.util.DateVisualTransformation
import com.vetqueue.app.util.InputFormatting
import com.vetqueue.app.util.TimeVisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookAppointmentScreen(navController: NavController, userId: String, clinicId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val factory = VetQueueViewModelFactory(app, userId)
    val petViewModel: PetViewModel = viewModel(factory = factory)
    val appointmentViewModel: AppointmentViewModel = viewModel(factory = factory)

    val pets by petViewModel.pets.collectAsState()
    val bookingState by appointmentViewModel.bookingState.collectAsState()

    var selectedPet by remember { mutableStateOf<PetEntity?>(null) }
    var service by remember { mutableStateOf("General Check-up") }

    // date/time are stored as RAW DIGITS ONLY (e.g. "20260325", "1030"). The
    // dashes/colon are added purely for display via DateVisualTransformation /
    // TimeVisualTransformation below. Keeping the underlying value as plain
    // digits (instead of re-formatting the full string on every keystroke) is
    // what keeps the text cursor in the right place while typing or editing
    // mid-string - re-formatting the whole string on each keystroke was
    // causing the cursor to jump to the end and digits to land in the wrong
    // position.
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }

    var petMenuExpanded by remember { mutableStateOf(false) }
    var serviceMenuExpanded by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(pets) {
        if (selectedPet == null) selectedPet = pets.firstOrNull()
    }

    // Shows a confirmation dialog instead of silently popping back - the
    // user now gets explicit feedback that the booking was accepted.
    LaunchedEffect(bookingState.success) {
        if (bookingState.success) {
            showSuccessDialog = true
        }
    }

    val services = listOf("General Check-up", "Vaccination", "Grooming", "Dental", "Surgery Consult")
    val formattedDate = InputFormatting.formatDate(date)
    val formattedTime = InputFormatting.formatTime(time)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Book Appointment") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            if (pets.isEmpty()) {
                Text("You need at least one pet profile before booking. Add a pet from the Pets tab first.")
                return@Column
            }

            ExposedDropdownMenuBox(expanded = petMenuExpanded, onExpandedChange = { petMenuExpanded = it }) {
                OutlinedTextField(
                    readOnly = true,
                    value = selectedPet?.name ?: "",
                    onValueChange = {},
                    label = { Text("Pet") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = petMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = petMenuExpanded, onDismissRequest = { petMenuExpanded = false }) {
                    pets.forEach { pet ->
                        DropdownMenuItem(text = { Text(pet.name) }, onClick = { selectedPet = pet; petMenuExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            ExposedDropdownMenuBox(expanded = serviceMenuExpanded, onExpandedChange = { serviceMenuExpanded = it }) {
                OutlinedTextField(
                    readOnly = true,
                    value = service,
                    onValueChange = {},
                    label = { Text("Service") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = serviceMenuExpanded, onDismissRequest = { serviceMenuExpanded = false }) {
                    services.forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { service = s; serviceMenuExpanded = false })
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Underlying value = raw digits; VisualTransformation displays "YYYY-MM-DD".
            OutlinedTextField(
                value = date,
                onValueChange = { input -> date = input.filter { it.isDigit() }.take(8) },
                label = { Text("Date (YYYY-MM-DD)") },
                placeholder = { Text("e.g. 2026-08-25") },
                visualTransformation = DateVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            // Underlying value = raw digits; VisualTransformation displays "HH:MM".
            OutlinedTextField(
                value = time,
                onValueChange = { input -> time = input.filter { it.isDigit() }.take(4) },
                label = { Text("Time (HH:MM)") },
                placeholder = { Text("e.g. 10:30") },
                visualTransformation = TimeVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            bookingState.errorMessage?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    selectedPet?.let { pet ->
                        appointmentViewModel.book(pet.petId, clinicId, service, formattedDate, formattedTime)
                    }
                },
                enabled = !bookingState.isSubmitting,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (bookingState.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("Confirm Booking")
                }
            }
        }
    }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { /* must acknowledge via the button below */ },
            icon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Booking Confirmed") },
            text = { Text("Your appointment for ${selectedPet?.name} on $formattedDate at $formattedTime has been booked.") },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    appointmentViewModel.resetBookingState()
                    navController.popBackStack()
                }) { Text("OK") }
            }
        )
    }
}