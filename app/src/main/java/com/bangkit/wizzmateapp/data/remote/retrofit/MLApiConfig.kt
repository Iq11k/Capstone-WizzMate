package com.bangkit.wizzmateapp.data.remote.retrofit

import com.bangkit.wizzmateapp.BuildConfig
import com.bangkit.wizzmateapp.BuildConfig.ML_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MLApiConfig {
    companion object{
        fun getApiService(): MLApiService {
            val loggingInterceptor =
                if(BuildConfig.DEBUG) { HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY) }else { HttpLoggingInterceptor().setLevel(
                    HttpLoggingInterceptor.Level.NONE) }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(ML_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()
            return retrofit.create(MLApiService::class.java)
        }
    }
}