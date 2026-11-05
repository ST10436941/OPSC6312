package com.vetqueue.app.ui.screens.notifications

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
import com.vetqueue.app.ui.viewmodel.NotificationViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController, userId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: NotificationViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))
    val notifications by viewModel.notifications.collectAsState()
    val formatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    LaunchedEffect(Unit) { viewModel.markAllRead() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No notifications yet. Push updates arrive here as your queue position and reminders change.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).padding(16.dp)) {
                items(notifications, key = { it.notificationId }) { n ->
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(n.type.replace('_', ' ').replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                            Text(n.message)
                            Spacer(Modifier.height(4.dp))
                            Text(formatter.format(Date(n.createdAt)), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
