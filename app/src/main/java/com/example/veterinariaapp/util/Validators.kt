package com.example.veterinariaapp.util

private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$")

fun validarEmail(email: String) = emailRegex.matches(email)

// Teléfono Chile: 11 dígitos "569XXXXXXXX"
private val fonoRegex = Regex("^[0-9]{11}\$")

fun formatearFonoChile(plain: String): String? {
    if (!fonoRegex.matches(plain)) return null
    if (!plain.startsWith("569")) return null
    val local = plain.substring(3)
    return "+56 9 ${local.substring(0,4)} ${local.substring(4,8)}"
}

// Fecha DDMMAAAA → AAAAMMDD (o null si no válida)
fun parseDDMMAAAA(ddmmyyyy: String): Int? {
    val s = ddmmyyyy.trim()
    if (s.length != 8 || !s.all { it.isDigit() }) return null
    val d = s.substring(0,2).toInt()
    val m = s.substring(2,4).toInt()
    val a = s.substring(4,8).toInt()
    if (m !in 1..12 || d !in 1..31) return null
    return "%04d%02d%02d".format(a,m,d).toInt()
}