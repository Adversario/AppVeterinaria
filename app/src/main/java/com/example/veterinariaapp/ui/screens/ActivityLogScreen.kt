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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    Text("Registro de actividades", style = MaterialTheme.typography.titleMedium)

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        var tipoExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = tipoExpanded,
                            onExpandedChange = { tipoExpanded = !tipoExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = filtro,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Tipo") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = tipoExpanded,
                                onDismissRequest = { tipoExpanded = false }
                            ) {
                                listOf("Todos", "AUTH", "CRUD", "NOTIF", "ERROR").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            filtro = option
                                            tipoExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var ordenExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = ordenExpanded,
                            onExpandedChange = { ordenExpanded = !ordenExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = orden,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Orden") },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = ordenExpanded,
                                onDismissRequest = { ordenExpanded = false }
                            ) {
                                listOf("Reciente", "Antiguo").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            orden = option
                                            ordenExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Text("Eventos: ${visibles.size}")
                }
            }
        }

        items(visibles, key = { "${it.epochMs}-${it.tipo}-${it.mensaje}" }) { event ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("${event.tipo} - ${event.hora}", style = MaterialTheme.typography.labelLarge)
                    Text(event.mensaje, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
