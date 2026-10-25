package com.pablozavfer.cuadernopredicas.data.model

import java.time.LocalDate
import java.util.UUID

enum class SermonStatus(val label: String) {
    DRAFT("Borrador"),
    READY("Lista"),
    PREACHED("Predicada");

    companion object {
        fun fromKey(key: String?): SermonStatus? = entries.firstOrNull { it.name == key }
    }
}

data class SermonPoint(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val body: String = "",
)

/** Prédica tal como la usa la app. [uid] es estable y sirve para detectar duplicados en respaldos. */
data class Sermon(
    val id: Long = 0,
    val uid: String = UUID.randomUUID().toString(),
    val title: String = "",
    val scripture: String = "",
    val preachDate: LocalDate? = null,
    val context: String = "",
    val introduction: String = "",
    val points: List<SermonPoint> = emptyList(),
    val application: String = "",
    val conclusion: String = "",
    val notes: String = "",
    val status: SermonStatus = SermonStatus.DRAFT,
    val tags: List<String> = emptyList(),
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = createdAt,
) {
    val displayTitle: String get() = title.ifBlank { "Sin título" }

    /** Compara solo el contenido escrito, ignorando identificadores y fechas internas. */
    fun sameContentAs(other: Sermon): Boolean = contentKey() == other.contentKey()

    private fun contentKey() = copy(
        id = 0,
        uid = "",
        createdAt = 0,
        updatedAt = 0,
        points = points.map { it.copy(id = "") },
    )
}
