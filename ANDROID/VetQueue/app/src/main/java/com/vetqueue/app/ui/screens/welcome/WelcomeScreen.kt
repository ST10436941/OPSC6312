package com.vetqueue.app.ui.screens.welcome

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Welcome 👋", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Manage your pet's veterinary visits.", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onLoginClick, modifier = Modifier.fillMaxWidth()) { Text("Login") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onRegisterClick, modifier = Modifier.fillMaxWidth()) { Text("Create Account") }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onGoogleClick, modifier = Modifier.fillMaxWidth()) {
            Text("Continue with Google")
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "Google Sign-On is wired up as a stub in this prototype — plug in " +
                "Credential Manager / Google Identity Services with your OAuth client ID for the final PoE.",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
