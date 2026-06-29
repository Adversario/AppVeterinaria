package com.example.veterinaria.data.model

data class Dueno(
    val id: Int,
    val nombre: String,
    val telefono: String
)

data class Mascota(
    val id: Int,
    val duenoId: Int,
    val nombre: String,
    val especie: String,
    val raza: String = "N/A",
    val edad: Int = 0
)

data class Consulta(
    val id: Int,
    val mascotaId: Int,
    val motivo: String,
    val fecha: String,
    val diagnostico: String = "Pendiente",
    val tratamiento: String = "Pendiente"
)