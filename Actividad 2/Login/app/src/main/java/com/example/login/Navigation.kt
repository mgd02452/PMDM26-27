package com.example.login

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Definimos una clase sellada para representar los destinos posibles de la app
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Home : Screen("home_screen")
}

@Composable
fun AppNavigation() {
    // 1. Controlador de navegación que lleva el historial
    val navController = rememberNavController()

    // 2. Contenedor de pantallas
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route // Pantalla inicial
    ) {
        // Ruta 1: LoginScreen
        composable(route = Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // Al iniciar sesión con éxito, navegamos a Home
                    navController.navigate(Screen.Home.route) {
                        // Eliminamos Login del historial para que al pulsar 'Atrás' no vuelva al login
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // Ruta 2: HomeScreen
        composable(route = Screen.Home.route) {
            HomeScreen()
        }
    }
}