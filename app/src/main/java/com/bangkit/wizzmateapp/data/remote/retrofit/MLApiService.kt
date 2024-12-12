package com.bangkit.wizzmateapp.data.remote.retrofit

import com.bangkit.wizzmateapp.data.remote.request.ModelRequest
import com.bangkit.wizzmateapp.data.remote.request.RegisterRequest
import com.bangkit.wizzmateapp.data.remote.response.RecommendationResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MLApiService {
    @POST("recommend")
    fun recomend(
        @Body request: ModelRequest
    ): Call<RecommendationResponse>
}