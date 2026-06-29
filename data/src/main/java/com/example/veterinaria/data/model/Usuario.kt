package com.example.veterinaria.data.model

data class Usuario(
    val id: Int,
    val email: String,
    var password: String,
    val rol: String,
    val ownerId: Int? = null
)