package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import com.example.veterinariaapp.viewmodel.VetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(vetVm: VetViewModel) {
    val mascotas by vetVm.mascotas.observeAsState(emptyList())
    val citas by vetVm.citas.observeAsState(emptyList())

    var mascotaIdTxt by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

    val citasOrdenadas = remember(citas) { citas.sortedBy { it.fecha } }
    val idsMascotas = remember(mascotas) { mascotas.map { it.id } }

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
                    Text("Agendar cita", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = mascotaIdTxt,
                        onValueChange = { mascotaIdTxt = it },
                        label = { Text("Mascota ID") },
                        supportingText = { Text("IDs: ${idsMascotas.joinToString()}") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(fecha, { fecha = it }, label = { Text("Fecha (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(nota, { nota = it }, label = { Text("Nota") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            val mid = mascotaIdTxt.toIntOrNull()
                            if (mid == null || mascotas.none { it.id == mid }) {
                                msg = "Mascota ID inválido."
                                return@Button
                            }
                            if (fecha.isBlank()) {
                                msg = "Fecha requerida."
                                return@Button
                            }
                            vetVm.agendarCita(mid, fecha.trim(), nota.trim())
                            msg = "Cita agendada ✅ (y se programó recordatorio)"
                            mascotaIdTxt = ""; fecha = ""; nota = ""
                        }
                    ) { Text("Guardar cita") }

                    msg?.let { Text(it) }
                }
            }
        }

        item {
            Text("Citas (${citasOrdenadas.size})", style = MaterialTheme.typography.titleMedium)
        }

        items(citasOrdenadas, key = { it.id }) { c ->
            val m = mascotas.firstOrNull { it.id == c.mascotaId }
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Cita: ${c.fecha}", style = MaterialTheme.typography.titleMedium)
                    Text("Mascota: ${m?.nombre ?: "?"} (ID ${c.mascotaId})")
                    if (c.nota.isNotBlank()) Text("Nota: ${c.nota}")
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = { vetVm.eliminarCita(c.id) }) { Text("Eliminar") }
                    }
                }
            }
        }
    }
}