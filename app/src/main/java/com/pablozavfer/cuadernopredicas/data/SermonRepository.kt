package com.pablozavfer.cuadernopredicas.data

import com.pablozavfer.cuadernopredicas.data.local.SermonDao
import com.pablozavfer.cuadernopredicas.data.local.toDomain
import com.pablozavfer.cuadernopredicas.data.local.toEntity
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.util.UUID

class SermonRepository(private val dao: SermonDao) {

    fun observeAll(): Flow<List<Sermon>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Sermon? = dao.getById(id)?.toDomain()

    suspend fun getAll(): List<Sermon> = dao.getAll().map { it.toDomain() }

    /** Inserta si la prédica es nueva (id = 0) o la actualiza. Devuelve su id. */
    suspend fun save(sermon: Sermon): Long =
        if (sermon.id == 0L) {
            dao.insert(sermon.toEntity())
        } else {
            dao.update(sermon.toEntity())
            sermon.id
        }

    suspend fun delete(id: Long) = dao.deleteById(id)

    /** Crea una copia como borrador y sin fecha, para no duplicar eventos en el calendario. */
    suspend fun duplicate(id: Long): Long? {
        val original = get(id) ?: return null
        val now = System.currentTimeMillis()
        val copy = original.copy(
            id = 0,
            uid = UUID.randomUUID().toString(),
            title = "${original.displayTitle} (copia)",
            points = original.points.map { it.copy(id = UUID.randomUUID().toString()) },
            status = SermonStatus.DRAFT,
            preachDate = null,
            isSample = false,
            createdAt = now,
            updatedAt = now,
        )
        return dao.insert(copy.toEntity())
    }

    suspend fun insertAll(sermons: List<Sermon>) {
        if (sermons.isNotEmpty()) dao.insertAll(sermons.map { it.copy(id = 0).toEntity() })
    }

    /** Agrega los ejemplos que falten (por ejemplo, si el usuario los borró). Devuelve cuántos agregó. */
    suspend fun addMissingSamples(today: LocalDate = LocalDate.now()): Int {
        val existing = dao.getAll().map { it.uid }.toSet()
        val missing = SampleContent.build(today).filter { it.uid !in existing }
        insertAll(missing)
        return missing.size
    }
}
