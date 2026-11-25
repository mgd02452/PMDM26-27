package com.example.conectividad

import android.app.PendingIntent
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager

const val SMS_SENT_ACTION = "com.example.conectividad.SMS_SENT"

/** Envío directo (requiere el permiso SEND_SMS concedido). */
fun sendSms(context: Context, phoneNumber: String, message: String) {
    val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else {
        @Suppress("DEPRECATION")
        SmsManager.getDefault()
    }

    // Un SMS largo se divide en varias partes
    val parts = smsManager.divideMessage(message)

    // PendingIntent para saber si el SMS llegó al centro de mensajes
    val sentIntent = PendingIntent.getBroadcast(
        context, 0,
        Intent(SMS_SENT_ACTION).setPackage(context.packageName),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val sentIntents = ArrayList(List(parts.size) { sentIntent })

    smsManager.sendMultipartTextMessage(phoneNumber, null, parts, sentIntents, null)
}

/** Delegar en la app de mensajería del sistema (sin permiso SEND_SMS). */
fun sendSmsViaIntent(context: Context, phoneNumber: String, message: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("smsto:$phoneNumber")
        putExtra("sms_body", message)
    }
    return try {
        context.startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false // No hay ninguna app de SMS instalada
    }
}