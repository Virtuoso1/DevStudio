package com.homeservices.app.data

import com.homeservices.app.data.api.dto.AuthResponse
import com.homeservices.app.data.repository.HomeServicesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.time.LocalDate

class ServicesSession(
    private val session: AuthResponse,
    private val scope: CoroutineScope,
    private val onExpired: () -> Unit,
) {
    val repository = HomeServicesRepository()
    val bookings = MutableStateFlow<List<Booking>>(emptyList())
    val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val bookAgain = MutableStateFlow<List<Pro>>(emptyList())
    val error = MutableStateFlow<String?>(null)
    val isProvider = session.user.role == "PROVIDER"
    private val token = session.token

    fun clearError() { error.value = null }

    suspend fun <T> request(action: suspend () -> T): T? = try {
        action()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        if (failure is HttpException && failure.code() == 401) onExpired()
        else error.value = apiError(failure)
        null
    }

    suspend fun loadBookings() {
        request { repository.bookings(token) }?.let { bookings.value = it }
    }

    suspend fun loadBookAgain() {
        if (!isProvider) request { repository.bookAgain(token) }?.let { bookAgain.value = it }
    }

    suspend fun loadConversations() {
        request { repository.conversations(session) }?.let { conversations.value = it }
    }

    suspend fun book(pro: Pro, date: LocalDate, time: String, note: String) =
        request { repository.book(token, pro, date, time, note) }

    fun updateBooking(id: String, status: String) {
        scope.launch {
            request { repository.updateBooking(token, id, status) }?.let { updated ->
                bookings.value = bookings.value.map { if (it.id == updated.id) updated else it }
            }
        }
    }

    suspend fun review(id: String, rating: Int, comment: String): Boolean {
        if (request { repository.review(token, id, rating, comment) } == null) return false
        bookings.value = bookings.value.map { if (it.id == id) it.copy(reviewed = true) else it }
        return true
    }

    suspend fun startChat(pro: Pro) = request {
        val id = repository.openChat(token, pro.id)
        Conversation(id, pro.name, pro.trade, null)
    }

    suspend fun messages(id: String) = request { repository.messages(session, id) }
    suspend fun send(id: String, text: String) = request { repository.send(session, id, text.trim()) }
}
