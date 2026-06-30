package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.veterinaria.data.model.Cita
import com.example.veterinaria.data.model.Mascota
import com.example.veterinariaapp.viewmodel.VetViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun HomeScreen(vetVm: VetViewModel, isOwner: Boolean) {
    val isLoading by vetVm.isLoading.observeAsState(false)
    val totalMascotas by vetVm.totalMascotas.observeAsState(0)
    val totalConsultas by vetVm.totalConsultas.observeAsState(0)
    val ultimo by vetVm.ultimoDuenoNombre.observeAsState("-")
    val duenos by vetVm.duenos.observeAsState(emptyList())
    val mascotas by vetVm.mascotas.observeAsState(emptyList())
    val proximasCitas by vetVm.proximasCitas.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        if (isLoading) {
            item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mascotas", style = MaterialTheme.typography.labelLarge)
                        Text("$totalMascotas", style = MaterialTheme.typography.headlineMedium)
                    }
                }
                ElevatedCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Consultas", style = MaterialTheme.typography.labelLarge)
                        Text("$totalConsultas", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }

        if (!isOwner) {
            item {
                Text("Proximos Pacientes", style = MaterialTheme.typography.titleMedium)
            }

            if (proximasCitas.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No tienes pacientes programados para las proximas horas. Buen dia!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(proximasCitas, key = { it.id }) { cita ->
                    val mascota = mascotas.firstOrNull { it.id == cita.mascotaId }
                    NextAppointmentCard(cita = cita, mascota = mascota)
                }
            }

            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ultimo dueno registrado", style = MaterialTheme.typography.labelLarge)
                        Text(ultimo, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }

            item { Text("Duenos (${duenos.size})", style = MaterialTheme.typography.titleMedium) }

            items(duenos, key = { it.id }) { dueno ->
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text(dueno.nombre, style = MaterialTheme.typography.titleMedium)
                        Text("Tel: ${dueno.telefono}")
                    }
                }
            }
        }
    }
}

@Composable
private fun NextAppointmentCard(cita: Cita, mascota: Mascota?) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = mascota?.nombre ?: "Paciente sin ficha",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = cita.nota.ifBlank { "Sin motivo registrado" },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = formatPredictiveDate(cita.fecha),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatPredictiveDate(value: String): String {
    val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    val parsed = runCatching { parser.parse(value) }.getOrNull() ?: return value

    val appointment = Calendar.getInstance().apply { time = parsed }
    val today = Calendar.getInstance()
    val tomorrow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1) }

    val dayLabel = when {
        appointment.isSameDay(today) -> "Hoy"
        appointment.isSameDay(tomorrow) -> "Manana"
        else -> SimpleDateFormat("dd MMM", Locale("es", "CL")).format(parsed)
    }
    val timeLabel = SimpleDateFormat("HH:mm", Locale.US).format(parsed)
    return "$dayLabel - $timeLabel hrs"
}

private fun Calendar.isSameDay(other: Calendar): Boolean =
    get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
        get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
