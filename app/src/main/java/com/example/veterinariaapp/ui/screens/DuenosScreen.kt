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
fun DuenosScreen(vetVm: VetViewModel) {
    val duenos by vetVm.duenos.observeAsState(emptyList())

    var nombre by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf<String?>(null) }

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
                    Text("Registrar dueño", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            if (nombre.isBlank() || telefono.isBlank()) {
                                msg = "Completa nombre y teléfono."
                            } else {
                                vetVm.agregarDueno(nombre.trim(), telefono.trim())
                                msg = "Dueño guardado ✅"
                                nombre = ""
                                telefono = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Guardar") }

                    msg?.let { Text(it) }
                }
            }
        }

        items(duenos, key = { it.id }) { d ->
            ElevatedCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(d.nombre, style = MaterialTheme.typography.titleMedium)
                    Text("Tel: ${d.telefono}")
                }
            }
        }
    }
}
