package com.homeservices.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homeservices.app.ui.components.BackButton
import com.homeservices.app.ui.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Cream)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            BackButton(onClick = onBack)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(Brand),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "KM",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Kelvin Mwangi", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = "kelvin@example.com", style = MaterialTheme.typography.bodyMedium, color = Muted)

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                ProfileInfoRow(icon = Icons.Rounded.Person, label = "Full Name", value = "Kelvin Mwangi")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                ProfileInfoRow(icon = Icons.Rounded.Email, label = "Email Address", value = "kelvin@example.com")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                ProfileInfoRow(icon = Icons.Rounded.Phone, label = "Phone Number", value = "+254 712 345 678")
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Outline)
                ProfileInfoRow(icon = Icons.Rounded.LocationOn, label = "Location", value = "Nairobi, Kenya")
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Ink, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Muted)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}
