package com.homeservices.app.data.repository

import com.homeservices.app.data.*
import com.homeservices.app.data.api.ApiService
import com.homeservices.app.data.api.EngineApiService
import com.homeservices.app.data.api.EngineNetworkModule
import com.homeservices.app.data.api.NetworkModule
import com.homeservices.app.data.api.dto.AuthResponse
import com.homeservices.app.data.api.dto.CreateBookingRequest
import com.homeservices.app.data.api.dto.DiagnoseRequest
import com.homeservices.app.data.api.dto.LoginRequest
import com.homeservices.app.data.api.dto.ProviderDto
import com.homeservices.app.data.api.dto.RegisterRequest
import com.homeservices.app.data.api.dto.SendMessageRequest
import com.homeservices.app.data.api.dto.StartConversationRequest
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeServicesRepository(
    private val api: ApiService = NetworkModule.apiService,
    private val engineApi: EngineApiService = EngineNetworkModule.apiService,
) {
    private var authToken: String? = null

    suspend fun login(request: LoginRequest): AuthResponse {
        val res = api.login(request)
        setAuthToken(res.token)
        return res
    }

    suspend fun register(request: RegisterRequest): AuthResponse {
        val res = api.register(request)
        setAuthToken(res.token)
        return res
    }

    /**
     * Calls the AI engine's /diagnose endpoint with a free-text phrase (English or Swahili).
     * Recognized -> trade + estimated KES price range. Unknown (low confidence) or the engine
     * being unreachable both tell the caller to fall back to the trade tiles.
     */
    suspend fun diagnose(text: String): DiagnoseResult {
        return try {
            val res = engineApi.diagnose(DiagnoseRequest(text))
            val price = res.price
            val trade = res.trade?.let(Trade::fromEngineString)
            if (res.status == "ok" && trade != null && price != null) {
                DiagnoseResult.Recognized(
                    trade = trade,
                    confidence = res.confidence,
                    priceLow = price.low,
                    priceMid = price.mid,
                    priceHigh = price.high,
                    currency = price.currency,
                    note = res.note.orEmpty(),
                )
            } else {
                DiagnoseResult.Unknown(res.confidence)
            }
        } catch (e: Exception) {
            DiagnoseResult.Failed
        }
    }

    fun setAuthToken(token: String) {
        authToken = "Bearer $token"
    }

    suspend fun getProviders(trade: Trade? = null, query: String? = null): List<Pro> {
        return try {
            val response = api.listProviders(trade = trade?.name, query = query)
            response.items.map { it.toDomain() }
        } catch (e: Exception) {
            // Fallback to local sample data if backend server is not running
            var list = ProData.all
            if (trade != null) list = list.filter { it.trade == trade }
            if (query != null && query.isNotBlank()) list = ProData.search(query)
            list
        }
    }

    suspend fun getBookings(): List<Booking> {
        val token = authToken ?: return emptyList()
        return try {
            val list = api.listBookings(token)
            list.map { b ->
                val pro = Pro(
                    id = b.provider.id,
                    name = b.provider.name,
                    trade = try { Trade.valueOf(b.provider.trade) } catch (_: Exception) { Trade.ELECTRICIAN },
                    rating = 4.8,
                    years = 5,
                    jobs = 100,
                    bio = "",
                    credentials = emptyList()
                )
                Booking(
                    id = b.id.hashCode().toLong(),
                    pro = pro,
                    date = try { LocalDate.parse(b.date) } catch (_: Exception) { LocalDate.now() },
                    time = b.timeSlot,
                    note = b.issueDescription
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun createBooking(pro: Pro, date: LocalDate, time: String, note: String): Booking? {
        val token = authToken ?: return null
        return try {
            val req = CreateBookingRequest(
                providerId = pro.id,
                date = date.toString(),
                timeSlot = time,
                issueDescription = note
            )
            val res = api.createBooking(token, req)
            Booking(
                id = res.id.hashCode().toLong(),
                pro = pro,
                date = date,
                time = time,
                note = note
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun fetchMessages(conversationId: String): List<ChatMessage> {
        val token = authToken ?: return emptyList()
        return try {
            val list = api.getMessages(token, conversationId)
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            list.map { msg ->
                ChatMessage(
                    id = msg.id.hashCode().toLong(),
                    text = msg.body,
                    fromMe = true,
                    time = try { LocalTime.parse(msg.createdAt.substring(11, 16)).format(formatter) } catch (_: Exception) { "12:00" }
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun sendMessage(conversationId: String, text: String): ChatMessage? {
        val token = authToken ?: return null
        return try {
            val res = api.sendMessage(token, conversationId, SendMessageRequest(text))
            ChatMessage(
                id = res.id.hashCode().toLong(),
                text = res.body,
                fromMe = true,
                time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun ProviderDto.toDomain(): Pro {
        val parsedTrade = try {
            Trade.valueOf(trade)
        } catch (e: Exception) {
            Trade.ELECTRICIAN
        }
        val creds = credentials?.split(",")?.map { it.trim() } ?: emptyList()
        return Pro(
            id = id,
            name = name,
            trade = parsedTrade,
            rating = ratingAvg,
            years = yearsExperience,
            jobs = ratingCount,
            bio = bio,
            credentials = creds
        )
    }
}
