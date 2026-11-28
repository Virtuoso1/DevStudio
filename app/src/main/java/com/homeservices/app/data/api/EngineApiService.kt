package com.homeservices.app.data.api

import com.homeservices.app.data.api.dto.DiagnoseRequest
import com.homeservices.app.data.api.dto.DiagnoseResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface EngineApiService {

    @POST("diagnose")
    suspend fun diagnose(@Body request: DiagnoseRequest): DiagnoseResponse
}
