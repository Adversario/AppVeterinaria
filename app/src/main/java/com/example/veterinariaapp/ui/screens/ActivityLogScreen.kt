package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.veterinariaapp.viewmodel.VetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogScreen(vetVm: VetViewModel) {
    val events by vetVm.activityLog.collectAsState()

    var filtro by remember { mutableStateOf("Todos") }
    var orden by remember { mutableStateOf("Reciente") }

    val visibles = remember(events, filtro, orden) {
        val base = if (filtro == "Todos") events else events.filter { it.tipo == filtro }
        if (orden == "Reciente") base.sortedByDescending { it.epochMs } else base.sortedBy { it.epochMs }
    }

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Registro de actividades", style = MaterialTheme.typography.titleMedium)

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    var e1 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = e1, onExpandedChange = { e1 = !e1 }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = filtro,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = e1, onDismissRequest = { e1 = false }) {
                            listOf("Todos", "AUTH", "CRUD", "NOTIF", "ERROR").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { filtro = it; e1 = false })
                            }
                        }
                    }

                    var e2 by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = e2, onExpandedChange = { e2 = !e2 }, modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = orden,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Orden") },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = e2, onDismissRequest = { e2 = false }) {
                            listOf("Reciente", "Antiguo").forEach {
                                DropdownMenuItem(text = { Text(it) }, onClick = { orden = it; e2 = false })
                            }
                        }
                    }
                }

                Text("Eventos: ${visibles.size}")
            }
        }

        visibles.take(50).forEach { ev ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${ev.tipo} • ${ev.hora}", style = MaterialTheme.typography.labelLarge)
                    Text(ev.mensaje, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}