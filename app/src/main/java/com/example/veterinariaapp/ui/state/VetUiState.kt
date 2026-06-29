package com.example.veterinariaapp.ui.state

data class ResumenUiState(
    val totalMascotas: Int = 0,
    val totalConsultas: Int = 0,
    val ultimoDueno: String = "-",
    val isLoading: Boolean = false
)