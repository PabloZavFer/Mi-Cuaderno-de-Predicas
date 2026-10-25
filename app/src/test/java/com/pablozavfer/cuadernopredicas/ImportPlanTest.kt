package com.pablozavfer.cuadernopredicas

import com.pablozavfer.cuadernopredicas.data.backup.RESTORED_SUFFIX
import com.pablozavfer.cuadernopredicas.data.backup.planImport
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportPlanTest {
    private var counter = 0
    private val uids = { "nuevo-${++counter}" }

    private val existing = Sermon(
        id = 7,
        uid = "u1",
        title = "Original",
        introduction = "Texto",
        points = listOf(SermonPoint(id = "p-local", title = "A")),
        updatedAt = 50,
    )

    @Test
    fun newSermonsAreAdded() {
        val incoming = Sermon(uid = "u2", title = "Otra")
        val plan = planImport(listOf(incoming), listOf(existing), uids)
        assertEquals(1, plan.newCount)
        assertEquals(listOf("u2"), plan.toInsert.map { it.uid })
    }

    @Test
    fun identicalSermonsAreSkippedEvenWithDifferentInternalIds() {
        val sameContent = existing.copy(id = 0, points = listOf(SermonPoint(id = "p-otro", title = "A")), updatedAt = 999)
        val plan = planImport(listOf(sameContent), listOf(existing), uids)
        assertEquals(1, plan.identicalCount)
        assertTrue(plan.toInsert.isEmpty())
    }

    @Test
    fun conflictsBecomeCopiesAndNeverOverwrite() {
        val changed = existing.copy(id = 0, introduction = "Texto editado en otro teléfono")
        val plan = planImport(listOf(changed), listOf(existing), uids)
        assertEquals(1, plan.conflictCount)
        val copy = plan.toInsert.single()
        assertEquals("nuevo-1", copy.uid)
        assertEquals("Original$RESTORED_SUFFIX", copy.title)
        assertEquals("Texto editado en otro teléfono", copy.introduction)
    }

    @Test
    fun restoringTheSameBackupTwiceDoesNotDuplicate() {
        val changed = existing.copy(id = 0, introduction = "Versión del respaldo")
        val first = planImport(listOf(changed), listOf(existing), uids)
        val libraryAfterFirst = listOf(existing) + first.toInsert
        val second = planImport(listOf(changed), libraryAfterFirst, uids)
        assertTrue(second.toInsert.isEmpty())
        assertEquals(1, second.identicalCount)
    }

    @Test
    fun countsAddUpToBackupSize() {
        val incoming = listOf(
            Sermon(uid = "n1", title = "Nueva"),
            existing.copy(id = 0),
            existing.copy(id = 0, uid = "u1", title = "Cambiada"),
        )
        val plan = planImport(incoming, listOf(existing), uids)
        assertEquals(3, plan.totalInBackup)
        assertEquals(1, plan.newCount)
        assertEquals(1, plan.identicalCount)
        assertEquals(1, plan.conflictCount)
        assertEquals(plan.toInsert.size, plan.toInsert.map { it.uid }.toSet().size)
    }
}
