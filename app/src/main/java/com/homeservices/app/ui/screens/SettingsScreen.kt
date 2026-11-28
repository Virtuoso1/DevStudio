package com.homeservices.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.homeservices.app.ui.components.BackButton
import com.homeservices.app.ui.theme.Cream

@Composable
fun SettingsScreen(onBack: () -> Unit, onProfile: () -> Unit, onSignOut: () -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Cream).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            BackButton(onClick = onBack)
            Text("Settings", style = MaterialTheme.typography.headlineMedium)
        }
        OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("My profile") }
        OutlinedButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) { Text("Sign out") }
    }
}
