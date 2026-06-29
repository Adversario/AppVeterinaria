package com.example.veterinaria.data.model

import com.example.veterinaria.data.annotations.ReflectInfo

data class DuenoReflect(
    @ReflectInfo("ID") val id: Int,
    @ReflectInfo("Nombre") val nombre: String,
    @ReflectInfo("Teléfono") val telefono: String
)

data class MascotaReflect(
    @ReflectInfo("ID") val id: Int,
    @ReflectInfo("Dueño ID") val duenoId: Int,
    @ReflectInfo("Nombre") val nombre: String,
    @ReflectInfo("Especie") val especie: String
)

data class ConsultaReflect(
    @ReflectInfo("ID") val id: Int,
    @ReflectInfo("Mascota ID") val mascotaId: Int,
    @ReflectInfo("Motivo") val motivo: String,
    @ReflectInfo("Fecha") val fecha: String
)