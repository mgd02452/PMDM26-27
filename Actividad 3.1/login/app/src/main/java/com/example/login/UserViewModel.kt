package com.example.login

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// [RETO 8]: ViewModel compartido que guarda los datos del usuario logueado
class UserViewModel : ViewModel() {

    // Estado PRIVADO y mutable: solo el ViewModel puede cambiarlo.
    private val _email = MutableStateFlow("")
    private val _nombre = MutableStateFlow("") // [RETO 3 - NOMBRE]: Home también muestra el nombre

    // Estado PÚBLICO e inmutable: las pantallas solo lo observan.
    val email: StateFlow<String> = _email.asStateFlow()
    val nombre: StateFlow<String> = _nombre.asStateFlow()

    // Lo llaman Login y Registro al tener éxito
    fun login(email: String, nombre: String) {
        _email.value = email
        _nombre.value = nombre
    }

    // Al cerrar sesión se limpia todo
    fun logout() {
        _email.value = ""
        _nombre.value = ""
    }
}