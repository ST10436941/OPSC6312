package com.vetqueue.app.ui.screens.queue

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.vetqueue.app.VetQueueApp
import com.vetqueue.app.ui.theme.VetOrange
import com.vetqueue.app.ui.theme.VetPurple
import com.vetqueue.app.ui.theme.VetPurpleLight
import com.vetqueue.app.ui.theme.VetRed
import com.vetqueue.app.ui.viewmodel.QueueViewModel
import com.vetqueue.app.ui.viewmodel.VetQueueViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveQueueScreen(navController: NavController, userId: String) {
    val app = androidx.compose.ui.platform.LocalContext.current.applicationContext as VetQueueApp
    val viewModel: QueueViewModel = viewModel(factory = VetQueueViewModelFactory(app, userId))
    val entry by viewModel.activeEntry.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Queue") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val current = entry
            if (current == null) {
                Spacer(Modifier.height(64.dp))
                Text("You're not currently in a queue.")
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = VetPurpleLight),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Your Position", color = VetPurple)
                        Text(
                            "#${current.position}",
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color = VetPurple
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("${current.position - 1} pets ahead")
                        Text("Est. wait: ${current.estWaitMinutes} minutes", color = VetOrange)
                    }
                }
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { viewModel.leave(current) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = VetRed),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Leave Queue") }
            }
        }
    }
}
