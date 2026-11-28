package com.homeservices.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.Trade
import com.homeservices.app.data.api.dto.RegisterRequest

@Composable
fun AuthScreen(
    busy: Boolean,
    error: String?,
    onLogin: (String, String) -> Unit,
    onRegister: (RegisterRequest) -> Unit,
    onClearError: () -> Unit,
) {
    var registering by rememberSaveable { mutableStateOf(false) }
    var provider by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var trade by rememberSaveable { mutableStateOf(Trade.PLUMBER) }
    var bio by rememberSaveable { mutableStateOf("") }
    var years by rememberSaveable { mutableStateOf("") }
    var tradeMenu by remember { mutableStateOf(false) }
    val isProvider = registering && provider
    val valid = email.isNotBlank() && password.isNotBlank() &&
        (!registering || (name.trim().length >= 2 && password.length >= 8)) &&
        (!isProvider || (bio.trim().length >= 10 && (years.toIntOrNull() ?: -1) >= 0))

    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().safeDrawingPadding().imePadding()
                .verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Home Services", style = MaterialTheme.typography.headlineLarge)
            Text(if (registering) "Create an account" else "Sign in", style = MaterialTheme.typography.titleLarge)
            if (registering) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Full name") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(!provider, { provider = false }, { Text("Customer") })
                    FilterChip(provider, { provider = true }, { Text("Provider") })
                }
            }
            OutlinedTextField(
                email, { email = it }, Modifier.fillMaxWidth(), label = { Text("Email") }, singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            )
            OutlinedTextField(
                password, { password = it }, Modifier.fillMaxWidth(), label = { Text("Password") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                supportingText = if (registering) ({ Text("At least 8 characters") }) else null,
            )
            if (isProvider) {
                Box {
                    OutlinedButton({ tradeMenu = true }) { Text(trade.label) }
                    DropdownMenu(tradeMenu, { tradeMenu = false }) {
                        Trade.entries.forEach { option ->
                            DropdownMenuItem({ Text(option.label) }, { trade = option; tradeMenu = false })
                        }
                    }
                }
                OutlinedTextField(
                    bio, { bio = it }, Modifier.fillMaxWidth(), label = { Text("About your services") },
                    supportingText = { Text("At least 10 characters") },
                )
                OutlinedTextField(
                    years, { years = it }, Modifier.fillMaxWidth(), label = { Text("Years of experience") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true,
                )
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Button(
                onClick = {
                    if (!registering) onLogin(email, password)
                    else onRegister(RegisterRequest(
                        name.trim(), email.trim(), password, if (isProvider) "PROVIDER" else "CUSTOMER",
                        trade.name.takeIf { isProvider }, bio.trim().takeIf { isProvider },
                        years.toIntOrNull().takeIf { isProvider },
                    ))
                },
                enabled = valid && !busy,
                modifier = Modifier.fillMaxWidth(),
            ) { Text(if (busy) "Please wait…" else if (registering) "Create account" else "Sign in") }
            TextButton(enabled = !busy, onClick = {
                registering = !registering
                password = ""
                onClearError()
            }) { Text(if (registering) "Already have an account? Sign in" else "Create an account") }
        }
    }
}
