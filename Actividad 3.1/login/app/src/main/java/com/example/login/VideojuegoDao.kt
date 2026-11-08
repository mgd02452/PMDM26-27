package com.example.login

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// ---------------------------------------------------------
// DAO (Data Access Object): define las operaciones sobre la tabla.
// Room genera la implementación automáticamente.
// ---------------------------------------------------------
@Dao
interface VideojuegoDao {

    // ---------------------------------------------------------
    // LECTURA: devuelve todos los videojuegos como Flow.
    // Flow hace que la UI se actualice automáticamente
    // cuando cambia la tabla (consulta reactiva).
    // ---------------------------------------------------------
    @Query("SELECT * FROM videojuegos ORDER BY titulo ASC")
    fun getAll(): Flow<List<Videojuego>>

    // ---------------------------------------------------------
    // LECTURA: un videojuego por ID.
    // ---------------------------------------------------------
    @Query("SELECT * FROM videojuegos WHERE id = :id")
    fun getById(id: Int): Flow<Videojuego?>

    // ---------------------------------------------------------
    // ESCRITURA: insertar un videojuego nuevo.
    // ---------------------------------------------------------
    @Insert
    suspend fun insert(videojuego: Videojuego)

    // ---------------------------------------------------------
    // ESCRITURA: actualizar un videojuego existente.
    // ---------------------------------------------------------
    @Update
    suspend fun update(videojuego: Videojuego)

    // ---------------------------------------------------------
    // ESCRITURA: borrar un videojuego.
    // ---------------------------------------------------------
    @Delete
    suspend fun delete(videojuego: Videojuego)

    // ---------------------------------------------------------
    // ESCRITURA: borrar todos (útil para pruebas).
    // ---------------------------------------------------------
    @Query("DELETE FROM videojuegos")
    suspend fun deleteAll()

    //Reto 2: Filtra por genero
    @Query("SELECT * FROM videojuegos WHERE genero LIKE '%' || :filtro || '%' ORDER BY titulo ASC")
    fun getByGenero(filtro: String): Flow<List<Videojuego>>
}