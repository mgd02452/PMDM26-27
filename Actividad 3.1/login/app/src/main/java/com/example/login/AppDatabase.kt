package com.example.login

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// ---------------------------------------------------------
// BASE DE DATOS: declara las entidades y la versión del esquema.
// Room genera la implementación.
// ---------------------------------------------------------
@Database(
    entities = [Videojuego::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // ---------------------------------------------------------
    // Expone el DAO para poder usarlo desde fuera.
    // ---------------------------------------------------------
    abstract fun videojuegoDao(): VideojuegoDao

    // ---------------------------------------------------------
    // Companion object: patrón Singleton.
    // Solo debe existir UNA instancia de la base de datos en toda la app.
    // ---------------------------------------------------------
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "login.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}