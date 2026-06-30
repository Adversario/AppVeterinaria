package com.example.veterinaria.data.model

data class Cita(
    val id: String,
    val mascotaId: String,
    val fecha: String,
    val nota: String = ""
)
