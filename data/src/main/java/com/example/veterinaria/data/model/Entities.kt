package com.example.veterinaria.data.model

data class Dueno(
    val id: String,
    val nombre: String,
    val telefono: String
)

data class Mascota(
    val id: String,
    val duenoId: String,
    val nombre: String,
    val especie: String,
    val raza: String = "N/A",
    val edad: Int = 0
)

data class Consulta(
    val id: String,
    val mascotaId: String,
    val motivo: String,
    val fecha: String,
    val diagnostico: String = "Pendiente",
    val tratamiento: String = "Pendiente"
)
