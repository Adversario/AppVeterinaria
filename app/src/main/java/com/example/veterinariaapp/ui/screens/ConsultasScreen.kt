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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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

    var showCreate by remember { mutableStateOf(false) }
    var editingConsulta by remember { mutableStateOf<Consulta?>(null) }
    var filtroMascota by remember { mutableStateOf("Todas") }
    var msg by remember { mutableStateOf<String?>(null) }

    val visibles = remember(consultas, filtroMascota, mascotasVisibles) {
        val petIds = mascotasVisibles.map { it.id }.toSet()
        consultas
            .filter { consulta -> petIds.contains(consulta.mascotaId) }
            .filter { consulta -> filtroMascota == "Todas" || consulta.mascotaId == filtroMascota }
            .sortedByDescending { it.fecha }
    }

    if (showCreate) {
        CreateConsultaDialog(
            mascotas = mascotasVisibles,
            onDismiss = { showCreate = false },
            onConfirm = { mascotaId, motivo, fecha, diagnostico, tratamiento ->
                vetVm.agregarConsulta(mascotaId, motivo, fecha, diagnostico, tratamiento)
                showCreate = false
                msg = "Consulta guardada."
            }
        )
    }

    editingConsulta?.let { consulta ->
        EditConsultaDialog(
            consulta = consulta,
            onDismiss = { editingConsulta = null },
            onConfirm = { newReason, newDate, newDiagnosis, newTreatment ->
                vetVm.editarConsulta(consulta.id, newReason, newDate, newDiagnosis, newTreatment)
                editingConsulta = null
                msg = "Consulta actualizada."
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva consulta")
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
                                modifier = Modifier.menuAnchor().fillMaxWidth()
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

            msg?.let {
                item { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }

            item {
                Text("Listado (${visibles.size})", style = MaterialTheme.typography.titleMedium)
            }

            items(visibles, key = { it.id }) { consulta ->
                val mascota = mascotas.firstOrNull { it.id == consulta.mascotaId }
                val dueno = duenos.firstOrNull { it.id == mascota?.duenoId }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(consulta.motivo, style = MaterialTheme.typography.titleMedium)
                                Text("Mascota: ${mascota?.nombre ?: "?"}")
                            }
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
                        }
                        Text("Fecha: ${consulta.fecha}")
                        Text("Diagnostico: ${consulta.diagnostico}")
                        Text("Tratamiento: ${consulta.tratamiento}")

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { editingConsulta = consulta }) { Text("Editar") }
                            OutlinedButton(onClick = { vetVm.eliminarConsulta(consulta.id) }) { Text("Eliminar") }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateConsultaDialog(
    mascotas: List<Mascota>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String) -> Unit
) {
    var selectedMascota by remember { mutableStateOf<Mascota?>(null) }
    var mascotaMenuExpanded by remember { mutableStateOf(false) }
    var motivo by remember { mutableStateOf("") }
    var diagnostico by remember { mutableStateOf("") }
    var tratamiento by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDate = selectedDateMillis?.let { formatConsultationDate(it) }.orEmpty()

    if (showDatePicker) {
        ConsultationDatePickerDialog(
            initialSelectedDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                selectedDateMillis = it
                showDatePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva consulta") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                ExposedDropdownMenuBox(
                    expanded = mascotaMenuExpanded,
                    onExpandedChange = { mascotaMenuExpanded = !mascotaMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedMascota?.let { "${it.nombre} - ${it.especie}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mascota") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = mascotaMenuExpanded,
                        onDismissRequest = { mascotaMenuExpanded = false }
                    ) {
                        mascotas.forEach { mascota ->
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
                ConsultaFields(
                    motivo = motivo,
                    onMotivoChange = { motivo = it },
                    selectedDate = selectedDate,
                    onDateClick = { showDatePicker = true },
                    diagnostico = diagnostico,
                    onDiagnosticoChange = { diagnostico = it },
                    tratamiento = tratamiento,
                    onTratamientoChange = { tratamiento = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val mascotaId = selectedMascota?.id
                    if (mascotaId != null && motivo.isNotBlank() && selectedDate.isNotBlank() && diagnostico.isNotBlank() && tratamiento.isNotBlank()) {
                        onConfirm(mascotaId, motivo.trim(), selectedDate, diagnostico.trim(), tratamiento.trim())
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
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
        ConsultationDatePickerDialog(
            initialSelectedDateMillis = selectedDateMillis,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                selectedDateMillis = it
                showDatePicker = false
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar consulta") },
        text = {
            ConsultaFields(
                motivo = motivo,
                onMotivoChange = { motivo = it },
                selectedDate = selectedDate,
                onDateClick = { showDatePicker = true },
                diagnostico = diagnostico,
                onDiagnosticoChange = { diagnostico = it },
                tratamiento = tratamiento,
                onTratamientoChange = { tratamiento = it }
            )
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
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun ConsultaFields(
    motivo: String,
    onMotivoChange: (String) -> Unit,
    selectedDate: String,
    onDateClick: () -> Unit,
    diagnostico: String,
    onDiagnosticoChange: (String) -> Unit,
    tratamiento: String,
    onTratamientoChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(motivo, onMotivoChange, label = { Text("Motivo") }, modifier = Modifier.fillMaxWidth())
        Button(onClick = onDateClick, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Event, contentDescription = null)
            Text(selectedDate.ifBlank { "Seleccionar fecha" }, modifier = Modifier.padding(start = 8.dp))
        }
        OutlinedTextField(
            value = diagnostico,
            onValueChange = onDiagnosticoChange,
            label = { Text("Diagnostico") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )
        OutlinedTextField(
            value = tratamiento,
            onValueChange = onTratamientoChange,
            label = { Text("Tratamiento") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = false,
            maxLines = 5
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConsultationDatePickerDialog(
    initialSelectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (Long?) -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis ?: System.currentTimeMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(datePickerState.selectedDateMillis) },
                enabled = datePickerState.selectedDateMillis != null
            ) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    ) {
        DatePicker(state = datePickerState)
    }
}

private fun formatConsultationDate(dateMillis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return SimpleDateFormat(ConsultationDatePattern, Locale.US).format(calendar.time)
}

private fun parseConsultationDate(value: String): Long? {
    val date = runCatching {
        SimpleDateFormat(ConsultationDatePattern, Locale.US).parse(value)
    }.getOrNull() ?: return null
    return Calendar.getInstance().apply { time = date }.timeInMillis
}
