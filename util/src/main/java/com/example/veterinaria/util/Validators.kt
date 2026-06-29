package com.example.veterinaria.util

private val emailRegex =
    Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

fun validarEmail(email: String) = emailRegex.matches(email)

// Teléfono Chile: 11 dígitos "569XXXXXXXX"
private val fonoRegex = Regex("^[0-9]{11}\$")

fun formatearFonoChile(plain: String): String? {
    if (!fonoRegex.matches(plain)) return null
    if (!plain.startsWith("569")) return null
    val local = plain.substring(3)
    return "+56 9 ${local.substring(0,4)} ${local.substring(4,8)}"
}