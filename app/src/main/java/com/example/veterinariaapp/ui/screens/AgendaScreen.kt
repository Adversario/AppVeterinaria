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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Cita
import com.example.veterinaria.data.model.Mascota
import com.example.veterinariaapp.viewmodel.VetViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private const val DateTimePattern = "yyyy-MM-dd HH:mm"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(vetVm: VetViewModel) {
    val mascotas by vetVm.mascotas.observeAsState(emptyList())
    val citas by vetVm.citas.observeAsState(emptyList())
    val citasOrdenadas = remember(citas) { citas.sortedBy { it.fecha } }

    var showCreate by remember { mutableStateOf(false) }
    var msg by remember { mutableStateOf<String?>(null) }

    if (showCreate) {
        CreateAppointmentDialog(
            mascotas = mascotas,
            onDismiss = { showCreate = false },
            onConfirm = { mascotaId, fecha, motivo ->
                vetVm.agendarCita(mascotaId, fecha, motivo)
                showCreate = false
                msg = "Cita agendada correctamente."
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agendar cita")
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
            msg?.let {
                item { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }

            item {
                Text("Citas agendadas (${citasOrdenadas.size})", style = MaterialTheme.typography.titleMedium)
            }

            items(citasOrdenadas, key = { it.id }) { cita ->
                AppointmentCard(
                    cita = cita,
                    mascota = mascotas.firstOrNull { it.id == cita.mascotaId },
                    onDelete = { vetVm.eliminarCita(cita.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateAppointmentDialog(
    mascotas: List<Mascota>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var selectedPet by remember { mutableStateOf<Mascota?>(null) }
    var motivo by remember { mutableStateOf("") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedHour by remember { mutableStateOf<Int?>(null) }
    var selectedMinute by remember { mutableStateOf<Int?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var petMenuExpanded by remember { mutableStateOf(false) }

    val todayStartMillis = remember { startOfTodayMillis() }
    val selectedDateLabel = selectedDateMillis?.let { formatDateLabel(it) } ?: "Seleccionar fecha"
    val selectedTimeLabel = if (selectedHour != null && selectedMinute != null) {
        "%02d:%02d hrs".format(selectedHour, selectedMinute)
    } else {
        "Seleccionar hora"
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis ?: todayStartMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis >= todayStartMillis
            }
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
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val now = remember { Calendar.getInstance() }
        val timePickerState = rememberTimePickerState(
            initialHour = selectedHour ?: now.get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedMinute ?: roundedMinute(now.get(Calendar.MINUTE)),
            is24Hour = false
        )
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedHour = timePickerState.hour
                        selectedMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") } }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agendar cita") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(
                    expanded = petMenuExpanded,
                    onExpandedChange = { petMenuExpanded = !petMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedPet?.let { "${it.nombre} - ${it.especie}" } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Mascota") },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = petMenuExpanded, onDismissRequest = { petMenuExpanded = false }) {
                        mascotas.forEach { pet ->
                            DropdownMenuItem(
                                text = { Text("${pet.nombre} - ${pet.especie}") },
                                onClick = {
                                    selectedPet = pet
                                    petMenuExpanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = motivo,
                    onValueChange = { motivo = it },
                    label = { Text("Motivo de la cita") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Event, contentDescription = null)
                        Text(
                            if (selectedDateMillis == null) "Fecha" else selectedDateLabel,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Button(onClick = { showTimePicker = true }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Filled.Schedule, contentDescription = null)
                        Text(
                            if (selectedHour == null || selectedMinute == null) "Hora" else selectedTimeLabel,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val pet = selectedPet
                    val dateMillis = selectedDateMillis
                    val hour = selectedHour
                    val minute = selectedMinute
                    if (pet != null && motivo.isNotBlank() && dateMillis != null && hour != null && minute != null) {
                        onConfirm(pet.id, formatIsoDateTime(dateMillis, hour, minute), motivo.trim())
                    }
                },
                enabled = mascotas.isNotEmpty()
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun AppointmentCard(
    cita: Cita,
    mascota: Mascota?,
    onDelete: () -> Unit
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(mascota?.nombre ?: "Mascota no encontrada", style = MaterialTheme.typography.titleMedium)
                Text(if (cita.nota.isBlank()) "Sin motivo registrado" else cita.nota, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatFriendlyDateTime(cita.fecha),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Cancelar cita", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        dismissButton = dismissButton,
        text = { content() },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = Color.Unspecified
    )
}

private fun startOfTodayMillis(): Long =
    Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

private fun roundedMinute(minute: Int): Int = ((minute + 4) / 5 * 5).coerceAtMost(59)

private fun formatIsoDateTime(dateMillis: Long, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = dateMillis
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    return SimpleDateFormat(DateTimePattern, Locale.US).format(calendar.time)
}

private fun formatDateLabel(dateMillis: Long): String {
    val calendar = Calendar.getInstance().apply { timeInMillis = dateMillis }
    return SimpleDateFormat("dd MMM", Locale("es", "CL")).format(calendar.time)
}

private fun formatFriendlyDateTime(value: String): String {
    val parser = SimpleDateFormat(DateTimePattern, Locale.US)
    val parsed = runCatching { parser.parse(value) }.getOrNull() ?: return value
    return SimpleDateFormat("d 'de' MMMM, HH:mm 'hrs'", Locale("es", "CL")).format(parsed)
}
