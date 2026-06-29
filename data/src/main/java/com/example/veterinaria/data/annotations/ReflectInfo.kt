package com.example.veterinaria.data.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ReflectInfo(val label: String)