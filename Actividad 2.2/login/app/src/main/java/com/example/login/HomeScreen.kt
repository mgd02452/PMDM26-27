package com.example.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Info //reto 2: icono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userEmail: String,
    userName: String, // [RETO 3 - NOMBRE]
    onLogout: () -> Unit,
    onGoProfile: () -> Unit // [RETO 4]: navegar a Perfil desde la barra inferior
) {
    // ---------------------------------------------------------
    // ESTADO: controla el menú overflow y el diálogo
    // ---------------------------------------------------------
    var menuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    //Reto 1: controla el diálogo de confirmación de cierre de sesión
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Reto 2: acceso directo a "Acerca de"
                    IconButton(onClick = { showAboutDialog = true }) {   // reutiliza el MISMO estado
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = stringResource(R.string.acerca_de_icono) // TalkBack
                        )
                    }
                    // --- Botón del menú overflow ---
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.abrir_menu)
                        )
                    }

                    // --- Menú desplegable ---
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_acerca_de)) },
                            onClick = {
                                menuExpanded = false
                                showAboutDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.menu_cerrar_sesion)) },
                            onClick = {
                                menuExpanded = false
                                //onLogout()
                                showLogoutDialog = true //reto 1: pedir confirmacion
                            }
                        )
                    }
                }
            )
        },
        // [RETO 4]: barra inferior; en Home, "Inicio" no hace nada porque ya estamos aquí
        bottomBar = {
            AppBottomBar(
                selected = "home",
                onHome = {},
                onProfile = onGoProfile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // [RETO 3 - NOMBRE]: saludo con el nombre (si llega vacío, saludo genérico)
            Text(
                text = if (userName.isNotEmpty()) {
                    stringResource(R.string.home_bienvenida_nombre, userName)
                } else {
                    stringResource(R.string.home_sin_email)
                },
                style = MaterialTheme.typography.headlineMedium
            )

            // [RETO 3 - NOMBRE]: el email se muestra debajo del saludo
            Text(
                text = userEmail,
                style = MaterialTheme.typography.bodyLarge
            )

            // Aquí se añadirán en futuras actividades:
            // - Lista de elementos (Room)
            // - Lista de la API (Retrofit)
            // - Botones de navegación a Mapas, Bluetooth, etc.
        }
    }

    // ---------------------------------------------------------
    // DIÁLOGO "Acerca de"
    // ---------------------------------------------------------
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.dialog_acerca_titulo)) },
            text = { Text(stringResource(R.string.dialog_acerca_texto)) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.dialog_aceptar))
                }
            }
        )
    }

    //reto 1: cerrar sesion
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },   // tocar fuera / atrás = cancelar
            title = { Text(stringResource(R.string.dialog_logout_titulo)) },
            text = { Text(stringResource(R.string.dialog_logout_texto)) },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false   // cerramos el diálogo
                    onLogout()                 // y SOLO ahora cerramos sesión
                }) { Text(stringResource(R.string.dialog_aceptar)) }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(stringResource(R.string.dialog_cancelar))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            userEmail = "alumno@ejemplo.com",
            userName = "Ana María",
            onLogout = {},
            onGoProfile = {} // [RETO 4]
        )
    }
}