package com.example.conectividad

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.IntentCompat
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission") // Cada método comprueba el permiso con has(...) antes de usar la API
class BluetoothController(
    private val context: Context,
    private val onDeviceFound: (BluetoothDevice) -> Unit,
    private val onDiscoveryFinished: () -> Unit = {}
) {
    private val adapter: BluetoothAdapter? =
        context.getSystemService(BluetoothManager::class.java)?.adapter

    val isAvailable: Boolean get() = adapter != null
    val isEnabled: Boolean get() = adapter?.isEnabled == true

    private var receiverRegistered = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = IntentCompat.getParcelableExtra(
                        intent, BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java
                    )
                    device?.let(onDeviceFound)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> onDiscoveryFinished()
            }
        }
    }

    // ---------- Permisos ----------
    private fun has(permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private fun canScan() =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) has(Manifest.permission.BLUETOOTH_SCAN)
        else has(Manifest.permission.ACCESS_FINE_LOCATION)

    private fun canConnect() =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || has(Manifest.permission.BLUETOOTH_CONNECT)

    // ---------- 1. DESCUBRIR ----------
    fun startDiscovery(): Boolean {
        val bt = adapter ?: return false
        if (!bt.isEnabled || !canScan()) return false

        if (!receiverRegistered) {
            val filter = IntentFilter().apply {
                addAction(BluetoothDevice.ACTION_FOUND)
                addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            }
            // ACTION_FOUND lo envía el sistema: el receptor debe estar "exported"
            ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_EXPORTED)
            receiverRegistered = true
        }
        if (bt.isDiscovering) bt.cancelDiscovery()
        return bt.startDiscovery()
    }

    fun stopDiscovery() {
        if (canScan()) adapter?.cancelDiscovery()
        if (receiverRegistered) {
            context.unregisterReceiver(receiver)
            receiverRegistered = false
        }
    }

    // ---------- 2. EMPAREJAR ----------
    fun pairDevice(device: BluetoothDevice): Boolean {
        if (!canConnect()) return false
        // El sistema muestra su propio diálogo (PIN o confirmación)
        return device.bondState == BluetoothDevice.BOND_NONE && device.createBond()
    }

    fun nameOf(device: BluetoothDevice): String? = if (canConnect()) device.name else null

    // ---------- 3. ENVIAR (RFCOMM) ----------
    suspend fun sendMessage(device: BluetoothDevice, message: String): Result<Unit> =
        withContext(Dispatchers.IO) { // connect() es BLOQUEANTE: nunca en el hilo principal
            if (!canConnect()) {
                return@withContext Result.failure(SecurityException("Falta el permiso BLUETOOTH_CONNECT"))
            }
            var socket: BluetoothSocket? = null
            try {
                // CRÍTICO: cancelar el descubrimiento antes de conectar
                if (canScan()) adapter?.cancelDiscovery()

                socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket.connect() // Bloqueante, pero estamos en Dispatchers.IO

                socket.outputStream.apply {
                    write(message.toByteArray())
                    flush()
                }
                Result.success(Unit)
            } catch (e: IOException) {
                Result.failure(e)
            } finally {
                try { socket?.close() } catch (_: IOException) { }
            }
        }

    // Opcional: añade este método a BluetoothController para recibir mensajes en otro móvil con tu app
    suspend fun listenForMessage(): Result<String> = withContext(Dispatchers.IO) {
        var server: BluetoothServerSocket? = null
        try {
            server = adapter?.listenUsingRfcommWithServiceRecord("PMDM", SPP_UUID)
                ?: return@withContext Result.failure(IOException("Sin Bluetooth"))
            val socket = server.accept()              // bloquea hasta que alguien conecta
            val text = socket.inputStream.bufferedReader().readText()
            socket.close()
            Result.success(text)
        } catch (e: IOException) {
            Result.failure(e)
        } finally {
            server?.close()
        }
    }

    companion object {
        /** UUID estándar del Serial Port Profile (SPP) */
        val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}