package com.example.veterinaria.data.model

data class Usuario(
    val id: String,
    val email: String,
    var password: String,
    val rol: String,
    val ownerId: String? = null
)
