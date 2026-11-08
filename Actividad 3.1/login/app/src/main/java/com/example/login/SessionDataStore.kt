package com.example.login

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// ---------------------------------------------------------
// Extensión sobre Context para obtener el DataStore.
// Se declara una sola vez por proceso (por eso es `val` de extensión).
// ---------------------------------------------------------
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "session_prefs"
)

// ---------------------------------------------------------
// Claves de las preferencias que vamos a guardar.
// ---------------------------------------------------------
private val KEY_EMAIL = stringPreferencesKey("email")
private val KEY_REMEMBER = booleanPreferencesKey("remember_session")

//Reto 5: Persistir el filtro con dataStore
private val KEY_FILTRO = stringPreferencesKey("filtro_genero")
class SessionDataStore(private val context: Context) {

    // ---------------------------------------------------------
    // Lectura: exponemos el email como Flow<String>.
    // Si no hay nada guardado, devuelve "".
    // ---------------------------------------------------------
    val emailFlow: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_EMAIL] ?: "" }

    // ---------------------------------------------------------
    // Lectura: ¿el usuario marcó "Recordar sesión"?
    // ---------------------------------------------------------
    val rememberFlow: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_REMEMBER] ?: false }

    // ---------------------------------------------------------
    // Escritura: guarda el email y el flag de "recordar".
    // Se llama desde una corrutina.
    // ---------------------------------------------------------
    suspend fun saveSession(email: String, remember: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_EMAIL] = email
            prefs[KEY_REMEMBER] = remember
        }
    }

    // ---------------------------------------------------------
    // Borrado: limpia la sesión (al cerrar sesión).
    // ---------------------------------------------------------
    suspend fun clearSession() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_EMAIL)
            prefs.remove(KEY_REMEMBER)
        }
    }
    //Reto 5: Persistir el filtro con dataStore
    //lectura del filtro guardado ("" si no hay)
    val filtroFlow: Flow<String> = context.dataStore.data
        .map{prefs -> prefs[KEY_FILTRO] ?: ""}
    //escritura del filtro
    suspend fun saveFiltro(filtro: String){
        context.dataStore.edit{prefs -> prefs[KEY_FILTRO] = filtro}
    }
}