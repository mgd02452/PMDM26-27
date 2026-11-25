package com.example.conectividad

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/** Permisos de Bluetooth que hay que pedir EN EJECUCIÓN según la versión de Android. */
fun bluetoothPermissions(): List<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        // BLUETOOTH y BLUETOOTH_ADMIN son permisos "normales": se conceden al instalar
        listOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

/** Muestra [content] solo si todos los permisos están concedidos; si no, explica y ofrece pedirlos. */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionsGate(
    permissions: List<String>,
    rationale: String,
    content: @Composable () -> Unit
) {
    val state = rememberMultiplePermissionsState(permissions)

    if (state.allPermissionsGranted) {
        content()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(rationale, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { state.launchMultiplePermissionRequest() }) { Text("Conceder permisos") }
            Spacer(Modifier.height(8.dp))
            Text(
                "Si los has denegado dos veces, concédelos en Ajustes → Aplicaciones → Permisos.",
                textAlign = TextAlign.Center
            )
        }
    }
}