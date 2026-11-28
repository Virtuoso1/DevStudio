package com.homeservices.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeservices.app.data.api.dto.LoginRequest
import com.homeservices.app.data.api.dto.RegisterRequest
import com.homeservices.app.data.api.dto.UserDto
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

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<UserDto?>(null)
    val currentUser: StateFlow<UserDto?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _darkMode = MutableStateFlow(false)
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun setDarkMode(enabled: Boolean) {
        _darkMode.value = enabled
    }

    fun login(name: String, email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            try {
                val res = repository.login(LoginRequest(email, pass))
                val user = res.user?.let { u -> u.copy(name = name.ifBlank { u.name }) } ?: UserDto("1", name.ifBlank { email.substringBefore("@") }, email, null, "CUSTOMER")
                _currentUser.value = user
                _isAuthenticated.value = true
                loadBookings()
                onSuccess()
            } catch (e: Exception) {
                // Fallback mock login if backend is offline
                if (email.isNotBlank() && pass.isNotBlank()) {
                    val mockUser = UserDto("1", name.ifBlank { email.substringBefore("@").replaceFirstChar { it.uppercase() } }, email, "+254 712 345 678", "CUSTOMER")
                    repository.setAuthToken("mock_token")
                    _currentUser.value = mockUser
                    _isAuthenticated.value = true
                    loadBookings()
                    onSuccess()
                } else {
                    _authError.value = e.localizedMessage ?: "Login failed"
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun register(
        name: String,
        email: String,
        phone: String?,
        pass: String,
        role: String,
        trade: String?,
        bio: String?,
        yearsExperience: Int?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            try {
                val res = repository.register(
                    RegisterRequest(
                        name = name,
                        email = email,
                        phone = phone,
                        password = pass,
                        role = role,
                        trade = trade,
                        bio = bio,
                        yearsExperience = yearsExperience
                    )
                )
                val user = res.user ?: UserDto("1", name, email, phone, role)
                _currentUser.value = user
                _isAuthenticated.value = true
                loadBookings()
                onSuccess()
            } catch (e: Exception) {
                // Fallback mock registration if backend is offline
                if (name.isNotBlank() && email.isNotBlank() && pass.isNotBlank()) {
                    val mockUser = UserDto("1", name, email, phone ?: "+254 712 345 678", role)
                    repository.setAuthToken("mock_token")
                    _currentUser.value = mockUser
                    _isAuthenticated.value = true
                    loadBookings()
                    onSuccess()
                } else {
                    _authError.value = e.localizedMessage ?: "Registration failed"
                }
            } finally {
                _authLoading.value = false
            }
        }
    }

    fun logout() {
        _isAuthenticated.value = false
        _currentUser.value = null
        repository.setAuthToken("")
    }

    fun clearAuthError() {
        _authError.value = null
    }

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
    val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

    private val _chats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chats: StateFlow<Map<String, List<ChatMessage>>> = _chats.asStateFlow()

    private val _diagnosis = MutableStateFlow<DiagnoseResult?>(null)
    val diagnosis: StateFlow<DiagnoseResult?> = _diagnosis.asStateFlow()

    private val _diagnosing = MutableStateFlow(false)
    val diagnosing: StateFlow<Boolean> = _diagnosing.asStateFlow()

    private var diagnoseJob: kotlinx.coroutines.Job? = null

    /** Called when the user submits the Home search bar. Cancels any diagnosis still in flight. */
    fun diagnose(text: String) {
        diagnoseJob?.cancel()
        if (text.isBlank()) {
            _diagnosis.value = null
            _diagnosing.value = false
            return
        }
        diagnoseJob = viewModelScope.launch {
            _diagnosing.value = true
            _diagnosis.value = repository.diagnose(text)
            _diagnosing.value = false
        }
    }

    /** Called when the search box is cleared, or the user picks a trade tile from the fallback. */
    fun clearDiagnosis() {
        diagnoseJob?.cancel()
        _diagnosis.value = null
        _diagnosing.value = false
    }

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
