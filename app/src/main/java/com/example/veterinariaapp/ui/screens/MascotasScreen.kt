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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
    val mascotas by vetVm.filteredMascotas.collectAsState()
    val allMascotas by vetVm.mascotas.observeAsState(emptyList())
    val searchQuery by vetVm.searchMascotaQuery.collectAsState()

    var filtroEspecie by remember { mutableStateOf("Todas") }
    var orden by remember { mutableStateOf("Nombre") }
    var msg by remember { mutableStateOf<String?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Mascota?>(null) }
    var editing by remember { mutableStateOf<Mascota?>(null) }

    if (showCreate && !isOwner) {
        CreateMascotaDialog(
            duenos = duenos,
            onDismiss = { showCreate = false },
            onConfirm = { duenoId, nombre, especie, raza, edad ->
                vetVm.agregarMascota(duenoId, nombre, especie, raza, edad)
                showCreate = false
                msg = "Mascota guardada."
            }
        )
    }

    deleteTarget?.let { pet ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Eliminar registro") },
            text = { Text("Estas seguro de eliminar a este registro? Esta accion no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vetVm.eliminarMascota(pet.id)
                        deleteTarget = null
                        msg = "Mascota eliminada."
                    }
                ) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { deleteTarget = null }) { Text("Cancelar") } }
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

    val especiesDisponibles = remember(allMascotas) {
        listOf("Todas") + allMascotas.map { it.especie }.distinct().sorted()
    }

    Scaffold(
        floatingActionButton = {
            if (!isOwner) {
                FloatingActionButton(onClick = { showCreate = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Registrar mascota")
                }
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
                    onValueChange = { vetVm.searchMascotaQuery.value = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { vetVm.searchMascotaQuery.value = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar busqueda")
                            }
                        }
                    },
                    placeholder = { Text("Buscar mascota...") }
                )
            }

            msg?.let {
                item { Text(it, style = MaterialTheme.typography.bodyMedium) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateMascotaDialog(
    duenos: List<Dueno>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int) -> Unit
) {
    var selectedDueno by remember { mutableStateOf<Dueno?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edadTxt by remember { mutableStateOf("") }
    var duenoError by remember { mutableStateOf(false) }
    var nombreError by remember { mutableStateOf(false) }
    var especieError by remember { mutableStateOf(false) }
    var razaError by remember { mutableStateOf(false) }
    var edadError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar mascota") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedDueno?.nombre ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Dueno") },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        isError = duenoError,
                        supportingText = {
                            if (duenoError) {
                                Text("Selecciona un dueno", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        duenos.forEach { dueno ->
                            DropdownMenuItem(
                                text = { Text(dueno.nombre) },
                                onClick = {
                                    selectedDueno = dueno
                                    duenoError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                MascotaFields(
                    nombre = nombre,
                    onNombreChange = {
                        nombre = it
                        nombreError = false
                    },
                    nombreError = nombreError,
                    especie = especie,
                    onEspecieChange = {
                        especie = it
                        especieError = false
                    },
                    especieError = especieError,
                    raza = raza,
                    onRazaChange = {
                        raza = it
                        razaError = false
                    },
                    razaError = razaError,
                    edadTxt = edadTxt,
                    onEdadChange = {
                        edadTxt = it
                        edadError = false
                    },
                    edadError = edadError
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val duenoId = selectedDueno?.id
                    val edad = edadTxt.toIntOrNull()
                    duenoError = duenoId == null
                    nombreError = nombre.isBlank()
                    especieError = especie.isBlank()
                    razaError = raza.isBlank()
                    edadError = edad == null || edad <= 0

                    if (!duenoError && !nombreError && !especieError && !razaError && !edadError && duenoId != null && edad != null) {
                        onConfirm(duenoId, nombre.trim(), especie.trim(), raza.trim(), edad)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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
    var nombreError by remember(mascota.id) { mutableStateOf(false) }
    var especieError by remember(mascota.id) { mutableStateOf(false) }
    var razaError by remember(mascota.id) { mutableStateOf(false) }
    var edadError by remember(mascota.id) { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar mascota") },
        text = {
            MascotaFields(
                nombre = nombre,
                onNombreChange = {
                    nombre = it
                    nombreError = false
                },
                nombreError = nombreError,
                especie = especie,
                onEspecieChange = {
                    especie = it
                    especieError = false
                },
                especieError = especieError,
                raza = raza,
                onRazaChange = {
                    raza = it
                    razaError = false
                },
                razaError = razaError,
                edadTxt = edadTxt,
                onEdadChange = {
                    edadTxt = it
                    edadError = false
                },
                edadError = edadError
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val edad = edadTxt.toIntOrNull()
                    nombreError = nombre.isBlank()
                    especieError = especie.isBlank()
                    razaError = raza.isBlank()
                    edadError = edad == null || edad <= 0

                    if (!nombreError && !especieError && !razaError && !edadError && edad != null) {
                        onConfirm(nombre.trim(), especie.trim(), raza.trim(), edad)
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun MascotaFields(
    nombre: String,
    onNombreChange: (String) -> Unit,
    nombreError: Boolean,
    especie: String,
    onEspecieChange: (String) -> Unit,
    especieError: Boolean,
    raza: String,
    onRazaChange: (String) -> Unit,
    razaError: Boolean,
    edadTxt: String,
    onEdadChange: (String) -> Unit,
    edadError: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre de la mascota") },
            modifier = Modifier.fillMaxWidth(),
            isError = nombreError,
            supportingText = {
                if (nombreError) {
                    Text("Este campo es obligatorio", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        OutlinedTextField(
            value = especie,
            onValueChange = onEspecieChange,
            label = { Text("Especie") },
            modifier = Modifier.fillMaxWidth(),
            isError = especieError,
            supportingText = {
                if (especieError) {
                    Text("Este campo es obligatorio", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        OutlinedTextField(
            value = raza,
            onValueChange = onRazaChange,
            label = { Text("Raza") },
            modifier = Modifier.fillMaxWidth(),
            isError = razaError,
            supportingText = {
                if (razaError) {
                    Text("Este campo es obligatorio", color = MaterialTheme.colorScheme.error)
                }
            }
        )
        OutlinedTextField(
            value = edadTxt,
            onValueChange = { onEdadChange(it.filter { char -> char.isDigit() }) },
            label = { Text("Edad") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = edadError,
            supportingText = {
                if (edadError) {
                    Text("Ingresa una edad mayor que 0", color = MaterialTheme.colorScheme.error)
                }
            }
        )
    }
}
