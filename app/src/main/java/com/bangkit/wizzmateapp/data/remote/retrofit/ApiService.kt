package com.bangkit.wizzmateapp.data.remote.retrofit

import com.bangkit.wizzmateapp.data.remote.request.LoginRequest
import com.bangkit.wizzmateapp.data.remote.request.RegisterRequest
import com.bangkit.wizzmateapp.data.remote.response.AuthResponse
import com.bangkit.wizzmateapp.data.remote.response.WisataResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {
    @POST("api/auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<AuthResponse>

    @POST("api/auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<AuthResponse>

    @GET("data")
    suspend fun getData(
        @Query("page") page: Int,
        @Query("size") size: Int
    ): WisataResponse

    @GET("data")
    suspend fun getDataByCategory(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("category") category: String,
        @Query("sort") sort: String
    ): WisataResponse
}