package com.example.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch

// ---------------------------------------------------------
// Definimos las rutas como constantes para evitar errores tipográficos.
// ---------------------------------------------------------
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val session = SessionDataStore(context)
    val scope = rememberCoroutineScope()

    // ---------------------------------------------------------
    // Base de datos y repositorio.
    //
    // remember { } evita recrear el repositorio en cada recomposición.
    // AppDatabase.getInstance es un singleton, así que siempre devuelve
    // la misma instancia.
    // ---------------------------------------------------------
    val repository = remember {
        val db = AppDatabase.getInstance(context)
        VideojuegoRepository(db.videojuegoDao(), session)
    }

    // ---------------------------------------------------------
    // AUTO-LOGIN: leemos el flag "remember" de DataStore ANTES de
    // componer el NavHost. collectAsState(initial = null) devuelve
    // null mientras no llega el primer valor, y luego el valor real.
    //
    // Mientras sea null, no componemos nada (pantalla en blanco).
    // Es un instante muy breve.
    // ---------------------------------------------------------
    val rememberSession by session.rememberFlow.collectAsState(initial = null)

    // Si aún no sabemos si hay sesión guardada, no componemos el NavHost.
    if (rememberSession == null) return

    // Si el usuario marcó "Recordar sesión", arrancamos en Home.
    // Si no, en Login.
    val startDestination = if (rememberSession == true) Routes.HOME else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- LOGIN ---
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { email, remember ->
                    scope.launch {
                        session.saveSession(email, remember)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
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
                onRegisterSuccess = { email, _ ->
                    scope.launch {
                        session.saveSession(email, remember = true)
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }

        // --- HOME ---
        composable(Routes.HOME) {
            // La factory construye el ViewModel con el repositorio.
            val factory = remember { VideojuegoViewModelFactory(repository) }
            val viewModel: VideojuegoViewModel = viewModel(factory = factory)

            HomeScreen(
                viewModel = viewModel,
                onLogout = {
                    scope.launch {
                        session.clearSession()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}