package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.veterinariaapp.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthLoginScreen(
    authVm: AuthViewModel,
    onLoginOk: () -> Unit,
    onReset: () -> Unit
) {
    val ui by authVm.ui.collectAsState()

    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Acceso") }) }
    ) { padding ->
        Column(
            Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AssistChip(
                onClick = {},
                label = { Text("Demo: staff@vet.cl / 1234  |  owner1@vet.cl / 1234") }
            )

            OutlinedTextField(
                value = user,
                onValueChange = { user = it },
                label = { Text("Correo") },
                modifier = Modifier.fillMaxWidth(),
                isError = ui.error != null
            )
            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contraseña") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = ui.error != null
            )

            Button(
                onClick = { authVm.login(user.trim(), pass) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.loading
            ) {
                Text(if (ui.loading) "Ingresando..." else "Ingresar")
            }

            TextButton(onClick = onReset) { Text("¿Olvidaste tu contraseña?") }

            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (ui.loggedIn) {
                LaunchedEffect(Unit) { onLoginOk() }
            }
        }
    }
}