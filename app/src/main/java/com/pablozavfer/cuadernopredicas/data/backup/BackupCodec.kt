package com.pablozavfer.cuadernopredicas.data.backup

import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID

// ----- Formato del archivo de respaldo (versión 1) -----

@Serializable
data class BackupFile(
    val format: String,
    val version: Int,
    val exportedAt: String = "",
    val appVersion: String = "",
    val sermons: List<BackupSermon> = emptyList(),
)

@Serializable
data class BackupSermon(
    val uid: String,
    val title: String = "",
    val scripture: String = "",
    val preachDate: String? = null,
    val context: String = "",
    val introduction: String = "",
    val points: List<BackupPoint> = emptyList(),
    val application: String = "",
    val conclusion: String = "",
    val notes: String = "",
    val status: String = SermonStatus.DRAFT.name,
    val tags: List<String> = emptyList(),
    val isSample: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

@Serializable
data class BackupPoint(val title: String = "", val body: String = "")

sealed interface BackupParseResult {
    data class Success(val sermons: List<Sermon>, val exportedAt: String) : BackupParseResult
    data class Failure(val message: String) : BackupParseResult
}

/** Convierte las prédicas a JSON y valida respaldos antes de restaurarlos. No depende de Android. */
object BackupCodec {
    const val FORMAT_ID = "mi-cuaderno-de-predicas"
    const val CURRENT_VERSION = 1

    const val MAX_FILE_BYTES = 20 * 1024 * 1024
    private const val MAX_TEXT_LENGTH = 200_000
    private const val MAX_POINTS = 300
    private const val MAX_TAGS = 50
    private const val MAX_TAG_LENGTH = 60
    private const val MAX_UID_LENGTH = 100

    const val INVALID_FILE = "El archivo no es un respaldo válido de Mi Cuaderno de Prédicas."

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(sermons: List<Sermon>, exportedAt: Instant, appVersion: String): String {
        val file = BackupFile(
            format = FORMAT_ID,
            version = CURRENT_VERSION,
            exportedAt = exportedAt.toString(),
            appVersion = appVersion,
            sermons = sermons.sortedBy { it.createdAt }.map { it.toBackup() },
        )
        return json.encodeToString(BackupFile.serializer(), file)
    }

    fun decode(text: String): BackupParseResult {
        if (text.isBlank()) return BackupParseResult.Failure("El archivo está vacío.")

        val file = try {
            json.decodeFromString(BackupFile.serializer(), text)
        } catch (e: SerializationException) {
            return BackupParseResult.Failure(INVALID_FILE)
        } catch (e: IllegalArgumentException) {
            return BackupParseResult.Failure(INVALID_FILE)
        }

        if (file.format != FORMAT_ID || file.version < 1) return BackupParseResult.Failure(INVALID_FILE)
        if (file.version > CURRENT_VERSION) {
            return BackupParseResult.Failure(
                "Este respaldo se creó con una versión más reciente de la aplicación. " +
                    "Actualiza la app para poder restaurarlo.",
            )
        }
        if (file.sermons.isEmpty()) return BackupParseResult.Failure("El respaldo no contiene prédicas.")

        val seenUids = HashSet<String>()
        val sermons = ArrayList<Sermon>(file.sermons.size)
        file.sermons.forEachIndexed { index, item ->
            val where = "La prédica nº ${index + 1} del respaldo"
            val error = validate(item)
            if (error != null) return BackupParseResult.Failure("$where $error")
            if (!seenUids.add(item.uid)) return BackupParseResult.Failure("$where está repetida dentro del archivo.")
            sermons += item.toSermon()
        }
        return BackupParseResult.Success(sermons, file.exportedAt)
    }

    /** Devuelve la descripción del problema, o null si el elemento es válido. */
    private fun validate(item: BackupSermon): String? {
        if (item.uid.isBlank() || item.uid.length > MAX_UID_LENGTH) return "no tiene un identificador válido."
        if (SermonStatus.fromKey(item.status) == null) return "tiene un estado desconocido."
        if (item.preachDate != null) {
            try {
                LocalDate.parse(item.preachDate)
            } catch (e: DateTimeParseException) {
                return "tiene una fecha de predicación no válida."
            }
        }
        val texts = listOf(
            item.title, item.scripture, item.context, item.introduction,
            item.application, item.conclusion, item.notes,
        ) + item.points.flatMap { listOf(it.title, it.body) }
        if (texts.any { it.length > MAX_TEXT_LENGTH }) return "tiene un texto demasiado largo."
        if (item.points.size > MAX_POINTS) return "tiene demasiados puntos."
        if (item.tags.size > MAX_TAGS || item.tags.any { it.length > MAX_TAG_LENGTH }) {
            return "tiene etiquetas no válidas."
        }
        return null
    }

    private fun Sermon.toBackup() = BackupSermon(
        uid = uid,
        title = title,
        scripture = scripture,
        preachDate = preachDate?.toString(),
        context = context,
        introduction = introduction,
        points = points.map { BackupPoint(it.title, it.body) },
        application = application,
        conclusion = conclusion,
        notes = notes,
        status = status.name,
        tags = tags,
        isSample = isSample,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private fun BackupSermon.toSermon(): Sermon {
        val now = System.currentTimeMillis()
        val created = if (createdAt > 0) createdAt else now
        return Sermon(
            id = 0,
            uid = uid,
            title = title,
            scripture = scripture,
            preachDate = preachDate?.let(LocalDate::parse),
            context = context,
            introduction = introduction,
            points = points.map { SermonPoint(id = UUID.randomUUID().toString(), title = it.title, body = it.body) },
            application = application,
            conclusion = conclusion,
            notes = notes,
            status = SermonStatus.fromKey(status) ?: SermonStatus.DRAFT,
            tags = tags.map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
            isSample = isSample,
            createdAt = created,
            updatedAt = if (updatedAt > 0) updatedAt else created,
        )
    }
}
