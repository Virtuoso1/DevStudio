package com.homeservices.app.data.api

import com.homeservices.app.data.api.dto.*
import retrofit2.http.*

interface ApiService {

    @GET("providers")
    suspend fun listProviders(
        @Query("trade") trade: String? = null,
        @Query("q") query: String? = null
    ): ProviderListResponse

    @GET("providers/{id}")
    suspend fun getProvider(@Path("id") id: String): ProviderDto

    @POST("bookings")
    suspend fun createBooking(
        @Header("Authorization") authHeader: String,
        @Body request: CreateBookingRequest
    ): BookingDto

    @GET("bookings")
    suspend fun listBookings(
        @Header("Authorization") authHeader: String,
        @Query("status") status: String? = null
    ): List<BookingDto>

    @POST("conversations")
    suspend fun startConversation(
        @Header("Authorization") authHeader: String,
        @Body request: StartConversationRequest
    ): StartConversationResponse

    @GET("conversations/{id}/messages")
    suspend fun getMessages(
        @Header("Authorization") authHeader: String,
        @Path("id") conversationId: String
    ): List<MessageDto>

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(
        @Header("Authorization") authHeader: String,
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): MessageDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}
