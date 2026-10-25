package com.pablozavfer.cuadernopredicas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local. Al cambiar el esquema, sube [version] y agrega una Migration:
 * nunca uses fallbackToDestructiveMigration, porque borraría las prédicas del usuario.
 */
@Database(entities = [SermonEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sermonDao(): SermonDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "cuaderno.db").build()
    }
}
