package com.example.veterinariaapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import com.example.veterinariaapp.util.PdfGenerator
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultasScreen(
    vetVm: VetViewModel,
    authVm: AuthViewModel
) {
    val session by authVm.session.collectAsState()
    val isOwner = session?.rol == "OWNER"
    val ownerId = session?.ownerId
    val context = LocalContext.current

    val mascotas by vetVm.mascotas.observeAsState(emptyList())
    val consultas by vetVm.consultas.observeAsState(emptyList())

    val mascotasVisibles = remember(mascotas, isOwner, ownerId) {
        if (!isOwner) mascotas else mascotas.filter { it.duenoId == ownerId }
    }
    val idsDisponibles = remember(mascotasVisibles) { mascotasVisibles.map { it.id } }

    var mascotaIdTxt by remember { mutableStateOf("") }
    var motivo by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }

    var editId by remember { mutableStateOf<Int?>(null) }
    var filtroMascota by remember { mutableStateOf("Todas") }
    var msg by remember { mutableStateOf<String?>(null) }

    val visibles = remember(consultas, filtroMascota, mascotasVisibles) {
        val setMascotas = mascotasVisibles.map { it.id }.toSet()
        consultas
            .filter { c -> setMascotas.contains(c.mascotaId) }
            .filter { c -> filtroMascota == "Todas" || c.mascotaId.toString() == filtroMascota }
            .sortedByDescending { it.fecha }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        if (editId == null) "Nueva consulta" else "Editar consulta #$editId",
                        style = MaterialTheme.typography.titleMedium
                    )

                    OutlinedTextField(
                        value = mascotaIdTxt,
                        onValueChange = { mascotaIdTxt = it },
                        label = { Text("Mascota ID") },
                        supportingText = { Text("IDs disponibles: ${idsDisponibles.joinToString()}") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editId == null
                    )
                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = fecha,
                        onValueChange = { fecha = it },
                        label = { Text("Fecha (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val mid = (
                                    if (editId != null) visibles.firstOrNull { it.id == editId }?.mascotaId
                                    else mascotaIdTxt.toIntOrNull()
                                    )

                            if (mid == null || !idsDisponibles.contains(mid)) {
                                msg = "Mascota ID inválido."
                                return@Button
                            }
                            if (motivo.isBlank() || fecha.isBlank()) {
                                msg = "Completa motivo y fecha."
                                return@Button
                            }

                            if (editId == null) {
                                vetVm.agregarConsulta(
                                    mid,
                                    motivo.trim(),
                                    fecha.trim(),
                                    diagnostico = "Pendiente",
                                    tratamiento = "Pendiente"
                                )
                                msg = "Consulta guardada ✅"
                            } else {
                                vetVm.editarConsulta(editId!!, motivo.trim(), fecha.trim())
                                msg = "Consulta actualizada ✅"
                            }

                            mascotaIdTxt = ""
                            motivo = ""
                            fecha = ""
                            editId = null
                        }
                    ) {
                        Text(if (editId == null) "Guardar consulta" else "Guardar cambios")
                    }

                    AnimatedVisibility(visible = editId != null, enter = fadeIn(), exit = fadeOut()) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                editId = null
                                mascotaIdTxt = ""
                                motivo = ""
                                fecha = ""
                                msg = "Edición cancelada."
                            }
                        ) { Text("Cancelar") }
                    }

                    msg?.let { Text(it) }
                }
            }
        }

        item {
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Filtro", style = MaterialTheme.typography.titleMedium)

                    var exp by remember { mutableStateOf(false) }
                    val opciones = remember(idsDisponibles) { listOf("Todas") + idsDisponibles.map { it.toString() } }

                    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
                        OutlinedTextField(
                            value = filtroMascota,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mascota") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                            opciones.forEach { o ->
                                DropdownMenuItem(
                                    text = { Text(o) },
                                    onClick = { filtroMascota = o; exp = false }
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Text("Listado (${visibles.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(visibles, key = { it.id }) { c ->
            val m = mascotas.firstOrNull { it.id == c.mascotaId }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(c.motivo, style = MaterialTheme.typography.titleMedium)
                    Text("Fecha: ${c.fecha}")
                    Text("Mascota: ${m?.nombre ?: "?"} (ID ${c.mascotaId})")
                    Text("Diagnóstico: ${c.diagnostico}")
                    Text("Tratamiento: ${c.tratamiento}")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            editId = c.id
                            motivo = c.motivo
                            fecha = c.fecha
                        }) { Text("Editar") }

                        IconButton(
                            onClick = {
                                val nombreMascota = m?.nombre ?: "Mascota ${c.mascotaId}"
                                runCatching {
                                    PdfGenerator.compartirRecetaPdf(context, c, nombreMascota)
                                }.onFailure {
                                    msg = "No se pudo generar el PDF."
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir receta PDF")
                        }

                        OutlinedButton(onClick = { vetVm.eliminarConsulta(c.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}
