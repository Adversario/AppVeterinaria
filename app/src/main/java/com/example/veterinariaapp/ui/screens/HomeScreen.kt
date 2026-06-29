package com.example.veterinariaapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.veterinariaapp.viewmodel.VetViewModel
import androidx.compose.runtime.livedata.observeAsState

@Composable
fun HomeScreen(vetVm: VetViewModel) {
    val isLoading by vetVm.isLoading.observeAsState(false)
    val totalMascotas by vetVm.totalMascotas.observeAsState(0)
    val totalConsultas by vetVm.totalConsultas.observeAsState(0)
    val ultimo by vetVm.ultimoDuenoNombre.observeAsState("-")
    val duenos by vetVm.duenos.observeAsState(emptyList())

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())

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

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Último dueño registrado", style = MaterialTheme.typography.labelLarge)
                Text(ultimo, style = MaterialTheme.typography.titleLarge)
            }
        }

        Text("Dueños (${duenos.size})", style = MaterialTheme.typography.titleMedium)
        duenos.take(4).forEach { d ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(d.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Tel: ${d.telefono}  •  ID: ${d.id}")
                }
            }
        }
    }
}