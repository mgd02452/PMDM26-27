package com.example.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Crear el canal de notificaciones (idempotente).
        NotificationHelper.createChannel(this)

        setContent {
            MaterialTheme {
                AppNavGraph()
            }
        }
    }
}