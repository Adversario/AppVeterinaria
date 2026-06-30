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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
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
import com.example.veterinaria.data.model.Consulta
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
    val duenos by vetVm.duenos.observeAsState(emptyList())

    val mascotasVisibles = remember(mascotas, isOwner, ownerId) {
        if (!isOwner) mascotas else mascotas.filter { it.duenoId == ownerId }
    }

    var selectedMascota by remember { mutableStateOf<Mascota?>(null) }
    var mascotaMenuExpanded by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }
    var diagnostico by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    var editingConsulta by remember { mutableStateOf<Consulta?>(null) }
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

    editingConsulta?.let { consulta ->
        EditConsultaDialog(
            consulta = consulta,
            onDismiss = { editingConsulta = null },
            onConfirm = { newReason, newDate, newDiagnosis, newTreatment ->
                vetVm.editarConsulta(
                    consulta.id,
                    newReason,
                    newDate,
                    newDiagnosis,
                    newTreatment
                )
                editingConsulta = null
                msg = "Consulta actualizada."
            }
        )
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
                        "Nueva consulta",
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
                            label = { Text("Mascota") },
                            supportingText = {
                                Text(if (mascotasVisibles.isEmpty()) "Registra una mascota antes de crear consultas." else "Selecciona paciente")
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = mascotaMenuExpanded,
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

                    OutlinedTextField(
                        value = diagnostico,
                        onValueChange = { diagnostico = it },
                        label = { Text("Diagnostico") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5
                    )

                    OutlinedTextField(
                        value = tratamiento,
                        onValueChange = { tratamiento = it },
                        label = { Text("Tratamiento") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false,
                        maxLines = 5
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
                            if (motivo.isBlank() || selectedDate.isBlank() || diagnostico.isBlank() || tratamiento.isBlank()) {
                                msg = "Completa motivo, fecha, diagnostico y tratamiento."
                                return@Button
                            }

                            vetVm.agregarConsulta(
                                mascotaId,
                                motivo.trim(),
                                selectedDate,
                                diagnostico = diagnostico.trim(),
                                tratamiento = tratamiento.trim()
                            )
                            msg = "Consulta guardada."

                            selectedMascota = null
                            motivo = ""
                            diagnostico = ""
                            tratamiento = ""
                            selectedDateMillis = null
                        },
                        enabled = mascotasVisibles.isNotEmpty()
                    ) {
                        Text("Guardar consulta")
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
            val dueno = duenos.firstOrNull { it.id == mascota?.duenoId }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(consulta.motivo, style = MaterialTheme.typography.titleMedium)
                    Text("Fecha: ${consulta.fecha}")
                    Text("Mascota: ${mascota?.nombre ?: "?"}")
                    Text("Diagnostico: ${consulta.diagnostico}")
                    Text("Tratamiento: ${consulta.tratamiento}")

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            editingConsulta = consulta
                        }) { Text("Editar") }

                        IconButton(
                            onClick = {
                                val nombreMascota = mascota?.nombre ?: "Mascota ${consulta.mascotaId}"
                                runCatching {
                                    PdfGenerator.compartirRecetaPdf(
                                        context = context,
                                        consulta = consulta,
                                        nombreMascota = nombreMascota,
                                        recipientEmail = dueno?.email
                                    )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditConsultaDialog(
    consulta: Consulta,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var motivo by remember(consulta.id) { mutableStateOf(consulta.motivo) }
    var diagnostico by remember(consulta.id) { mutableStateOf(consulta.diagnostico) }
    var tratamiento by remember(consulta.id) { mutableStateOf(consulta.tratamiento) }
    var selectedDateMillis by remember(consulta.id) { mutableStateOf(parseConsultationDate(consulta.fecha)) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDate = selectedDateMillis?.let { formatConsultationDate(it) } ?: consulta.fecha

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar consulta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Text(selectedDate, modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedTextField(
                    value = diagnostico,
                    onValueChange = { diagnostico = it },
                    label = { Text("Diagnostico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 5
                )
                OutlinedTextField(
                    value = tratamiento,
                    onValueChange = { tratamiento = it },
                    label = { Text("Tratamiento") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (motivo.isNotBlank() && selectedDate.isNotBlank() && diagnostico.isNotBlank() && tratamiento.isNotBlank()) {
                        onConfirm(motivo.trim(), selectedDate, diagnostico.trim(), tratamiento.trim())
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
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
