package com.pablozavfer.cuadernopredicas

import com.pablozavfer.cuadernopredicas.data.SermonTemplate
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import com.pablozavfer.cuadernopredicas.export.toShareText
import com.pablozavfer.cuadernopredicas.ui.library.LibraryFilter
import com.pablozavfer.cuadernopredicas.ui.library.SortOrder
import com.pablozavfer.cuadernopredicas.ui.library.allTags
import com.pablozavfer.cuadernopredicas.ui.library.applyFilter
import com.pablozavfer.cuadernopredicas.util.TextSearch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class LibraryFilterTest {
    private val today = LocalDate.of(2026, 10, 25)

    private val a = Sermon(
        id = 1, title = "El perdón de Dios", scripture = "Salmo 32", tags = listOf("Gracia"),
        status = SermonStatus.READY, preachDate = today.plusDays(10), updatedAt = 100,
    )
    private val b = Sermon(
        id = 2, title = "Caminar en fe", scripture = "Hebreos 11:8", status = SermonStatus.DRAFT,
        points = listOf(SermonPoint(title = "Abraham obedeció", body = "Salió sin saber adónde iba")),
        preachDate = today.minusDays(3), updatedAt = 300,
    )
    private val c = Sermon(
        id = 3, title = "Oración", scripture = "Mateo 6:5-15", tags = listOf("gracia", "Oración"),
        status = SermonStatus.PREACHED, preachDate = today.plusDays(2), updatedAt = 200,
    )
    private val d = Sermon(id = 4, title = "Sin fecha", updatedAt = 400)
    private val all = listOf(a, b, c, d)

    private fun ids(filter: LibraryFilter) = applyFilter(all, filter, today).map { it.id }

    @Test
    fun searchIgnoresCaseAndAccents() {
        assertEquals(listOf(1L), ids(LibraryFilter(query = "PERDON")))
        assertEquals(listOf(3L), ids(LibraryFilter(query = "oracion")))
        assertTrue(TextSearch.matches(c, "Oración"))
    }

    @Test
    fun searchFindsReferenceAndPointContent() {
        assertEquals(listOf(2L), ids(LibraryFilter(query = "hebreos 11")))
        assertEquals(listOf(3L), ids(LibraryFilter(query = "6:5")))
        assertEquals(listOf(2L), ids(LibraryFilter(query = "adonde")))
        assertTrue(ids(LibraryFilter(query = "palabra-inexistente")).isEmpty())
    }

    @Test
    fun allWordsMustMatch() {
        assertEquals(listOf(2L), ids(LibraryFilter(query = "fe abraham")))
        assertFalse(TextSearch.matches(b, "fe perdón"))
    }

    @Test
    fun filtersByStatusAndTag() {
        assertEquals(listOf(4L, 2L), ids(LibraryFilter(status = SermonStatus.DRAFT)))
        assertEquals(listOf(3L, 1L), ids(LibraryFilter(tag = "Gracia")))
    }

    @Test
    fun sortsByModificationNewestFirst() {
        assertEquals(listOf(4L, 2L, 3L, 1L), ids(LibraryFilter(sort = SortOrder.MODIFIED)))
    }

    @Test
    fun sortsByPreachDateUpcomingThenPastThenUndated() {
        assertEquals(listOf(3L, 1L, 2L, 4L), ids(LibraryFilter(sort = SortOrder.PREACH_DATE)))
    }

    @Test
    fun tagsAreUniqueIgnoringCase() {
        assertEquals(listOf("Gracia", "Oración"), allTags(all))
    }

    @Test
    fun shareTextIncludesSectionsInOrder() {
        val text = SermonTemplate.EXPOSITORY.build(today).copy(title = "Prueba", scripture = "Juan 1:1").toShareText()
        assertTrue(text.startsWith("Prueba\nTexto: Juan 1:1\nFecha: Domingo 25 de octubre de 2026"))
        val order = listOf("CONTEXTO DEL PASAJE", "INTRODUCCIÓN", "PUNTOS PRINCIPALES", "1. Lo que dice el texto",
            "APLICACIÓN PRÁCTICA", "CONCLUSIÓN", "NOTAS ADICIONALES")
        val positions = order.map { text.indexOf(it) }
        assertTrue("Faltan secciones: $positions", positions.all { it >= 0 })
        assertEquals(positions.sorted(), positions)
    }

    @Test
    fun emptySectionsAreOmittedFromShareText() {
        val text = Sermon(title = "Solo título").toShareText()
        assertFalse(text.contains("INTRODUCCIÓN"))
        assertFalse(text.contains("PUNTOS PRINCIPALES"))
    }
}
