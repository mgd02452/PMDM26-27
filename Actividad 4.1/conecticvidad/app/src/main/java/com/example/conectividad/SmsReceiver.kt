package com.example.conectividad

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony

/**
 * Receptor de SMS entrantes.
 *
 * Ojo: este objeto lo crea el SISTEMA (por eso lo declaramos en el Manifest),
 * no tu app. Por tanto no puede tocar la UI: solo publica el mensaje en el SmsBus.
 */
class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        // Devuelve un array: un SMS largo llega partido en varios trozos
        val parts = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        if (parts.isEmpty()) return

        // Hay que concatenar el cuerpo: si no, solo se ve la primera parte
        val body = parts.joinToString("") { it.messageBody }
        val sender = parts.firstOrNull()?.originatingAddress ?: "(número desconocido)"

        SmsBus.publish(sender, body)
    }
}