package com.pablozavfer.cuadernopredicas.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SermonDao {
    @Query("SELECT * FROM sermons")
    fun observeAll(): Flow<List<SermonEntity>>

    @Query("SELECT * FROM sermons")
    suspend fun getAll(): List<SermonEntity>

    @Query("SELECT * FROM sermons WHERE id = :id")
    suspend fun getById(id: Long): SermonEntity?

    @Insert
    suspend fun insert(entity: SermonEntity): Long

    /** Se ejecuta en una sola transacción: o se insertan todas o ninguna. */
    @Insert
    suspend fun insertAll(entities: List<SermonEntity>)

    @Update
    suspend fun update(entity: SermonEntity)

    @Query("DELETE FROM sermons WHERE id = :id")
    suspend fun deleteById(id: Long)
}
