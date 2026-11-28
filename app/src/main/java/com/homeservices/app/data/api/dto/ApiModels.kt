package com.homeservices.app.data.api.dto

import com.google.gson.annotations.SerializedName

// --- Providers ---

data class ProviderDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("trade") val trade: String,
    @SerializedName("bio") val bio: String,
    @SerializedName("yearsExperience") val yearsExperience: Int,
    @SerializedName("credentials") val credentials: String?,
    @SerializedName("ratingAvg") val ratingAvg: Double,
    @SerializedName("ratingCount") val ratingCount: Int
)

data class ProviderListResponse(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("items") val items: List<ProviderDto>
)

// --- Bookings ---

data class CreateBookingRequest(
    @SerializedName("providerId") val providerId: String,
    @SerializedName("date") val date: String, // YYYY-MM-DD
    @SerializedName("timeSlot") val timeSlot: String,
    @SerializedName("issueDescription") val issueDescription: String
)

data class BookingProviderDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("trade") val trade: String
)

data class BookingCustomerDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String
)

data class BookingDto(
    @SerializedName("id") val id: String,
    @SerializedName("date") val date: String,
    @SerializedName("timeSlot") val timeSlot: String,
    @SerializedName("issueDescription") val issueDescription: String,
    @SerializedName("status") val status: String,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("provider") val provider: BookingProviderDto,
    @SerializedName("customer") val customer: BookingCustomerDto
)

// --- Chat ---

data class StartConversationRequest(
    @SerializedName("providerId") val providerId: String
)

data class StartConversationResponse(
    @SerializedName("id") val id: String,
    @SerializedName("providerId") val providerId: String
)

data class MessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("body") val body: String,
    @SerializedName("senderId") val senderId: String,
    @SerializedName("createdAt") val createdAt: String
)

data class SendMessageRequest(
    @SerializedName("body") val body: String
)

// --- AI engine (trade + price diagnosis) ---

data class DiagnoseRequest(
    @SerializedName("text") val text: String
)

data class DiagnosePriceDto(
    @SerializedName("currency") val currency: String,
    @SerializedName("low") val low: Int,
    @SerializedName("mid") val mid: Int,
    @SerializedName("high") val high: Int
)

data class DiagnoseResponse(
    @SerializedName("status") val status: String, // "ok" or "unknown"
    @SerializedName("trade") val trade: String? = null,
    @SerializedName("group") val group: String? = null,
    @SerializedName("confidence") val confidence: Double,
    @SerializedName("price") val price: DiagnosePriceDto? = null,
    @SerializedName("note") val note: String? = null
)

// --- Auth ---

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String?,
    @SerializedName("role") val role: String
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterRequest(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password") val password: String,
    @SerializedName("role") val role: String = "CUSTOMER",
    @SerializedName("trade") val trade: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("yearsExperience") val yearsExperience: Int? = null,
    @SerializedName("credentials") val credentials: String? = null
)

data class AuthResponse(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto?
)
