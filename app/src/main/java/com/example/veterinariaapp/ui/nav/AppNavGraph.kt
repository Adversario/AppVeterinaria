package com.example.veterinariaapp.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.veterinariaapp.ui.screens.AuthLoginScreen
import com.example.veterinariaapp.ui.screens.ResetPasswordScreen
import com.example.veterinariaapp.ui.screens.ShellScreen
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel

object Routes {
    const val Login = "login"
    const val Reset = "reset"
    const val Shell = "shell"
}

@Composable
fun AppNavGraph(
    navController: NavHostController,
    authVm: AuthViewModel,
    vetVm: VetViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.Login
    ) {
        composable(Routes.Login) {
            AuthLoginScreen(
                authVm = authVm,
                onLoginOk = {
                    vetVm.cargarResumenConProgreso()
                    navController.navigate(Routes.Shell) {
                        popUpTo(Routes.Login) { inclusive = true }
                    }
                },
                onReset = { navController.navigate(Routes.Reset) }
            )
        }
        composable(Routes.Reset) {
            ResetPasswordScreen(
                authVm = authVm,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.Shell) {
            ShellScreen(
                authVm = authVm,
                vetVm = vetVm,
                onLogout = {
                    authVm.logout()
                    navController.navigate(Routes.Login) {
                        popUpTo(Routes.Shell) { inclusive = true }
                    }
                }
            )
        }
    }
}