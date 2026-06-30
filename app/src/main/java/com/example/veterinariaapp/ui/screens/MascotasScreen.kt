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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Dueno
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
                        OutlinedTextField(edadTxt, { edadTxt = it }, label = { Text("Edad") }, modifier = Modifier.fillMaxWidth())

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
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(pet.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Especie: ${pet.especie} - Raza: ${pet.raza} - Edad: ${pet.edad}")
                    duenos.firstOrNull { it.id == pet.duenoId }?.let { dueno ->
                        Text("Dueno: ${dueno.nombre}")
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { vetVm.eliminarMascota(pet.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}
