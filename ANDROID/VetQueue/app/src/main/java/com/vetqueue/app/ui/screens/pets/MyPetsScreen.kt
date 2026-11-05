package com.vetqueue.app.ui.screens.pets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.navigation.VetQueueBottomBar
import com.vetqueue.app.ui.viewmodel.PetViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyPetsScreen(navController: NavController, userId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: PetViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))
    val pets by viewModel.pets.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My Pets") }) },
        bottomBar = { VetQueueBottomBar(navController) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Pet")
            }
        }
    ) { padding ->
        if (pets.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Filled.Pets, contentDescription = null, modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(8.dp))
                Text("No pets yet. Tap + to add your first pet.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(pets, key = { it.petId }) { pet ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Column {
                                Text(pet.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                                Text("${pet.species}${if (pet.breed.isNotBlank()) " • ${pet.breed}" else ""}")
                                if (pet.syncStatus == "pending") {
                                    Text("Pending sync", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                }
                            }
                            IconButton(onClick = { viewModel.deletePet(pet) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete ${pet.name}")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPetDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, species, breed, dob, gender, weight, notes ->
                viewModel.addPet(name, species, breed, dob, gender, weight, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddPetDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("") }
    var breed by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Pet") },
        text = {
            Column {
                OutlinedTextField(name, { name = it }, label = { Text("Name*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(species, { species = it }, label = { Text("Species*") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(breed, { breed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(dob, { dob = it }, label = { Text("Date of Birth (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(gender, { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(weight, { weight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth())
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || species.isBlank()) {
                    error = "Name and species are required."
                    return@TextButton
                }
                val parsedWeight = weight.toDoubleOrNull()
                if (weight.isNotBlank() && parsedWeight == null) {
                    error = "Weight must be a number."
                    return@TextButton
                }
                onSave(name, species, breed, dob, gender, parsedWeight ?: 0.0, notes)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
