package com.homeservices.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.homeservices.app.ui.components.BackButton
import com.homeservices.app.ui.theme.Cream
import com.homeservices.app.ui.theme.Ink
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var locationServices by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Cream),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                BackButton(onClick = onBack)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "App Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
            }
        }

        item {
            Text(
                text = "Preferences",
                style = MaterialTheme.typography.titleMedium,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingToggleRow(
                        icon = Icons.Rounded.Notifications,
                        title = "Push Notifications",
                        subtitle = "Booking updates and message alerts",
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                    SettingToggleRow(
                        icon = Icons.Rounded.LocationOn,
                        title = "Location Services",
                        subtitle = "Find service technicians near your area",
                        checked = locationServices,
                        onCheckedChange = { locationServices = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                    SettingToggleRow(
                        icon = Icons.Rounded.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Enable dark mode visual theme",
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }
        }

        item {
            Text(
                text = "Account & Support",
                style = MaterialTheme.typography.titleMedium,
                color = Muted,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SettingItemRow(
                        icon = Icons.Rounded.Security,
                        title = "Privacy & Security",
                        subtitle = "Manage password and account privacy"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                    SettingItemRow(
                        icon = Icons.Rounded.Help,
                        title = "Help & Support Center",
                        subtitle = "FAQs and customer care contact"
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                    SettingItemRow(
                        icon = Icons.Rounded.Info,
                        title = "About HomeServices",
                        subtitle = "Terms of service and software licenses"
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Muted)
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Muted)
    }
}
