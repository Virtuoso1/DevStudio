package com.homeservices.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeservices.app.data.repository.HomeServicesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/** AppViewModel integrated with HomeServicesRepository REST API. */
class AppViewModel(
    private val repository: HomeServicesRepository = HomeServicesRepository()
) : ViewModel() {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _chats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chats: StateFlow<Map<String, List<ChatMessage>>> = _chats.asStateFlow()

    private var nextId = 1L
    private val clock = DateTimeFormatter.ofPattern("HH:mm")
    private val replies = listOf(
        "Thanks for the details. I can come and take a look, which day suits you?",
        "Sounds doable. Tap BOOK on my profile and pick a time slot that works for you.",
        "Got it. I'll bring what I need for that. Anything else I should know before I arrive?",
    )

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            val remoteBookings = repository.getBookings()
            if (remoteBookings.isNotEmpty()) {
                _bookings.value = remoteBookings
            }
        }
    }

    fun book(pro: Pro, date: LocalDate, time: String, note: String): Booking {
        val booking = Booking(nextId++, pro, date, time, note.trim())
        _bookings.update { listOf(booking) + it }

        viewModelScope.launch {
            val created = repository.createBooking(pro, date, time, note)
            if (created != null) {
                _bookings.update { current ->
                    current.map { if (it.id == booking.id) created else it }
                }
            }
        }
        return booking
    }

    fun cancelBooking(id: Long) {
        _bookings.update { list -> list.filterNot { it.id == id } }
    }

    fun openChat(proId: String) {
        _chats.update { chats ->
            if (proId in chats) chats
            else chats + (proId to listOf(message("Hello, how can I assist you?", fromMe = false)))
        }
    }

    fun send(proId: String, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        append(proId, message(trimmed, fromMe = true))

        viewModelScope.launch {
            val sentMsg = repository.sendMessage(proId, trimmed)
            if (sentMsg == null) {
                delay(1400)
                val theirCount = _chats.value[proId].orEmpty().count { !it.fromMe }
                append(proId, message(replies[(theirCount - 1).coerceAtLeast(0) % replies.size], fromMe = false))
            }
        }
    }

    private fun append(proId: String, msg: ChatMessage) {
        _chats.update { chats -> chats + (proId to (chats[proId].orEmpty() + msg)) }
    }

    private fun message(text: String, fromMe: Boolean) =
        ChatMessage(nextId++, text, fromMe, LocalTime.now().format(clock))
}
