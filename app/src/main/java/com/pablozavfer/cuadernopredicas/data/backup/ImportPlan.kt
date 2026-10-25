package com.pablozavfer.cuadernopredicas.data.backup

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import java.util.UUID

/**
 * Resultado de comparar un respaldo con la biblioteca actual.
 * Restaurar nunca modifica ni borra prédicas existentes:
 * - nuevas: se agregan tal cual;
 * - idénticas a una existente: se omiten;
 * - con el mismo identificador pero contenido distinto: se agregan como copia marcada "(restaurada)".
 */
data class ImportPlan(
    val toInsert: List<Sermon>,
    val newCount: Int,
    val identicalCount: Int,
    val conflictCount: Int,
) {
    val totalInBackup: Int get() = newCount + identicalCount + conflictCount
}

const val RESTORED_SUFFIX = " (restaurada)"

fun planImport(
    incoming: List<Sermon>,
    existing: List<Sermon>,
    newUid: () -> String = { UUID.randomUUID().toString() },
): ImportPlan {
    val byUid = existing.associateBy { it.uid }
    // Todo lo que habrá en la biblioteca, para no agregar dos veces el mismo contenido.
    val known = existing.toMutableList()
    val toInsert = mutableListOf<Sermon>()
    var newCount = 0
    var identical = 0
    var conflicts = 0

    for (sermon in incoming) {
        val sameUid = byUid[sermon.uid]
        val candidate = when {
            sameUid == null -> sermon
            sameUid.sameContentAs(sermon) -> null
            else -> sermon.copy(uid = newUid(), title = sermon.displayTitle + RESTORED_SUFFIX)
        }
        // También se omite si ya existe exactamente ese contenido con otro identificador
        // (por ejemplo, al restaurar dos veces el mismo respaldo).
        if (candidate == null || known.any { it.sameContentIgnoringUid(candidate) }) {
            identical++
            continue
        }
        if (sameUid == null) newCount++ else conflicts++
        toInsert += candidate
        known += candidate
    }
    return ImportPlan(toInsert, newCount, identical, conflicts)
}

private fun Sermon.sameContentIgnoringUid(other: Sermon) = copy(uid = other.uid).sameContentAs(other)
