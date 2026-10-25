package com.pablozavfer.cuadernopredicas.export

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.util.Dates

/** Sección con contenido, en el orden en que se predica. Se usa en el modo de predicación, el texto y el PDF. */
data class SermonSection(
    val heading: String,
    val body: String = "",
    val points: List<SermonPoint> = emptyList(),
)

fun Sermon.sections(includeNotes: Boolean = true): List<SermonSection> {
    val visiblePoints = points.filter { it.title.isNotBlank() || it.body.isNotBlank() }
    return buildList {
        if (context.isNotBlank()) add(SermonSection("Contexto del pasaje", context.trim()))
        if (introduction.isNotBlank()) add(SermonSection("Introducción", introduction.trim()))
        if (visiblePoints.isNotEmpty()) add(SermonSection("Puntos principales", points = visiblePoints))
        if (application.isNotBlank()) add(SermonSection("Aplicación práctica", application.trim()))
        if (conclusion.isNotBlank()) add(SermonSection("Conclusión", conclusion.trim()))
        if (includeNotes && notes.isNotBlank()) add(SermonSection("Notas adicionales", notes.trim()))
    }
}

fun pointHeading(index: Int, point: SermonPoint): String =
    "${index + 1}. ${point.title.trim().ifEmpty { "Punto ${index + 1}" }}"

/** Texto plano para compartir por mensajería o correo. */
fun Sermon.toShareText(): String = buildString {
    appendLine(displayTitle)
    if (scripture.isNotBlank()) appendLine("Texto: ${scripture.trim()}")
    preachDate?.let { appendLine("Fecha: ${Dates.long(it)}") }
    sections().forEach { section ->
        appendLine()
        appendLine(section.heading.uppercase(Dates.spanish))
        if (section.body.isNotEmpty()) appendLine(section.body)
        section.points.forEachIndexed { i, point ->
            if (i > 0) appendLine()
            appendLine(pointHeading(i, point))
            if (point.body.isNotBlank()) appendLine(point.body.trim())
        }
    }
    appendLine()
    append("— Preparado con Mi Cuaderno de Prédicas")
}
