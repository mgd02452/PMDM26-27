package com.example.login

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// [RETO 7]: extensión a nivel de archivo: UNA sola instancia de DataStore por proceso.
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

object UserPreferences {
    private val EMAIL_KEY = stringPreferencesKey("saved_email")

    // Lectura: un Flow que emite cada vez que cambia el valor guardado.
    fun emailFlow(context: Context): Flow<String> =
        context.dataStore.data.map { prefs -> prefs[EMAIL_KEY] ?: "" }

    // Escritura: función suspend (hay que llamarla desde una corrutina).
    suspend fun saveEmail(context: Context, email: String) {
        context.dataStore.edit { prefs -> prefs[EMAIL_KEY] = email }
    }

    // Borrado: cuando el usuario NO marca la casilla.
    suspend fun clearEmail(context: Context) {
        context.dataStore.edit { prefs -> prefs.remove(EMAIL_KEY) }
    }
}