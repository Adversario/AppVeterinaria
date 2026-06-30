package com.example.veterinaria.data.repository

import com.example.veterinaria.data.Repository
import com.example.veterinaria.data.model.Consulta
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota

/**
 * Adaptador simple para mantener compatibilidad con capas previas
 * (si tu app antigua llamaba a un "VetRepository").
 */
class RepositoryVetRepository {

    fun getDuenos(): List<Dueno> = Repository.getDuenos()
    fun getMascotas(): List<Mascota> = Repository.getMascotas()
    fun getConsultas(): List<Consulta> = Repository.getConsultas()

    fun addDueno(nombre: String, telefono: String): Dueno =
        Repository.addDueno(nombre, telefono)

    fun addMascota(duenoId: String, nombre: String, especie: String): Mascota =
        Repository.addMascota(
            duenoId = duenoId,
            nombre = nombre,
            especie = especie,
            raza = "N/A",
            edad = 0
        )

    fun addConsulta(mascotaId: String, motivo: String, fecha: String): Consulta =
        Repository.addConsulta(
            mascotaId = mascotaId,
            motivo = motivo,
            fecha = fecha,
            diagnostico = "Pendiente",
            tratamiento = "Pendiente"
        )

    fun deleteConsulta(id: String) = Repository.deleteConsulta(id)
    fun updateConsulta(id: String, motivo: String, fecha: String) = Repository.updateConsulta(id, motivo, fecha)
}
