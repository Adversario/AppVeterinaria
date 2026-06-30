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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.veterinariaapp.viewmodel.VetViewModel

@Composable
fun HomeScreen(vetVm: VetViewModel, isOwner: Boolean) {
    val isLoading by vetVm.isLoading.observeAsState(false)
    val totalMascotas by vetVm.totalMascotas.observeAsState(0)
    val totalConsultas by vetVm.totalConsultas.observeAsState(0)
    val ultimo by vetVm.ultimoDuenoNombre.observeAsState("-")
    val duenos by vetVm.duenos.observeAsState(emptyList())

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
