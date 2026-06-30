package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
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
            Modifier
                .padding(padding)
                .padding(16.dp),
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
                isError = ui.error != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("Contrasena") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                isError = ui.error != null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )

            Button(
                onClick = { authVm.login(user.trim(), pass) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !ui.loading
            ) {
                Text(if (ui.loading) "Ingresando..." else "Ingresar")
            }

            TextButton(onClick = onReset) { Text("Olvidaste tu contrasena?") }

            ui.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (ui.loggedIn) {
                LaunchedEffect(Unit) { onLoginOk() }
            }
        }
    }
}
