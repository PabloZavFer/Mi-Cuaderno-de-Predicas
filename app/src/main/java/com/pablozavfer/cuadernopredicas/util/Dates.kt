package com.pablozavfer.cuadernopredicas.util

import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

object Dates {
    val spanish: Locale = Locale.forLanguageTag("es")

    private val longFormat = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", spanish)
    private val shortFormat = DateTimeFormatter.ofPattern("EEE d MMM yyyy", spanish)
    private val monthFormat = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", spanish)

    /** "Domingo 1 de noviembre de 2026" */
    fun long(date: LocalDate): String = longFormat.format(date).capitalizeFirst()

    /** "Dom 1 nov 2026" */
    fun short(date: LocalDate): String = shortFormat.format(date).replace(".", "").capitalizeFirst()

    /** "Noviembre de 2026" */
    fun month(month: YearMonth): String = monthFormat.format(month).capitalizeFirst()

    private fun String.capitalizeFirst() = replaceFirstChar { it.titlecase(spanish) }
}
