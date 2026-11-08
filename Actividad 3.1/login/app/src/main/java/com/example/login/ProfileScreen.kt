package com.example.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// [RETO 4]: pantalla de perfil con su propio Scaffold (decisión de diseño: un Scaffold por pantalla)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(userEmail: String, onGoHome: () -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.titulo_perfil)) }) },
        bottomBar = { AppBottomBar(selected = "profile", onHome = onGoHome, onProfile = {}) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                stringResource(R.string.perfil_texto),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(userEmail)
        }
    }
}

// [RETO 4]: barra inferior reutilizable (evita duplicar código en Home y Profile)
// Se usa NavigationBar porque M3 la recomienda para destinos y da el estado "seleccionado" gratis.
@Composable
fun AppBottomBar(selected: String, onHome: () -> Unit, onProfile: () -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == "home",
            onClick = onHome,
            // contentDescription = null: el label ya describe el icono
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_inicio)) }
        )
        NavigationBarItem(
            selected = selected == "profile",
            onClick = onProfile,
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text(stringResource(R.string.nav_perfil)) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    MaterialTheme {
        ProfileScreen(userEmail = "alumno@ejemplo.com", onGoHome = {})
    }
}