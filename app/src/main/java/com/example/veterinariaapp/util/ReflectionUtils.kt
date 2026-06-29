package com.example.veterinariaapp.util

fun reflectionResumen(obj: Any): String {
    val c = obj::class.java
    val props = c.declaredFields.joinToString("\n") { " - ${it.name} : ${it.type.simpleName}" }
    val methods = c.declaredMethods.joinToString("\n") { " - ${it.name}()" }
    return buildString {
        appendLine("===== Reflection: ${c.simpleName} =====")
        appendLine("Propiedades:")
        appendLine(props.ifBlank { " (ninguna)" })
        appendLine("Métodos:")
        appendLine(methods.ifBlank { " (ninguno)" })
        appendLine("======================================")
    }
}