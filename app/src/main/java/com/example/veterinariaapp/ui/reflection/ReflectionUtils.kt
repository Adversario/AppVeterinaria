package com.example.veterinariaapp.ui.reflection

import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class CampoImportante(val etiqueta: String)

fun reflectionResumen(obj: Any): String {
    val kClass: KClass<out Any> = obj::class
    val sb = StringBuilder()

    sb.append("Clase: ").append(kClass.simpleName ?: "Desconocida").append("\n")

    kClass.memberProperties.forEach { prop ->
        val ann = prop.findAnnotation<CampoImportante>()
        if (ann != null) {
            val valor = runCatching { prop.getter.call(obj) }.getOrNull()
            sb.append("- ").append(ann.etiqueta).append(": ").append(valor).append("\n")
        }
    }

    return sb.toString()
}