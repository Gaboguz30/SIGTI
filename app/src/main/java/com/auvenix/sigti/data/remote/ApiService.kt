package com.auvenix.sigti.data.remote

import com.auvenix.sigti.data.model.LoginRequest
import com.auvenix.sigti.data.model.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}