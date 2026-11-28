package com.homeservices.app.data.repository

import com.homeservices.app.data.*
import com.homeservices.app.data.api.ApiService
import com.homeservices.app.data.api.NetworkModule
import com.homeservices.app.data.api.dto.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class HomeServicesRepository(private val api: ApiService = NetworkModule.apiService) {
    suspend fun login(email: String, password: String) = api.login(LoginRequest(email, password))
    suspend fun register(request: RegisterRequest) = api.register(request)
    suspend fun me(token: String) = api.me(bearer(token)).user

    suspend fun providers(trade: Trade? = null, query: String? = null): List<Pro> {
        val providers = mutableListOf<Pro>()
        var page = 1
        do {
            val response = api.listProviders(trade?.name, query, page++)
            providers += response.items.map { it.toDomain() }
        } while (response.items.isNotEmpty() && providers.size < response.total)
        return providers.distinctBy { it.id }
    }

    suspend fun provider(id: String) = api.getProvider(id).toDomain()
    suspend fun reviews(id: String) = api.getReviews(id)
    suspend fun bookAgain(token: String) = api.bookAgain(bearer(token)).map { it.toDomain() }
    suspend fun bookings(token: String) = api.listBookings(bearer(token)).map { it.toDomain() }

    suspend fun book(token: String, pro: Pro, date: LocalDate, time: String, note: String) =
        api.createBooking(bearer(token), CreateBookingRequest(pro.id, date.toString(), time, note.trim())).toDomain()

    suspend fun updateBooking(token: String, id: String, status: String) =
        api.updateBooking(bearer(token), id, StatusRequest(status)).toDomain()

    suspend fun review(token: String, id: String, rating: Int, comment: String) {
        api.review(bearer(token), id, ReviewRequest(rating, comment.trim()))
    }

    suspend fun conversations(session: AuthResponse) = api.listConversations(bearer(session.token)).map {
        Conversation(it.id, it.otherParty.name, it.otherParty.trade?.let(Trade::valueOf), it.lastMessage?.toDomain(session.user.id))
    }

    suspend fun openChat(token: String, providerId: String) =
        api.startConversation(bearer(token), StartConversationRequest(providerId)).id

    suspend fun messages(session: AuthResponse, id: String) =
        api.getMessages(bearer(session.token), id).map { it.toDomain(session.user.id) }

    suspend fun send(session: AuthResponse, id: String, text: String) =
        api.sendMessage(bearer(session.token), id, SendMessageRequest(text)).toDomain(session.user.id)

    private fun bearer(token: String) = "Bearer $token"

    private fun ProviderDto.toDomain() = Pro(
        id, name, Trade.valueOf(trade), ratingAvg, yearsExperience, ratingCount, bio,
        credentials.orEmpty().split(",").map(String::trim).filter(String::isNotEmpty),
    )

    private fun BookingDto.toDomain() = Booking(
        id, provider.name, Trade.valueOf(provider.trade), customer.name,
        LocalDate.parse(date), timeSlot, issueDescription, status, review != null,
    )

    private fun MessageDto.toDomain(userId: String) = ChatMessage(
        id ?: createdAt, body, senderId == userId,
        Instant.parse(createdAt).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm")),
    )
}
