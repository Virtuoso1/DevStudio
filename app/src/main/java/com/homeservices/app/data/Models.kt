package com.homeservices.app.data

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.ElectricBolt
import androidx.compose.material.icons.rounded.Plumbing
import com.homeservices.app.ui.theme.ApplianceOrange
import com.homeservices.app.ui.theme.ApplianceOrangeSoft
import com.homeservices.app.ui.theme.ElectricRed
import com.homeservices.app.ui.theme.ElectricRedSoft
import com.homeservices.app.ui.theme.PlumberBlue
import com.homeservices.app.ui.theme.PlumberBlueSoft
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class Trade(
    val label: String,
    val tileLabel: String,
    val plural: String,
    val accent: Color,
    val soft: Color,
    val icon: ImageVector,
) {
    ELECTRICIAN("Electrician", "Electrician", "Electricians", ElectricRed, ElectricRedSoft, Icons.Rounded.ElectricBolt),
    PLUMBER("Plumber", "Plumber", "Plumbers", PlumberBlue, PlumberBlueSoft, Icons.Rounded.Plumbing),
    APPLIANCE("Appliance technician", "Appliance", "Appliance techs", ApplianceOrange, ApplianceOrangeSoft, Icons.Rounded.Build),
}

data class Pro(
    val id: String,
    val name: String,
    val trade: Trade,
    val rating: Double,
    val years: Int,
    val jobs: Int,
    val bio: String,
    val credentials: List<String>,
) {
    val initials: String
        get() = name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase()
}

data class Booking(
    val id: Long,
    val pro: Pro,
    val date: LocalDate,
    val time: String,
    val note: String,
)

data class ChatMessage(
    val id: Long,
    val text: String,
    val fromMe: Boolean,
    val time: String,
)

fun LocalDate.pretty(): String =
    format(DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault()))

fun formatRating(rating: Double): String = String.format(Locale.US, "%.2f", rating)
