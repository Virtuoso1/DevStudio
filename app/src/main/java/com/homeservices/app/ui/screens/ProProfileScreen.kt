package com.homeservices.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.homeservices.app.data.Booking
import com.homeservices.app.data.Pro
import com.homeservices.app.data.formatRating
import com.homeservices.app.data.pretty
import com.homeservices.app.ui.components.Avatar
import com.homeservices.app.ui.components.BackButton
import com.homeservices.app.ui.theme.Cream
import com.homeservices.app.ui.theme.Ink
import com.homeservices.app.ui.theme.Muted
import com.homeservices.app.ui.theme.Outline
import com.homeservices.app.ui.theme.Success
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProProfileScreen(
    pro: Pro,
    onBack: () -> Unit,
    onMessage: () -> Unit,
    onConfirmBooking: (LocalDate, String, String) -> Booking,
    onViewBookings: () -> Unit,
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BackButton(onClick = onBack)

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                border = BorderStroke(1.dp, Outline),
            ) {
                Column {
                    Box(Modifier.fillMaxWidth().height(5.dp).background(pro.trade.accent))
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Avatar(pro.initials, pro.trade, size = 76.dp)
                        Spacer(Modifier.height(12.dp))
                        Text(pro.name, style = MaterialTheme.typography.headlineSmall)
                        Text(pro.trade.label, style = MaterialTheme.typography.bodyMedium, color = pro.trade.accent, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Outline)
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            Stat(formatRating(pro.rating), "Rating")
                            Stat("${pro.years} yrs", "Experience")
                            Stat("${pro.jobs}", "Jobs done")
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("About", style = MaterialTheme.typography.titleLarge)
                Text(pro.bio, style = MaterialTheme.typography.bodyLarge, color = Ink.copy(alpha = 0.85f))
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Credentials", style = MaterialTheme.typography.titleLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    pro.credentials.forEach { c ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(pro.trade.soft)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.Verified, null, tint = pro.trade.accent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(c, style = MaterialTheme.typography.labelMedium, color = Ink)
                        }
                    }
                }
            }
        }

        Surface(color = Color.White, shadowElevation = 8.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = onMessage,
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.5.dp, pro.trade.accent),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = pro.trade.accent),
                ) {
                    Icon(Icons.Rounded.Chat, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Message", style = MaterialTheme.typography.labelLarge)
                }
                Button(
                    onClick = { showSheet = true },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pro.trade.accent, contentColor = Color.White),
                ) {
                    Text("BOOK", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }

    if (showSheet) {
        BookingSheet(
            pro = pro,
            onDismiss = { showSheet = false },
            onConfirm = onConfirmBooking,
            onViewBookings = {
                showSheet = false
                onViewBookings()
            },
        )
    }
}

@Composable
private fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Muted)
    }
}

private val TimeSlots = listOf("08:00", "09:30", "11:00", "13:00", "14:30", "16:00")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun BookingSheet(
    pro: Pro,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate, String, String) -> Booking,
    onViewBookings: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val dates = remember { (0..6).map { LocalDate.now().plusDays(it.toLong()) } }
    var date by remember { mutableStateOf(dates.first()) }
    var time by remember { mutableStateOf<String?>(null) }
    var note by remember { mutableStateOf("") }
    var confirmed by remember { mutableStateOf<Booking?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Cream,
    ) {
        val done = confirmed
        if (done == null) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Book ${pro.name}", style = MaterialTheme.typography.headlineSmall)

                Text("Day", style = MaterialTheme.typography.titleMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dates) { d ->
                        val selected = d == date
                        Column(
                            modifier = Modifier
                                .width(58.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) pro.trade.accent else Color.White)
                                .clickable { date = d }
                                .padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            val locale = java.util.Locale.getDefault()
                            val short = java.time.format.TextStyle.SHORT
                            Text(d.dayOfWeek.getDisplayName(short, locale), style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else Muted)
                            Text("${d.dayOfMonth}", style = MaterialTheme.typography.titleLarge, color = if (selected) Color.White else Ink)
                            Text(d.month.getDisplayName(short, locale), style = MaterialTheme.typography.labelMedium, color = if (selected) Color.White else Muted)
                        }
                    }
                }

                Text("Time", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TimeSlots.forEach { slot ->
                        val selected = slot == time
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) pro.trade.accent else Color.White)
                                .clickable { time = slot }
                                .padding(horizontal = 18.dp, vertical = 10.dp),
                        ) {
                            Text(slot, style = MaterialTheme.typography.labelLarge, color = if (selected) Color.White else Ink)
                        }
                    }
                }

                Text("What needs fixing?", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Briefly describe the issue (optional)", color = Muted) },
                    minLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = pro.trade.accent,
                        unfocusedBorderColor = Outline,
                    ),
                )

                Button(
                    onClick = { confirmed = onConfirm(date, time!!, note) },
                    enabled = time != null,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = pro.trade.accent,
                        contentColor = Color.White,
                        disabledContainerColor = Outline,
                        disabledContentColor = Muted,
                    ),
                ) {
                    Text(if (time == null) "Pick a time" else "Confirm booking", style = MaterialTheme.typography.labelLarge)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 24.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Rounded.CheckCircle, null, tint = Success, modifier = Modifier.size(64.dp))
                Text("Booking confirmed", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${pro.name} · ${done.date.pretty()} at ${done.time}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Muted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onViewBookings,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = pro.trade.accent, contentColor = Color.White),
                ) { Text("View bookings", style = MaterialTheme.typography.labelLarge) }
                TextButton(onClick = onDismiss) { Text("Done", color = Ink) }
            }
        }
    }
}
