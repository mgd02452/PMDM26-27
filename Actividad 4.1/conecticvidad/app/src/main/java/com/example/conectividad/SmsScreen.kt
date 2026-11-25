package com.example.conectividad

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun SmsScreen() {
    val context = LocalContext.current
    val smsPermission = rememberPermissionState(Manifest.permission.SEND_SMS)
    val receivePermission = rememberPermissionState(Manifest.permission.RECEIVE_SMS)
    val hasTelephony = context.packageManager.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)

    var phone by remember { mutableStateOf("5554") } // 5554 = puerto de consola del primer emulador (= su número por defecto)
    var message by remember { mutableStateOf("Hola desde Android") }
    var status by remember { mutableStateOf("") }

    // --- SMS entrantes --------------------------------------------------
    // El receptor publica en SmsBus; aquí nos suscribimos y lo mostramos.
    val incoming by SmsBus.received.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(incoming) {
        val sms = incoming ?: return@LaunchedEffect
        snackbarHostState.showSnackbar("SMS de ${sms.sender}: ${sms.body}")
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = phone, onValueChange = { phone = it },
                label = { Text("Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = message, onValueChange = { message = it },
                label = { Text("Mensaje") },
                modifier = Modifier.fillMaxWidth()
            )

            // Opción A: Intent → la app de mensajería, sin permiso
            OutlinedButton(
                onClick = {
                    status = if (sendSmsViaIntent(context, phone, message)) "Abierta la app de mensajería"
                    else "No hay ninguna app de SMS instalada"
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enviar con Intent (sin permiso)") }

            // Opción B: SmsManager → envío directo, con permiso
            if (smsPermission.status.isGranted) {
                Button(
                    onClick = {
                        sendSms(context, phone, message)
                        status = "SMS enviado a la cola (sin confirmación todavía: Reto 2)"
                    },
                    enabled = hasTelephony,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Enviar con SmsManager") }
            } else {
                Button(
                    onClick = { smsPermission.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Conceder permiso SEND_SMS") }
            }
            if (!hasTelephony) Text("Este dispositivo no tiene telefonía.")

            // Opción C: recibir los SMS que entran
            if (receivePermission.status.isGranted) {
                Text("Permiso RECEIVE_SMS concedido: tu app ya recibe los SMS entrantes.")
            } else {
                Button(
                    onClick = { receivePermission.launchPermissionRequest() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Conceder permiso RECEIVE_SMS") }
            }

            Text(status)
        }
    }
}