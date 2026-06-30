package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Dueno
import com.example.veterinariaapp.viewmodel.VetViewModel

@Composable
fun DuenosScreen(vetVm: VetViewModel) {
    val duenos by vetVm.filteredDuenos.collectAsState()
    val searchQuery by vetVm.searchDuenoQuery.collectAsState()

    var showCreate by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Dueno?>(null) }
    var deleteTarget by remember { mutableStateOf<Dueno?>(null) }

    if (showCreate) {
        CreateDuenoDialog(
            onDismiss = { showCreate = false },
            onConfirm = { nombre, telefono, email ->
                vetVm.agregarDueno(nombre, telefono, email)
                showCreate = false
                msg = "Dueno guardado."
            }
        )
    }

    editing?.let { dueno ->
        EditDuenoDialog(
            dueno = dueno,
            onDismiss = { editing = null },
            onConfirm = { newName, newPhone, newEmail ->
                vetVm.editarDueno(dueno.id, newName, newPhone, newEmail)
                editing = null
                msg = "Dueno actualizado."
            }
        )
    }

    deleteTarget?.let { dueno ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar registro") },
            text = { Text("Estas seguro de eliminar a este registro? Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vetVm.eliminarDueno(dueno.id)
                        deleteTarget = null
                        msg = "Dueno eliminado."
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Registrar dueno")
            }
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { vetVm.searchDuenoQuery.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { vetVm.searchDuenoQuery.value = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar busqueda")
                            }
                        }
                    },
                    placeholder = { Text("Buscar dueno por nombre...") }
                )
            }

            msg?.let {
                item { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }

            items(duenos, key = { it.id }) { dueno ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(dueno.nombre, style = MaterialTheme.typography.titleMedium)
                                Text("Tel: ${dueno.telefono}")
                                dueno.email?.takeIf { it.isNotBlank() }?.let { Text("Correo: $it") }
                            }
                            ContactActionButtons(phone = dueno.telefono)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { editing = dueno }) { Text("Editar") }
                            OutlinedButton(onClick = { deleteTarget = dueno }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateDuenoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar dueno") },
        text = {
            DuenoFields(
                nombre = nombre,
                onNombreChange = { nombre = it },
                telefono = telefono,
                onTelefonoChange = { telefono = it },
                email = email,
                onEmailChange = { email = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
                        onConfirm(nombre.trim(), telefono.trim(), email.trim().ifBlank { null })
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun EditDuenoDialog(
    dueno: Dueno,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?) -> Unit
) {
    var nombre by remember(dueno.id) { mutableStateOf(dueno.nombre) }
    var telefono by remember(dueno.id) { mutableStateOf(dueno.telefono) }
    var email by remember(dueno.id) { mutableStateOf(dueno.email.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar dueno") },
        text = {
            DuenoFields(
                nombre = nombre,
                onNombreChange = { nombre = it },
                telefono = telefono,
                onTelefonoChange = { telefono = it },
                email = email,
                onEmailChange = { email = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nombre.isNotBlank() && telefono.isNotBlank()) {
                        onConfirm(nombre.trim(), telefono.trim(), email.trim().ifBlank { null })
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DuenoFields(
    nombre: String,
    onNombreChange: (String) -> Unit,
    telefono: String,
    onTelefonoChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefonoChange,
            label = { Text("Telefono") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { onEmailChange(it.filter { char -> !char.isWhitespace() }) },
            label = { Text("Correo Electronico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
