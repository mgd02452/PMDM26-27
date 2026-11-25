package com.example.conectividad

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun BluetoothScreen() {
    PermissionsGate(
        permissions = bluetoothPermissions(),
        rationale = "Para buscar y conectar dispositivos Bluetooth necesitamos tu permiso."
    ) {
        BluetoothContent()
    }
}

@Composable
private fun BluetoothContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val devices = remember { mutableStateListOf<BluetoothDevice>() }
    var scanning by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    val controller = remember {
        BluetoothController(
            context = context,
            onDeviceFound = { d -> if (devices.none { it.address == d.address }) devices.add(d) },
            onDiscoveryFinished = { scanning = false }
        )
    }
    // Al salir de la pantalla: parar el escaneo y liberar el receptor
    DisposableEffect(Unit) { onDispose { controller.stopDiscovery() } }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (!controller.isAvailable) {
            Text("Este dispositivo no tiene Bluetooth.")
            return@Column
        }
        if (!controller.isEnabled) {
            Text("Activa el Bluetooth para continuar.")
            return@Column
        }

        Button(
            onClick = {
                devices.clear()
                scanning = controller.startDiscovery()
                status = if (scanning) "Buscando…" else "No se pudo iniciar la búsqueda"
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Buscar dispositivos") }

        if (scanning) LinearProgressIndicator(Modifier.fillMaxWidth().padding(top = 8.dp))
        Spacer(Modifier.height(8.dp))

        LazyColumn(Modifier.weight(1f)) {
            items(devices, key = { it.address }) { device ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(controller.nameOf(device) ?: "(sin nombre)")
                        Text(device.address)
                    }
                    OutlinedButton(onClick = { controller.pairDevice(device) }) { Text("Emparejar") }
                    Button(onClick = {
                        scanning = false
                        status = "Enviando…"
                        scope.launch {
                            status = controller.sendMessage(device, "Hola desde Android").fold(
                                onSuccess = { "Mensaje enviado" },
                                onFailure = { "Error: ${it.message}" }
                            )
                        }
                    }) { Text("Enviar") }
                }
            }
        }
        Text(status)
    }
}