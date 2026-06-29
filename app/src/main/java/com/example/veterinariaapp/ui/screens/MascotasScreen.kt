package com.example.veterinariaapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
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

    var duenoIdTxt by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var especie by remember { mutableStateOf("") }
    var raza by remember { mutableStateOf("") }
    var edadTxt by remember { mutableStateOf("") }

    var filtroEspecie by remember { mutableStateOf("Todas") }
    var orden by remember { mutableStateOf("ID") }
    var msg by remember { mutableStateOf<String?>(null) }

    val visibles = remember(mascotas, filtroEspecie, orden, isOwner, ownerId) {
        mascotas
            .asSequence()
            .filter { m -> if (!isOwner) true else m.duenoId == ownerId }
            .filter { m -> filtroEspecie == "Todas" || m.especie.equals(filtroEspecie, true) }
            .let { seq ->
                when (orden) {
                    "Nombre" -> seq.sortedBy { it.nombre.lowercase() }
                    "Especie" -> seq.sortedBy { it.especie.lowercase() }
                    else -> seq.sortedBy { it.id }
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

                        OutlinedTextField(
                            value = duenoIdTxt,
                            onValueChange = { duenoIdTxt = it },
                            label = { Text("Dueño ID") },
                            supportingText = {
                                val ids = duenos.joinToString { it.id.toString() }
                                Text("IDs disponibles: $ids")
                            },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(nombre, { nombre = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(especie, { especie = it }, label = { Text("Especie") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(raza, { raza = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(edadTxt, { edadTxt = it }, label = { Text("Edad") }, modifier = Modifier.fillMaxWidth())

                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                val did = duenoIdTxt.toIntOrNull()
                                val edad = edadTxt.toIntOrNull()

                                if (did == null || did <= 0) {
                                    msg = "Dueño ID inválido."
                                    return@Button
                                }
                                if (nombre.isBlank() || especie.isBlank() || raza.isBlank() || edad == null || edad < 0) {
                                    msg = "Completa nombre/especie/raza y edad válida."
                                    return@Button
                                }
                                vetVm.agregarMascota(did, nombre.trim(), especie.trim(), raza.trim(), edad)
                                msg = "Mascota guardada ✅"
                                duenoIdTxt = ""; nombre = ""; especie = ""; raza = ""; edadTxt = ""
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
                                especiesDisponibles.forEach { e ->
                                    DropdownMenuItem(
                                        text = { Text(e) },
                                        onClick = { filtroEspecie = e; exp1 = false }
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
                                listOf("ID", "Nombre", "Especie").forEach { o ->
                                    DropdownMenuItem(
                                        text = { Text(o) },
                                        onClick = { orden = o; exp2 = false }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        items(visibles, key = { it.id }) { m ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(m.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Especie: ${m.especie} • Raza: ${m.raza} • Edad: ${m.edad}")
                    Text("Dueño ID: ${m.duenoId} • ID: ${m.id}")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { vetVm.eliminarMascota(m.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}