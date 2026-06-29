package com.example.veterinariaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.example.veterinaria.data.Repository
import com.example.veterinariaapp.ui.nav.AppNavGraph
import com.example.veterinariaapp.ui.theme.VeterinariaAppTheme
import com.example.veterinariaapp.viewmodel.AuthViewModel
import com.example.veterinariaapp.viewmodel.VetViewModel

class MainActivity : ComponentActivity() {

    private val authVm: AuthViewModel by viewModels()
    private val vetVm: VetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Repository.init(applicationContext)

        setContent {
            VeterinariaAppTheme {
                val navController = rememberNavController()
                AppNavGraph(
                    navController = navController,
                    authVm = authVm,
                    vetVm = vetVm
                )
            }
        }
    }
}
