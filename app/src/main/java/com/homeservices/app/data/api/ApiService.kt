package com.homeservices.app.data.api

import com.homeservices.app.data.api.dto.*
import retrofit2.http.*

interface ApiService {
    @GET("providers")
    suspend fun listProviders(
        @Query("trade") trade: String?,
        @Query("q") query: String?,
        @Query("page") page: Int
    ): ProviderListResponse

    @GET("providers/{id}")
    suspend fun getProvider(@Path("id") id: String): ProviderDto

    @GET("providers/{id}/reviews")
    suspend fun getReviews(@Path("id") id: String): List<ReviewDto>

    @POST("bookings")
    suspend fun createBooking(@Header("Authorization") token: String, @Body request: CreateBookingRequest): BookingDto

    @GET("bookings")
    suspend fun listBookings(@Header("Authorization") token: String): List<BookingDto>

    @GET("bookings/book-again")
    suspend fun bookAgain(@Header("Authorization") token: String): List<ProviderDto>

    @PATCH("bookings/{id}/status")
    suspend fun updateBooking(@Header("Authorization") token: String, @Path("id") id: String, @Body request: StatusRequest): BookingDto

    @POST("bookings/{id}/reviews")
    suspend fun review(@Header("Authorization") token: String, @Path("id") id: String, @Body request: ReviewRequest): ReviewDto

    @GET("conversations")
    suspend fun listConversations(@Header("Authorization") token: String): List<ConversationDto>

    @POST("conversations")
    suspend fun startConversation(@Header("Authorization") token: String, @Body request: StartConversationRequest): StartConversationResponse

    @GET("conversations/{id}/messages")
    suspend fun getMessages(@Header("Authorization") token: String, @Path("id") id: String): List<MessageDto>

    @POST("conversations/{id}/messages")
    suspend fun sendMessage(@Header("Authorization") token: String, @Path("id") id: String, @Body request: SendMessageRequest): MessageDto

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @GET("me")
    suspend fun me(@Header("Authorization") token: String): UserResponse
}
