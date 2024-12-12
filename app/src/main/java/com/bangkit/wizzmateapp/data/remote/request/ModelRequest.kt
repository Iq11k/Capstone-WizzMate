package com.bangkit.wizzmateapp.data.remote.request

data class ModelRequest(
    val user_id: Int,
    val user_lat: Double,
    val user_lng: Double,
    val user_city: String,
    val days: Int,
    val time: Int,
    val budget: Int,
    val departure_city: String,
    val destination_city: String
)