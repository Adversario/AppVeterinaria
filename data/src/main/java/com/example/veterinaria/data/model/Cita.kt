package com.example.veterinaria.data.model

data class Cita(
    val id: Int,
    val mascotaId: Int,
    val fecha: String,
    val nota: String = ""
)