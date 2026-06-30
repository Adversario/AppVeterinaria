package com.example.veterinaria.data.model

data class Session(
    val email: String,
    val rol: String,
    val ownerId: String? = null
)
