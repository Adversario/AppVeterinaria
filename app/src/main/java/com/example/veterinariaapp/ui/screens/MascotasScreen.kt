package com.example.veterinariaapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Dueno
import com.example.veterinaria.data.model.Mascota
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MascotasScreen(
    vetVm: VetViewModel,
    authVm: AuthViewModel
) {
    val session by authVm.session.collectAsState()
    val isOwner = session?.rol == "OWNER"
    val ownerId = session?.ownerId

    val duenos by vetVm.duenos.observeAsState(emptyList())
    val mascotas by vetVm.mascotas.observeAsState(emptyList())

    var selectedDueno by remember { mutableStateOf<Dueno?>(null) }
    var duenoMenuExpanded by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edadTxt by remember { mutableStateOf("") }

    var filtroEspecie by remember { mutableStateOf("Todas") }
    var orden by remember { mutableStateOf("Nombre") }
    var msg by remember { mutableStateOf<String?>(null) }
    var deleteTarget by remember { mutableStateOf<Mascota?>(null) }
    var editing by remember { mutableStateOf<Mascota?>(null) }

    deleteTarget?.let { pet ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar registro") },
            text = { Text("Estas seguro de eliminar a este registro? Esta accion no se puede deshacer.") },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        vetVm.eliminarMascota(pet.id)
                        deleteTarget = null
                        msg = "Mascota eliminada."
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") }
            }
        )
    }

    editing?.let { pet ->
        EditMascotaDialog(
            mascota = pet,
            onDismiss = { editing = null },
            onConfirm = { newName, newSpecies, newBreed, newAge ->
                vetVm.editarMascota(
                    id = pet.id,
                    duenoId = pet.duenoId,
                    nombre = newName,
                    especie = newSpecies,
                    raza = newBreed,
                    edad = newAge
                )
                editing = null
                msg = "Mascota actualizada."
            }
        )
    }

    val visibles = remember(mascotas, filtroEspecie, orden, isOwner, ownerId) {
        mascotas
            .asSequence()
            .filter { pet -> if (!isOwner) true else pet.duenoId == ownerId }
            .filter { pet -> filtroEspecie == "Todas" || pet.especie.equals(filtroEspecie, true) }
            .let { seq ->
                when (orden) {
                    "Nombre" -> seq.sortedBy { it.nombre.lowercase() }
                    "Especie" -> seq.sortedBy { it.especie.lowercase() }
                    else -> seq.sortedBy { it.nombre.lowercase() }
                }
            }
            .toList()
    }

    val especiesDisponibles = remember(mascotas) {
        listOf("Todas") + mascotas.map { it.especie }.distinct().sorted()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            AnimatedVisibility(visible = !isOwner, enter = fadeIn(), exit = fadeOut()) {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Registrar mascota", style = MaterialTheme.typography.titleMedium)

                        ExposedDropdownMenuBox(
                            expanded = duenoMenuExpanded,
                            onExpandedChange = { duenoMenuExpanded = !duenoMenuExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedDueno?.nombre ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Dueno") },
                                supportingText = {
                                    Text(if (duenos.isEmpty()) "Registra un dueno antes de crear mascotas." else "Selecciona el responsable")
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = duenoMenuExpanded,
                                onDismissRequest = { duenoMenuExpanded = false }
                            ) {
                                duenos.forEach { dueno ->
                                    DropdownMenuItem(
                                        text = { Text(dueno.nombre) },
                                        onClick = {
                                            selectedDueno = dueno
                                            duenoMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(especie, { especie = it }, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(
                            value = edadTxt,
                            onValueChange = { edadTxt = it.filter { char -> char.isDigit() } },
                            label = { Text("Edad") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val duenoId = selectedDueno?.id
                                val edad = edadTxt.toIntOrNull()

                                if (duenoId == null) {
                                    msg = "Selecciona un dueno."
                                    return@Button
                                }
                                if (nombre.isBlank() || especie.isBlank() || raza.isBlank() || edad == null || edad < 0) {
                                    msg = "Completa nombre/especie/raza y edad valida."
                                    return@Button
                                }
                                vetVm.agregarMascota(duenoId, nombre.trim(), especie.trim(), raza.trim(), edad)
                                msg = "Mascota guardada."
                                selectedDueno = null
                                nombre = ""
                                especie = ""
                                raza = ""
                                edadTxt = ""
                            }
                        ) { Text("Guardar mascota") }

                        msg?.let { Text(it) }
                    }
                }
            }
        }

        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Filtrar y ordenar", style = MaterialTheme.typography.titleMedium)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        var exp1 by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = exp1,
                            onExpandedChange = { exp1 = !exp1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = filtroEspecie,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Especie") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = exp1, onDismissRequest = { exp1 = false }) {
                                especiesDisponibles.forEach { especieDisponible ->
                                    DropdownMenuItem(
                                        text = { Text(especieDisponible) },
                                        onClick = {
                                            filtroEspecie = especieDisponible
                                            exp1 = false
                                        }
                                    )
                                }
                            }
                        }

                        var exp2 by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = exp2,
                            onExpandedChange = { exp2 = !exp2 },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = orden,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Orden") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(expanded = exp2, onDismissRequest = { exp2 = false }) {
                                listOf("Nombre", "Especie").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            orden = option
                                            exp2 = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        items(visibles, key = { it.id }) { pet ->
            val dueno = duenos.firstOrNull { it.id == pet.duenoId }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(pet.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Especie: ${pet.especie} - Raza: ${pet.raza} - Edad: ${pet.edad}")
                            dueno?.let { Text("Dueno: ${it.nombre}") }
                        }
                        ContactActionButtons(phone = dueno?.telefono)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { editing = pet }) { Text("Editar") }
                        OutlinedButton(onClick = { deleteTarget = pet }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditMascotaDialog(
    mascota: Mascota,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, Int) -> Unit
) {
    var nombre by remember(mascota.id) { mutableStateOf(mascota.nombre) }
    var especie by remember(mascota.id) { mutableStateOf(mascota.especie) }
    var raza by remember(mascota.id) { mutableStateOf(mascota.raza) }
    var edadTxt by remember(mascota.id) { mutableStateOf(mascota.edad.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar mascota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = especie,
                    onValueChange = { especie = it },
                    label = { Text("Especie") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = raza,
                    onValueChange = { raza = it },
                    label = { Text("Raza") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = edadTxt,
                    onValueChange = { edadTxt = it.filter { char -> char.isDigit() } },
                    label = { Text("Edad") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val edad = edadTxt.toIntOrNull()
                    if (nombre.isNotBlank() && especie.isNotBlank() && raza.isNotBlank() && edad != null) {
                        onConfirm(nombre.trim(), especie.trim(), raza.trim(), edad)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
