package com.example.veterinariaapp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val isStaff = !isOwner

    val tabs = remember(isOwner) {
        if (isOwner) {
            listOf(
                TabItem("home", "Inicio") { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                TabItem("pets", "Mis mascotas") { Icon(Icons.Filled.Pets, contentDescription = "Mis mascotas") },
                TabItem("consultas", "Mis consultas") {
                    Icon(Icons.Filled.MedicalServices, contentDescription = "Mis consultas")
                }
            )
        } else {
            listOf(
                TabItem("home", "Inicio") { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
                TabItem("duenos", "Duenos") { Icon(Icons.Filled.Person, contentDescription = "Duenos") },
                TabItem("pets", "Mascotas") { Icon(Icons.Filled.Pets, contentDescription = "Mascotas") },
                TabItem("consultas", "Consultas") {
                    Icon(Icons.Filled.MedicalServices, contentDescription = "Consultas")
                },
                TabItem("agenda", "Agenda") { Icon(Icons.Filled.Event, contentDescription = "Agenda") }
            )
        }
    }

    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: tabs.first().route
    val selectedTabRoute = if (currentRoute == "log") "home" else currentRoute

    val currentTitle = remember(currentRoute, tabs) {
        if (currentRoute == "log") "Actividad" else tabs.firstOrNull { it.route == currentRoute }?.label ?: "VeterinariaApp"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentTitle) },
                actions = {
                    TextButton(onClick = { vetVm.cargarResumenConProgreso() }) { Text("Refrescar") }
                    if (isStaff) {
                        IconButton(
                            onClick = {
                                nav.navigate("log") {
                                    launchSingleTop = true
                                    popUpTo("home") {
                                        saveState = true
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Filled.History, contentDescription = "Ver actividad")
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Salir")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTabRoute == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(nav.graph.startDestinationId) { saveState = true }
                            }
                        },
                        icon = tab.icon,
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
                composable("home") { HomeScreen(vetVm = vetVm, isOwner = isOwner) }
                if (isStaff) composable("duenos") { DuenosScreen(vetVm = vetVm) }
                composable("pets") { MascotasScreen(vetVm = vetVm, authVm = authVm) }
                composable("consultas") { ConsultasScreen(vetVm = vetVm, authVm = authVm) }
                if (isStaff) composable("agenda") { AgendaScreen(vetVm = vetVm) }
                if (isStaff) composable("log") { ActivityLogScreen(vetVm = vetVm) }
            }
        }
    }
}
