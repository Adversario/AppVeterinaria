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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Mascota
import com.example.veterinariaapp.util.PdfGenerator
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val ConsultationDatePattern = "yyyy-MM-dd"

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

    var selectedMascota by remember { mutableStateOf<Mascota?>(null) }
    var mascotaMenuExpanded by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var editId by remember { mutableStateOf<String?>(null) }
    var filtroMascota by remember { mutableStateOf("Todas") }
    var msg by remember { mutableStateOf<String?>(null) }

    val selectedDate = selectedDateMillis?.let { formatConsultationDate(it) }.orEmpty()

    val visibles = remember(consultas, filtroMascota, mascotasVisibles) {
        val petIds = mascotasVisibles.map { it.id }.toSet()
        consultas
            .filter { consulta -> petIds.contains(consulta.mascotaId) }
            .filter { consulta -> filtroMascota == "Todas" || consulta.mascotaId == filtroMascota }
            .sortedByDescending { it.fecha }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: System.currentTimeMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                        if (editId == null) "Nueva consulta" else "Editar consulta",
                        style = MaterialTheme.typography.titleMedium
                    )

                    ExposedDropdownMenuBox(
                        expanded = mascotaMenuExpanded,
                        onExpandedChange = { mascotaMenuExpanded = !mascotaMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedMascota?.let { "${it.nombre} - ${it.especie}" } ?: "",
                            onValueChange = {},
                            readOnly = true,
                            enabled = editId == null,
                            label = { Text("Mascota") },
                            supportingText = {
                                Text(if (mascotasVisibles.isEmpty()) "Registra una mascota antes de crear consultas." else "Selecciona paciente")
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = mascotaMenuExpanded && editId == null,
                            onDismissRequest = { mascotaMenuExpanded = false }
                        ) {
                            mascotasVisibles.forEach { mascota ->
                                DropdownMenuItem(
                                    text = { Text("${mascota.nombre} - ${mascota.especie}") },
                                    onClick = {
                                        selectedMascota = mascota
                                        mascotaMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = motivo,
                        onValueChange = { motivo = it },
                        label = { Text("Motivo") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Event, contentDescription = null)
                        Text(
                            text = selectedDate.ifBlank { "Seleccionar fecha" },
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val mascotaId = selectedMascota?.id
                            if (mascotaId == null) {
                                msg = "Selecciona una mascota."
                                return@Button
                            }
                            if (motivo.isBlank() || selectedDate.isBlank()) {
                                msg = "Completa motivo y fecha."
                                return@Button
                            }

                            if (editId == null) {
                                vetVm.agregarConsulta(
                                    mascotaId,
                                    motivo.trim(),
                                    selectedDate,
                                    diagnostico = "Pendiente",
                                    tratamiento = "Pendiente"
                                )
                                msg = "Consulta guardada."
                            } else {
                                vetVm.editarConsulta(editId!!, motivo.trim(), selectedDate)
                                msg = "Consulta actualizada."
                            }

                            selectedMascota = null
                            motivo = ""
                            selectedDateMillis = null
                            editId = null
                        },
                        enabled = mascotasVisibles.isNotEmpty()
                    ) {
                        Text(if (editId == null) "Guardar consulta" else "Guardar cambios")
                    }

                    AnimatedVisibility(visible = editId != null, enter = fadeIn(), exit = fadeOut()) {
                        OutlinedButton(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                editId = null
                                selectedMascota = null
                                motivo = ""
                                selectedDateMillis = null
                                msg = "Edicion cancelada."
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
                    val opciones = remember(mascotasVisibles) {
                        listOf("Todas") + mascotasVisibles.map { it.id }
                    }

                    ExposedDropdownMenuBox(expanded = exp, onExpandedChange = { exp = !exp }) {
                        OutlinedTextField(
                            value = if (filtroMascota == "Todas") "Todas" else mascotasVisibles.firstOrNull { it.id == filtroMascota }?.nombre.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Mascota") },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = exp, onDismissRequest = { exp = false }) {
                            opciones.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(if (option == "Todas") "Todas" else mascotasVisibles.firstOrNull { it.id == option }?.nombre ?: option)
                                    },
                                    onClick = {
                                        filtroMascota = option
                                        exp = false
                                    }
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

        items(visibles, key = { it.id }) { consulta ->
            val mascota = mascotas.firstOrNull { it.id == consulta.mascotaId }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(consulta.motivo, style = MaterialTheme.typography.titleMedium)
                    Text("Fecha: ${consulta.fecha}")
                    Text("Mascota: ${mascota?.nombre ?: "?"} (ID ${consulta.mascotaId})")
                    Text("Diagnostico: ${consulta.diagnostico}")
                    Text("Tratamiento: ${consulta.tratamiento}")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            editId = consulta.id
                            selectedMascota = mascota
                            motivo = consulta.motivo
                            selectedDateMillis = parseConsultationDate(consulta.fecha)
                        }) { Text("Editar") }

                        IconButton(
                            onClick = {
                                val nombreMascota = mascota?.nombre ?: "Mascota ${consulta.mascotaId}"
                                runCatching {
                                    PdfGenerator.compartirRecetaPdf(context, consulta, nombreMascota)
                                }.onFailure {
                                    msg = "No se pudo generar el PDF."
                                }
                            }
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Compartir receta PDF")
                        }

                        OutlinedButton(onClick = { vetVm.eliminarConsulta(consulta.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}

private fun formatConsultationDate(dateMillis: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateMillis
    }
    return SimpleDateFormat(ConsultationDatePattern, Locale.US).format(calendar.time)
}

private fun parseConsultationDate(value: String): Long? {
    val date = runCatching {
        SimpleDateFormat(ConsultationDatePattern, Locale.US).parse(value)
    }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = date }.timeInMillis
}
