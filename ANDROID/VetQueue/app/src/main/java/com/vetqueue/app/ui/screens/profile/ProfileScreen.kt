package com.vetqueue.app.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.navigation.Screen
import com.vetqueue.app.ui.navigation.VetQueueBottomBar
import com.vetqueue.app.ui.theme.VetPurpleLight
import com.vetqueue.app.ui.viewmodel.ProfileViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

/** Requirement 3.2 (User Settings): language, notification preferences, logout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, userId: String, onLoggedOut: () -> Unit) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: ProfileViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))

    val userName by viewModel.userName.collectAsState()
    val language by viewModel.language.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    var showLanguageDialog by remember { mutableStateOf(false) }
    val languages = listOf("English", "isiZulu", "Sesotho")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile & Settings") }) },
        bottomBar = { VetQueueBottomBar(navController) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(VetPurpleLight),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(40.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(userName ?: "", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(24.dp))

            SettingsRow(label = "Language: $language") { showLanguageDialog = true }
            SettingsRow(label = "Notifications: ${if (notificationsEnabled) "On" else "Off"}") {
                viewModel.setNotificationsEnabled(!notificationsEnabled)
            }
            SettingsRow(label = "Logout", isDestructive = true) {
                viewModel.logout(onLoggedOut)
            }
        }
    }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Select Language") },
            text = {
                Column {
                    languages.forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableRow { viewModel.setLanguage(lang); showLanguageDialog = false }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = lang == language, onClick = { viewModel.setLanguage(lang); showLanguageDialog = false })
                            Spacer(Modifier.width(8.dp))
                            Text(lang)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showLanguageDialog = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun SettingsRow(label: String, isDestructive: Boolean = false, onClick: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickableRow(onClick)
    ) {
        Text(
            label,
            modifier = Modifier.padding(16.dp),
            color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Modifier.clickableRow(onClick: () -> Unit) = this.then(Modifier.clickable(onClick = onClick))
