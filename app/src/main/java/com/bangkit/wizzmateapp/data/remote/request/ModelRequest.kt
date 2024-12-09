package com.bangkit.wizzmateapp.data.remote.request

data class ModelRequest(
    val userId: Int,
    val userLat: Double,
    val userLng: Double,
    val userCity: String,
    val category: List<String>,
    val days: Int,
    val time: Int,
    val budget: Int,
    val isNewUser: Boolean,
    val departureCity: String,
    val destinationCity: String
)