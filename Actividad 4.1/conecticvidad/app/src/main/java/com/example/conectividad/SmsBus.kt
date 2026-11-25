package com.example.conectividad

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Un SMS que acaba de entrar en el dispositivo. */
data class SmsReceived(val sender: String, val body: String, val id: Long)

/**
 * Puente entre el receptor y la UI.
 *
 * El id no es un detalle: un StateFlow NO vuelve a emitir si el valor nuevo es
 * igual al anterior, así que dos SMS idénticos seguidos (el mismo texto del
 * mismo emisor) se comerían el segundo. Con un id incremental cada aviso es único.
 */
object SmsBus {
    private var nextId = 0L
    private val _received = MutableStateFlow<SmsReceived?>(null)
    val received = _received.asStateFlow()

    fun publish(sender: String, body: String) {
        _received.value = SmsReceived(sender, body, nextId++)
    }
}