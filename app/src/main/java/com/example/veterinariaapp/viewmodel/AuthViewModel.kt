package com.example.veterinariaapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.veterinaria.data.Repository
import com.example.veterinaria.data.model.Session
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val loggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel : ViewModel() {

    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui

    private val _session = MutableStateFlow<Session?>(null)
    val session: StateFlow<Session?> = _session

    private val _resetMessage = MutableStateFlow<String?>(null)
    val resetMessage: StateFlow<String?> = _resetMessage

    init {
        viewModelScope.launch {
            Repository.sessionFlow.collect { savedSession ->
                _session.value = savedSession
                if (savedSession != null) {
                    _ui.value = AuthUiState(loggedIn = true)
                } else {
                    _ui.value = AuthUiState()
                }
            }
        }
    }

    fun login(email: String, pass: String) {
        _ui.value = AuthUiState(loading = true)
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                Repository.login(email, pass)
            }.onSuccess { s ->
                _session.value = s
                Repository.logAuth("Login OK: ${s.email} (${s.rol})")
                _ui.value = AuthUiState(loggedIn = true)
            }.onFailure {
                Repository.logError("Login FAIL: $email")
                _ui.value = AuthUiState(error = "Credenciales inválidas.")
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { Repository.resetPassword(email) }
                .onSuccess { newPass ->
                    Repository.logAuth("Reset password OK: $email")
                    _resetMessage.value = "Nueva contraseña (demo): $newPass"
                }
                .onFailure {
                    Repository.logError("Reset password FAIL: $email")
                    _resetMessage.value = "No se encontró el usuario."
                }
        }
    }

    fun logout() {
        val s = _session.value
        if (s != null) Repository.logAuth("Logout: ${s.email}")
        viewModelScope.launch(Dispatchers.IO) {
            Repository.logoutAsync()
            _session.value = null
            _ui.value = AuthUiState()
        }
    }
}
