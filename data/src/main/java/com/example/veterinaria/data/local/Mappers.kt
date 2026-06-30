package com.example.veterinaria.data.local

import com.example.veterinaria.data.local.entity.AppointmentEntity
import com.example.veterinaria.data.local.entity.ConsultationEntity
import com.example.veterinaria.data.local.entity.OwnerEntity
import com.example.veterinaria.data.local.entity.PetEntity
import com.example.veterinaria.data.local.entity.UserEntity
import com.example.veterinaria.data.model.Cita
import com.example.veterinaria.data.model.Consulta
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota
import com.example.veterinaria.data.model.Usuario

fun OwnerEntity.toDomain() = Dueno(id = id, nombre = name, telefono = phone, email = email)
fun PetEntity.toDomain() = Mascota(id = id, duenoId = ownerId, nombre = name, especie = species, raza = breed, edad = age)
fun ConsultationEntity.toDomain() = Consulta(
    id = id,
    mascotaId = petId,
    motivo = reason,
    fecha = date,
    diagnostico = diagnosis,
    tratamiento = treatment
)
fun AppointmentEntity.toDomain() = Cita(id = id, mascotaId = petId, fecha = date, nota = note)
fun UserEntity.toDomain() = Usuario(id = id, email = email, password = password, rol = role, ownerId = ownerId)
