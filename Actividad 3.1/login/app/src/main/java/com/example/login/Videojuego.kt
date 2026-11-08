package com.example.login

import androidx.room.Entity
import androidx.room.PrimaryKey
//Reto 6: exportar lista a JSON
import kotlinx.serialization.Serializable

@Serializable //permiteconvertir la clase a JSON //Reto 6: exportar lista a JSON
// ---------------------------------------------------------
// ENTIDAD: representa una fila de la tabla "videojuegos".
// Room genera automáticamente la tabla a partir de esta clase.
// ---------------------------------------------------------
@Entity(tableName = "videojuegos")
data class Videojuego(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,                  // 0 = Room asigna el ID automáticamente

    val titulo: String,
    val plataforma: String,           // "PS5", "Switch", "PC"...
    val genero: String,               // "RPG", "Aventura", "Shooter"...
    val anyo: Int,                    // 1998, 2023...
    val nota: Float,                  // 1.0 a 10.0
    val completado: Boolean = false   // ¿Lo has terminado?
)