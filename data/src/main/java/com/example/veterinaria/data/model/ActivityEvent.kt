package com.example.veterinaria.data.model

data class ActivityEvent(
    val tipo: String,
    val mensaje: String,
    val epochMs: Long,
    val hora: String
)