package com.example.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
//Reto 4
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

//Reto 6: exportar lista a JSON
import android.content.Context

// ---------------------------------------------------------
// VIEWMODEL: contiene la lógica de negocio de la pantalla Home.
//
// - Sobrevive a los cambios de configuración (rotaciones).
// - Expone el estado a la UI mediante StateFlow.
// - Recibe acciones de la UI (insertar, borrar, etc.).
// - Habla con el repositorio, no directamente con Room/DataStore.
// ---------------------------------------------------------
class VideojuegoViewModel(
    private val repository: VideojuegoRepository
) : ViewModel() {

    // ---------------------------------------------------------
    // ESTADO: lista de videojuegos como StateFlow.
    //
    // stateIn convierte el Flow del repositorio en un StateFlow:
    //   - viewModelScope: se cancela cuando el ViewModel se destruye.
    //   - SharingStarted.WhileSubscribed(5000): mantiene el Flow activo
    //     mientras haya suscriptores. Si no hay nadie durante 5 segundos,
    //     lo pausa (ahorra recursos).
    //   - emptyList(): valor inicial mientras llega el primer dato.
    // ---------------------------------------------------------
    val videojuegos: StateFlow<List<Videojuego>> = repository.videojuegos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // ---------------------------------------------------------
    // ESTADO: email del usuario como StateFlow.
    // ---------------------------------------------------------
    val emailUsuario: StateFlow<String> = repository.emailUsuario
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ""
        )

    // Reto 4: ordenar por título, nota, año
    private val _orden = MutableStateFlow(Orden.TITULO)
    val orden: StateFlow<Orden> = _orden.asStateFlow()

    fun cambiarOrden(nuevo: Orden) {
        _orden.value = nuevo
    }

    //Reto 2: Filtar por genero
    val filtro: StateFlow<String> = repository.filtro

    //Reto 5: Persistir el filtro con dataStore
    init{
        //al crear el viewmodel recuperamos el filtro de la ultima vez
        viewModelScope.launch { repository.cargarFiltroGuardado() }
    }
    fun cambiarFiltro(texto: String){
        repository.setFiltro(texto)
        viewModelScope.launch { repository.guardarFiltro(texto) } //Reto 5: Persistir el filtro con dataStore
    }
    // ---------------------------------------------------------
    // ACCIONES: la UI llama a estas funciones.
    // Cada una lanza una corrutina en viewModelScope.
    // ---------------------------------------------------------

    fun insertar(videojuego: Videojuego) {
        viewModelScope.launch {
            repository.insertar(videojuego)
        }
    }

    fun actualizar(videojuego: Videojuego) {
        viewModelScope.launch {
            repository.actualizar(videojuego)
        }
    }

    fun borrar(videojuego: Videojuego) {
        viewModelScope.launch {
            repository.borrar(videojuego)
        }
    }

    fun borrarTodo() {
        viewModelScope.launch {
            repository.borrarTodo()
        }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            repository.cerrarSesion()
        }
    }

    //Reto 6: exportar lista a JSON
    //onTerminado recibe la ruta del archivo para que la UI avise al usuario
    fun exportarJson(context: Context, onTerminado: (String) -> Unit){
        viewModelScope.launch {
            val ruta = repository.exportarJson(context.applicationContext)
            onTerminado(ruta)
        }
    }
}

// ---------------------------------------------------------
// FACTORY: le dice a Compose cómo construir el ViewModel.
//
// El ViewModel necesita un repositorio, que no puede crear solo.
// La factory se lo pasa.
// ---------------------------------------------------------
class VideojuegoViewModelFactory(
    private val repository: VideojuegoRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VideojuegoViewModel::class.java)) {
            return VideojuegoViewModel(repository) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}