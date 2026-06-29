package com.example.veterinaria.data.repository

import com.example.veterinaria.data.model.Consulta
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota

interface VetRepository {
    fun getDuenos(): List<Dueno>
    fun getMascotas(): List<Mascota>
    fun getConsultas(): List<Consulta>

    fun addDueno(nombre: String, telefono: String): Dueno
    fun addMascota(
        duenoId: Int,
        nombre: String,
        especie: String,
        raza: String = "N/A",
        edad: Int = 0
    ): Mascota

    fun addConsulta(
        mascotaId: Int,
        motivo: String,
        fecha: String,
        diagnostico: String = "Pendiente",
        tratamiento: String = "Pendiente"
    ): Consulta

    fun deleteConsulta(id: Int)
    fun updateConsulta(id: Int, motivo: String, fecha: String)
}