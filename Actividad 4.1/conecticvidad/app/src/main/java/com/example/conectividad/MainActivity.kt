package com.example.conectividad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Si el asistente generó un tema (p. ej. ConectividadTheme), puedes usarlo en lugar de MaterialTheme
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { ConnectivityApp() }
            }
        }
    }
}