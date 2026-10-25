package com.example.login

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
// [RETO 8]: para leer el StateFlow del ViewModel como estado de Compose
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
// [RETO 8]: para obtener el ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// [RETO 6]: duración de las transiciones en un solo sitio (ms)
private const val DURACION = 300

// ---------------------------------------------------------
// Definimos las rutas como constantes para evitar errores tipográficos.
// ---------------------------------------------------------
// [RETO 8]: las rutas ya no llevan argumentos (ni Uri.encode, ni problemas con @, + o /).
// Las funciones Routes.home(...) y Routes.profile(...) desaparecen.
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PROFILE = "profile" // [RETO 4]
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    // [RETO 8]: una sola instancia, ligada a la Activity: la comparten todos los destinos.
    // Sobrevive a la navegación y a la rotación de pantalla.
    val userViewModel: UserViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,

        // [RETO 6]: transiciones a nivel de NavHost (valen para TODAS las pantallas)

        // Navegación HACIA ADELANTE: la nueva pantalla ENTRA por la derecha
        enterTransition = {
            slideInHorizontally(tween(DURACION)) { fullWidth -> fullWidth }
        },
        // Navegación HACIA ADELANTE: la pantalla actual SALE un poco hacia la izquierda
        exitTransition = {
            slideOutHorizontally(tween(DURACION)) { fullWidth -> -fullWidth / 3 }
        },
        // Navegación HACIA ATRÁS: la pantalla anterior VUELVE desde la izquierda
        popEnterTransition = {
            slideInHorizontally(tween(DURACION)) { fullWidth -> -fullWidth / 3 }
        },
        // Navegación HACIA ATRÁS: la pantalla actual SE VA hacia la derecha
        popExitTransition = {
            slideOutHorizontally(tween(DURACION)) { fullWidth -> fullWidth }
        }
    ) {
        // --- LOGIN ---
        composable(
            route = Routes.LOGIN,
            // [RETO 6]: caso especial, al cerrar sesión entra con fundido
            enterTransition = { fadeIn(tween(DURACION)) }
        ) {
            LoginScreen(
                // [RETO 3 - NOMBRE]: el callback recibe email y nombre
                onLoginSuccess = { email, nombre ->
                    // [RETO 8]: guardamos en el ViewModel en vez de pasarlos por la ruta
                    userViewModel.login(email, nombre)
                    navController.navigate(Routes.HOME) {
                        // Evita que al volver atrás se vuelva al login
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGoToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        // --- REGISTRO ---
        composable(Routes.REGISTER) {
            RegisterScreen(
                // [RETO 3 - NOMBRE]: el callback recibe email y nombre
                onRegisterSuccess = { email, nombre ->
                    // [RETO 8]: guardamos en el ViewModel
                    userViewModel.login(email, nombre)
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- HOME ---
        composable(Routes.HOME) {
            // [RETO 8]: collectAsState convierte el StateFlow en un State que Compose observa
            val email by userViewModel.email.collectAsState()
            val nombre by userViewModel.nombre.collectAsState()
            HomeScreen(
                userEmail = email,
                userName = nombre, // [RETO 3 - NOMBRE]
                onLogout = {
                    userViewModel.logout() // [RETO 8]: limpiamos el estado
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                // [RETO 4]: navegar a Perfil desde la barra inferior
                onGoProfile = {
                    navController.navigate(Routes.PROFILE)
                }
            )
        }

        // --- [RETO 4] PERFIL ---
        composable(Routes.PROFILE) {
            // [RETO 8]: el email también viene del ViewModel
            val email by userViewModel.email.collectAsState()
            ProfileScreen(
                userEmail = email,
                // [RETO 4]: popBackStack vuelve a Home sin crear otra copia en la pila
                onGoHome = { navController.popBackStack() }
            )
        }
    }
}