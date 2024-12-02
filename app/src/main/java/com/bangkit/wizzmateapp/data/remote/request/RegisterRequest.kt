package com.bangkit.wizzmateapp.data.remote.request

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)