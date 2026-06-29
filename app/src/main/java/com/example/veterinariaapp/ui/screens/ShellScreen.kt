package com.example.veterinariaapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel

private data class TabItem(
    val route: String,
    val label: String,
    val icon: @Composable () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShellScreen(
    authVm: AuthViewModel,
    vetVm: VetViewModel,
    onLogout: () -> Unit
) {
    val session by authVm.session.collectAsState()
    val isOwner = session?.rol == "OWNER"

    val tabs = remember(isOwner) {
        if (isOwner) {
            listOf(
                TabItem("home", "Inicio") { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                TabItem("pets", "Mis mascotas") { Icon(Icons.Filled.Pets, contentDescription = "Mis mascotas") },
                TabItem("consultas", "Mis consultas") { Icon(Icons.Filled.MedicalServices, contentDescription = "Mis consultas") },
                TabItem("log", "Actividad") { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Actividad") }
            )
        } else {
            listOf(
                TabItem("home", "Inicio") { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                TabItem("duenos", "Dueños") { Icon(Icons.Filled.Person, contentDescription = "Dueños") },
                TabItem("pets", "Mascotas") { Icon(Icons.Filled.Pets, contentDescription = "Mascotas") },
                TabItem("consultas", "Consultas") { Icon(Icons.Filled.MedicalServices, contentDescription = "Consultas") },
                TabItem("agenda", "Agenda") { Icon(Icons.Filled.Event, contentDescription = "Agenda") },
                TabItem("log", "Actividad") { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Actividad") }
            )
        }
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: tabs.first().route

    val currentTitle = remember(currentRoute, tabs) {
        tabs.firstOrNull { it.route == currentRoute }?.label ?: "VeterinariaApp"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) }, // 👈 aquí va el nombre de la pantalla (ya no abajo)
                actions = {
                    TextButton(onClick = { vetVm.cargarResumenConProgreso() }) { Text("Refrescar") }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { t ->
                    val selected = currentRoute == t.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            nav.navigate(t.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = t.icon,
                        // 👇 clave: no renderizamos label (0 texto, 0 truncamiento)
                        label = null,
                        alwaysShowLabel = false
                    )
                }
            }
        }
    ) { inner ->
        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
            NavHost(
                navController = nav,
                startDestination = tabs.first().route,
                modifier = Modifier.padding(inner)
            ) {
                composable("home") { HomeScreen(vetVm = vetVm) }
                if (!isOwner) composable("duenos") { DuenosScreen(vetVm = vetVm) }
                composable("pets") { MascotasScreen(vetVm = vetVm, authVm = authVm) }
                composable("consultas") { ConsultasScreen(vetVm = vetVm, authVm = authVm) }
                if (!isOwner) composable("agenda") { AgendaScreen(vetVm = vetVm) }
                composable("log") { ActivityLogScreen(vetVm = vetVm) }
            }
        }
    }
}