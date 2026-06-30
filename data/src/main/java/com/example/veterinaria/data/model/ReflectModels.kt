package com.example.veterinaria.data.model

import com.example.veterinaria.data.annotations.ReflectInfo

data class DuenoReflect(
    @ReflectInfo("ID") val id: String,
    @ReflectInfo("Nombre") val nombre: String,
    @ReflectInfo("Teléfono") val telefono: String
)

data class MascotaReflect(
    @ReflectInfo("ID") val id: String,
    @ReflectInfo("Dueño ID") val duenoId: String,
    @ReflectInfo("Nombre") val nombre: String,
    @ReflectInfo("Especie") val especie: String
)

data class ConsultaReflect(
    @ReflectInfo("ID") val id: String,
    @ReflectInfo("Mascota ID") val mascotaId: String,
    @ReflectInfo("Motivo") val motivo: String,
    @ReflectInfo("Fecha") val fecha: String
)
