package com.example.login

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// RobolectricTestRunner: ejecuta el test con un Android simulado en el PC.
@RunWith(RobolectricTestRunner::class)
class VideojuegoRepositoryTest {

    private lateinit var db: AppDatabase
    private lateinit var repo: VideojuegoRepository

    // Se ejecuta ANTES de cada test: base de datos limpia y en memoria.
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()   // solo en tests
            .build()
        // Los tests no usan la sesión, así que basta con una instancia normal.
        // (SessionDataStore es una clase final, por eso no hacemos un Fake.)
        repo = VideojuegoRepository(db.videojuegoDao(), SessionDataStore(context))
    }

    // Se ejecuta DESPUÉS de cada test: cierra la base de datos.
    @After
    fun tearDown() {
        db.close()
    }

    // Videojuego de ejemplo para no repetir código
    private fun juego(titulo: String = "Zelda") = Videojuego(
        titulo = titulo,
        plataforma = "Switch",
        genero = "Aventura",
        anyo = 2017,
        nota = 9.5f
    )

    // runTest permite llamar a funciones suspend dentro del test
    @Test
    fun insertar_y_leer() = runTest {
        repo.insertar(juego("Zelda"))

        val lista = repo.videojuegos.first()   // first() = foto actual del Flow

        assertEquals(1, lista.size)
        assertEquals("Zelda", lista[0].titulo)
    }

    @Test
    fun actualizar_persiste_el_cambio() = runTest {
        repo.insertar(juego("Zelda"))
        val original = repo.videojuegos.first()[0]   // ya trae el id asignado por Room

        repo.actualizar(original.copy(titulo = "Zelda BOTW", completado = true))

        val lista = repo.videojuegos.first()
        assertEquals(1, lista.size)                  // no se ha duplicado
        assertEquals("Zelda BOTW", lista[0].titulo)
        assertTrue(lista[0].completado)
    }

    @Test
    fun borrar_elimina_el_videojuego() = runTest {
        repo.insertar(juego("Zelda"))
        repo.insertar(juego("Elden Ring"))
        val zelda = repo.videojuegos.first().first { it.titulo == "Zelda" }

        repo.borrar(zelda)

        val lista = repo.videojuegos.first()
        assertEquals(1, lista.size)
        assertEquals("Elden Ring", lista[0].titulo)
    }

    @Test
    fun borrarTodo_deja_la_lista_vacia() = runTest {
        repo.insertar(juego("Zelda"))
        repo.insertar(juego("Elden Ring"))

        repo.borrarTodo()

        assertTrue(repo.videojuegos.first().isEmpty())
    }
}