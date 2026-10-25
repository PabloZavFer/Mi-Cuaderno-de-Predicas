package com.pablozavfer.cuadernopredicas.ui.library

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import com.pablozavfer.cuadernopredicas.util.TextSearch
import java.time.LocalDate

enum class SortOrder(val label: String) {
    MODIFIED("Última modificación"),
    PREACH_DATE("Fecha de predicación"),
}

data class LibraryFilter(
    val query: String = "",
    val status: SermonStatus? = null,
    val tag: String? = null,
    val sort: SortOrder = SortOrder.MODIFIED,
) {
    val isActive: Boolean get() = query.isNotBlank() || status != null || tag != null
}

fun applyFilter(all: List<Sermon>, filter: LibraryFilter, today: LocalDate): List<Sermon> {
    val filtered = all.filter { sermon ->
        (filter.status == null || sermon.status == filter.status) &&
            (filter.tag == null || sermon.tags.any { it.equals(filter.tag, ignoreCase = true) }) &&
            TextSearch.matches(sermon, filter.query)
    }
    return when (filter.sort) {
        SortOrder.MODIFIED -> filtered.sortedByDescending { it.updatedAt }
        // Primero las próximas (la más cercana arriba), luego las pasadas (la más reciente arriba)
        // y al final las que no tienen fecha.
        SortOrder.PREACH_DATE -> {
            val (dated, undated) = filtered.partition { it.preachDate != null }
            val (upcoming, past) = dated.partition { !it.preachDate!!.isBefore(today) }
            upcoming.sortedBy { it.preachDate } +
                past.sortedByDescending { it.preachDate } +
                undated.sortedByDescending { it.updatedAt }
        }
    }
}

/** Etiquetas distintas de toda la biblioteca, en orden alfabético sin importar acentos. */
fun allTags(all: List<Sermon>): List<String> =
    all.flatMap { it.tags }
        .distinctBy { TextSearch.normalize(it) }
        .sortedBy { TextSearch.normalize(it) }
