package com.example.login

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// ---------------------------------------------------------
// Helper para gestionar notificaciones.
//
// - Crea el canal (obligatorio desde Android 8).
// - Muestra notificaciones.
// ---------------------------------------------------------
object NotificationHelper {

    private const val CHANNEL_ID = "login_videojuegos"
    private const val CHANNEL_NAME = "Videojuegos"
    private const val CHANNEL_DESC = "Notificaciones al añadir videojuegos"

    // ---------------------------------------------------------
    // Crea el canal. Es idempotente: si ya existe, no hace nada.
    // Se llama al arrancar la app (por ejemplo, en MainActivity).
    // ---------------------------------------------------------
    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESC
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    // ---------------------------------------------------------
    // Muestra una notificación.
    // Antes de llamar a esta función, hay que comprobar que
    // el permiso POST_NOTIFICATIONS está concedido.
    // ---------------------------------------------------------
    fun mostrarNotificacion(
        context: Context,
        titulo: String,
        mensaje: String
    ) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(
                System.currentTimeMillis().toInt(),
                notification
            )
        } catch (e: SecurityException) {
            // El permiso no está concedido. No hacemos nada.
        }
    }
}