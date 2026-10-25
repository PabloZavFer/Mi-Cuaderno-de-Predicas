package com.pablozavfer.cuadernopredicas

import com.pablozavfer.cuadernopredicas.data.SampleContent
import com.pablozavfer.cuadernopredicas.data.backup.BackupCodec
import com.pablozavfer.cuadernopredicas.data.backup.BackupParseResult
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate

class BackupCodecTest {

    private val sermon = Sermon(
        uid = "abc-123",
        title = "La fe que persevera",
        scripture = "Hebreos 11:1-6",
        preachDate = LocalDate.of(2026, 11, 1),
        context = "Contexto con acentos: ñandú, corazón \"comillas\" y\nsaltos de línea.",
        introduction = "Intro",
        points = listOf(SermonPoint(title = "Uno", body = "Cuerpo 1"), SermonPoint(title = "Dos", body = "")),
        application = "Aplicar",
        conclusion = "Concluir",
        notes = "Notas",
        status = SermonStatus.READY,
        tags = listOf("Fe", "Hebreos"),
        createdAt = 1_000,
        updatedAt = 2_000,
    )

    private fun roundTrip(list: List<Sermon>): List<Sermon> {
        val json = BackupCodec.encode(list, Instant.parse("2026-10-25T10:00:00Z"), "1.0.0")
        val result = BackupCodec.decode(json)
        assertTrue("Se esperaba un respaldo válido: $result", result is BackupParseResult.Success)
        return (result as BackupParseResult.Success).sermons
    }

    @Test
    fun exportAndRestoreKeepsAllContent() {
        val restored = roundTrip(listOf(sermon)).single()
        assertTrue(restored.sameContentAs(sermon))
        assertEquals(sermon.uid, restored.uid)
        assertEquals(sermon.createdAt, restored.createdAt)
        assertEquals(sermon.updatedAt, restored.updatedAt)
        assertEquals(0L, restored.id)
    }

    @Test
    fun samplesSurviveRoundTrip() {
        val samples = SampleContent.build(LocalDate.of(2026, 10, 25))
        val restored = roundTrip(samples)
        assertEquals(3, restored.size)
        samples.forEach { original -> assertTrue(restored.any { it.sameContentAs(original) }) }
    }

    private fun failureOf(text: String): String {
        val result = BackupCodec.decode(text)
        assertTrue("Se esperaba un error para: $text", result is BackupParseResult.Failure)
        return (result as BackupParseResult.Failure).message
    }

    @Test
    fun rejectsEmptyAndGarbage() {
        assertEquals("El archivo está vacío.", failureOf("   "))
        assertEquals(BackupCodec.INVALID_FILE, failureOf("esto no es json"))
        assertEquals(BackupCodec.INVALID_FILE, failureOf("[1,2,3]"))
        assertEquals(BackupCodec.INVALID_FILE, failureOf("""{"format":"otra-app","version":1,"sermons":[]}"""))
        assertEquals(BackupCodec.INVALID_FILE, failureOf("""{"version":1,"sermons":[]}"""))
    }

    @Test
    fun rejectsNewerVersion() {
        val message = failureOf("""{"format":"mi-cuaderno-de-predicas","version":99,"sermons":[{"uid":"a"}]}""")
        assertTrue(message.contains("versión más reciente"))
    }

    @Test
    fun rejectsEmptyBackup() {
        assertEquals(
            "El respaldo no contiene prédicas.",
            failureOf("""{"format":"mi-cuaderno-de-predicas","version":1,"sermons":[]}"""),
        )
    }

    @Test
    fun rejectsInvalidItemsWithPosition() {
        fun file(items: String) = """{"format":"mi-cuaderno-de-predicas","version":1,"sermons":[$items]}"""
        assertTrue(failureOf(file("""{"uid":"a"},{"uid":""}""")).startsWith("La prédica nº 2"))
        assertTrue(failureOf(file("""{"uid":"a","status":"INVENTADO"}""")).contains("estado desconocido"))
        assertTrue(failureOf(file("""{"uid":"a","preachDate":"31/12/2026"}""")).contains("fecha"))
        assertTrue(failureOf(file("""{"uid":"a"},{"uid":"a"}""")).contains("repetida"))
        assertEquals(BackupCodec.INVALID_FILE, failureOf(file("""{"title":"sin uid"}""")))
    }

    @Test
    fun ignoresUnknownFieldsAndFillsDefaults() {
        val result = BackupCodec.decode(
            """{"format":"mi-cuaderno-de-predicas","version":1,"futuro":true,
               "sermons":[{"uid":"x","title":"Solo título","campoNuevo":"ok","tags":[" Fe ","","Fe"]}]}""",
        )
        val sermon = (result as BackupParseResult.Success).sermons.single()
        assertEquals("Solo título", sermon.title)
        assertEquals(SermonStatus.DRAFT, sermon.status)
        assertEquals(listOf("Fe"), sermon.tags)
        assertTrue(sermon.createdAt > 0)
    }
}
