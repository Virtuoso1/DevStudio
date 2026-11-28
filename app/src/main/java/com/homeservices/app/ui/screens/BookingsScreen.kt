package com.homeservices.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.Booking
import com.homeservices.app.data.pretty
import com.homeservices.app.ui.components.Avatar
import com.homeservices.app.ui.components.EmptyState
import com.homeservices.app.ui.theme.ElectricRed
import com.homeservices.app.ui.theme.Ink
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline
import com.homeservices.app.ui.theme.Success

@Composable
fun BookingsScreen(
    bookings: List<Booking>,
    isProvider: Boolean,
    onStatus: (String, String) -> Unit,
    onReview: suspend (String, Int, String) -> Boolean,
    onFindPro: () -> Unit,
) {
    var reviewBooking by remember { mutableStateOf<Booking?>(null) }
    Column(Modifier.fillMaxSize()) {
        Text(
            "Bookings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 4.dp),
        )
        if (bookings.isEmpty()) {
            EmptyState(
                icon = Icons.Rounded.CalendarMonth,
                title = "No bookings yet",
                message = "When you book a professional, your appointments show up here.",
                modifier = Modifier.weight(1f),
            ) {
                Button(
                    onClick = onFindPro,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
                ) { Text("Find a professional") }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(bookings, key = { it.id }) { booking ->
                    BookingCard(booking, isProvider, { onStatus(booking.id, it) }, { reviewBooking = booking })
                }
            }
        }
    }
    reviewBooking?.let { booking ->
        ReviewDialog({ reviewBooking = null }) { rating, comment -> onReview(booking.id, rating, comment) }
    }
}

@Composable
private fun BookingCard(booking: Booking, isProvider: Boolean, onStatus: (String) -> Unit, onReview: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, Outline),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val name = if (isProvider) booking.customerName else booking.providerName
                Avatar(name.split(" ").mapNotNull { it.firstOrNull() }.take(2).joinToString("").uppercase(), booking.trade)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(booking.trade.label, style = MaterialTheme.typography.bodyMedium, color = Muted)
                }
                Text(
                    booking.status.lowercase().replaceFirstChar(Char::uppercase),
                    style = MaterialTheme.typography.labelMedium,
                    color = Success,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Success.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            Text("${booking.date.pretty()} · ${booking.time}", style = MaterialTheme.typography.titleMedium)
            if (booking.note.isNotEmpty()) {
                Text(booking.note, style = MaterialTheme.typography.bodyMedium, color = Muted)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (booking.status in listOf("PENDING", "CONFIRMED")) {
                    TextButton(onClick = { onStatus("CANCELLED") }) { Text("Cancel", color = ElectricRed) }
                }
                if (isProvider && booking.status == "PENDING") {
                    TextButton(onClick = { onStatus("CONFIRMED") }) { Text("Confirm") }
                }
                if (isProvider && booking.status == "CONFIRMED") {
                    TextButton(onClick = { onStatus("COMPLETED") }) { Text("Complete") }
                }
                if (!isProvider && booking.status == "COMPLETED" && !booking.reviewed) {
                    TextButton(onClick = onReview) { Text("Leave a review") }
                }
            }
        }
    }
}

@Composable
private fun ReviewDialog(onDismiss: () -> Unit, onReview: suspend (Int, String) -> Boolean) {
    var rating by remember { mutableStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var saving by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text("Review this job") },
        text = {
            Column {
                Row {
                    (1..5).forEach { value ->
                        TextButton(onClick = { rating = value }, enabled = !saving, contentPadding = PaddingValues(4.dp)) {
                            Text(if (rating == value) "★$value" else "$value")
                        }
                    }
                }
                OutlinedTextField(comment, { comment = it.take(500) }, label = { Text("Comment (optional)") }, enabled = !saving)
                if (failed) Text("Your review could not be saved. Please try again.", color = ElectricRed)
            }
        },
        confirmButton = {
            TextButton(enabled = !saving, onClick = {
                saving = true
                failed = false
                scope.launch {
                    try {
                        if (onReview(rating, comment)) onDismiss() else failed = true
                    } finally {
                        saving = false
                    }
                }
            }) { Text(if (saving) "Saving…" else "Submit review") }
        },
        dismissButton = { TextButton(enabled = !saving, onClick = onDismiss) { Text("Cancel") } },
    )
}