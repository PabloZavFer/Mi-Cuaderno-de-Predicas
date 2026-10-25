package com.pablozavfer.cuadernopredicas.util

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import java.text.Normalizer
import java.util.Locale

/** Búsqueda que ignora mayúsculas y acentos ("predica" encuentra "Prédica"). */
object TextSearch {
    private val diacritics = Regex("\\p{Mn}+")
    private val whitespace = Regex("\\s+")

    fun normalize(text: String): String =
        Normalizer.normalize(text, Normalizer.Form.NFD)
            .replace(diacritics, "")
            .lowercase(Locale.ROOT)

    /** Cada palabra de la búsqueda debe aparecer en el título, la referencia, las etiquetas o el contenido. */
    fun matches(sermon: Sermon, query: String): Boolean {
        val terms = normalize(query).trim().split(whitespace).filter { it.isNotEmpty() }
        if (terms.isEmpty()) return true
        val haystack = normalize(searchableText(sermon))
        return terms.all { haystack.contains(it) }
    }

    private fun searchableText(s: Sermon): String = buildString {
        listOf(s.title, s.scripture, s.context, s.introduction, s.application, s.conclusion, s.notes)
            .forEach { append(it).append('\n') }
        s.points.forEach { append(it.title).append('\n').append(it.body).append('\n') }
        s.tags.forEach { append(it).append('\n') }
    }
}
