package com.example.login

import kotlinx.coroutines.flow.Flow

//Reto 2: Filtro por genero
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest

//Reto 4: ordenar por titulo, nota, anyo
import kotlinx.coroutines.flow.combine

//Reto 5: Persistir el filtro con dataStore
import kotlinx.coroutines.flow.first

//Reto 6: exportar lista a JSON
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

// ---------------------------------------------------------
// REPOSITORIO: capa intermedia entre la UI y las fuentes de datos.
//
// Une Room (videojuegos) y DataStore (sesión) en una sola API.
// La UI solo conoce esta clase; no sabe de Room ni de DataStore.
// ---------------------------------------------------------

//Reto 2: Filtar por genero
@OptIn(ExperimentalCoroutinesApi::class)

//Reto 4: flitrar por titulo, nota, anyo
private val _orden= MutableStateFlow(Orden.TITULO)
val orden: StateFlow<Orden> = _orden.asStateFlow()
fun setOrden(nuevo: Orden){
    _orden.value = nuevo
}
class VideojuegoRepository(
    private val dao: VideojuegoDao,
    private val session: SessionDataStore
) {

    // ---------------------------------------------------------
    // VIDEOJUEGOS (vienen de Room)
    // ---------------------------------------------------------

    //Reto 2: Filtar por genero
    private val  _filtro = MutableStateFlow("")
    val filtro: StateFlow<String> = _filtro.asStateFlow()

    //Reto 4: flitrar por titulo, nota, anyo
    val videojuegos: Flow<List<Videojuego>> = _filtro
        .flatMapLatest { texto -> dao.getByGenero(texto.trim()) }
        .combine(_orden){lista, orden ->
            when(orden){
                Orden.TITULO ->lista.sortedBy{it.titulo.lowercase()}
                Orden.NOTA -> lista.sortedByDescending{it.nota}
                Orden.ANYO -> lista.sortedByDescending{it.anyo}
            }
        }
    fun setFiltro(texto:String){
        _filtro.value = texto
    }
    // Consulta reactiva: la UI se actualiza sola cuando cambia la tabla.
    //val videojuegos: Flow<List<Videojuego>> = dao.getAll()

    // ---------------------------------------------------------
    // Operaciones de escritura.
    // Son `suspend` porque Room las ejecuta fuera del hilo principal.
    // ---------------------------------------------------------
    suspend fun insertar(videojuego: Videojuego) = dao.insert(videojuego)

    suspend fun actualizar(videojuego: Videojuego) = dao.update(videojuego)

    suspend fun borrar(videojuego: Videojuego) = dao.delete(videojuego)

    suspend fun borrarTodo() = dao.deleteAll()

    // ---------------------------------------------------------
    // SESIÓN (viene de DataStore)
    // ---------------------------------------------------------

    val emailUsuario: Flow<String> = session.emailFlow

    suspend fun guardarSesion(email: String, remember: Boolean) =
        session.saveSession(email, remember)

    suspend fun cerrarSesion() = session.clearSession()

    //Reto 5: Persistir el filtro con dataStore
    //lee el filtro guardado en dataStore y lo aplica (se llama al arrancar)
    suspend fun cargarFiltroGuardado(){
        _filtro.value = session.filtroFlow.first() //coge solo el primer valor
    }
    suspend fun guardarFiltro(texto: String) = session.saveFiltro(texto)

    //Reto 6: exportar lista a JSON
    //exporta todos los videojuegos (sin filtro) a videojuegos.json.
    //devuelve la ruta del archivo creado
    suspend fun exportarJson(context: Context): String{
        val lista = dao.getAll().first() //lista actual del Room
        //prettyPrint = true -> JSON con sangrias, legible
        val json = Json{prettyPrint = true}
            .encodeToString(ListSerializer(Videojuego.serializer()), lista)
        //carpeta externa propia de la app (no pide permisos). si no existe, usa filesDir.
        val carpeta = context.getExternalFilesDir(null) ?: context.filesDir
        val archivo = File(carpeta, "videojuegos.json")
        //escribir en disco: siempre fuera del hilo principal
        withContext(Dispatchers.IO){archivo.writeText(json)}
        return archivo.absolutePath
    }
}