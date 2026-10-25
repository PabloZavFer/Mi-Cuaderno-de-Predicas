package com.pablozavfer.cuadernopredicas.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.time.LocalDate

/**
 * Una fila por prédica. Los puntos y las etiquetas se guardan como JSON dentro de la fila:
 * se leen y escriben siempre junto con la prédica y así el esquema es más simple.
 */
@Entity(tableName = "sermons", indices = [Index(value = ["uid"], unique = true)])
data class SermonEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uid: String,
    val title: String,
    val scripture: String,
    @ColumnInfo(name = "preach_epoch_day") val preachEpochDay: Long?,
    val context: String,
    val introduction: String,
    @ColumnInfo(name = "points_json") val pointsJson: String,
    val application: String,
    val conclusion: String,
    val notes: String,
    val status: String,
    @ColumnInfo(name = "tags_json") val tagsJson: String,
    @ColumnInfo(name = "is_sample") val isSample: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

@Serializable
private data class StoredPoint(val id: String, val title: String, val body: String)

private val storageJson = Json { ignoreUnknownKeys = true }
private val pointsSerializer = ListSerializer(StoredPoint.serializer())
private val tagsSerializer = ListSerializer(String.serializer())

fun SermonEntity.toDomain(): Sermon = Sermon(
    id = id,
    uid = uid,
    title = title,
    scripture = scripture,
    preachDate = preachEpochDay?.let(LocalDate::ofEpochDay),
    context = context,
    introduction = introduction,
    points = runCatching { storageJson.decodeFromString(pointsSerializer, pointsJson) }
        .getOrDefault(emptyList())
        .map { SermonPoint(id = it.id, title = it.title, body = it.body) },
    application = application,
    conclusion = conclusion,
    notes = notes,
    status = SermonStatus.fromKey(status) ?: SermonStatus.DRAFT,
    tags = runCatching { storageJson.decodeFromString(tagsSerializer, tagsJson) }.getOrDefault(emptyList()),
    isSample = isSample,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun Sermon.toEntity(): SermonEntity = SermonEntity(
    id = id,
    uid = uid,
    title = title,
    scripture = scripture,
    preachEpochDay = preachDate?.toEpochDay(),
    context = context,
    introduction = introduction,
    pointsJson = storageJson.encodeToString(pointsSerializer, points.map { StoredPoint(it.id, it.title, it.body) }),
    application = application,
    conclusion = conclusion,
    notes = notes,
    status = status.name,
    tagsJson = storageJson.encodeToString(tagsSerializer, tags),
    isSample = isSample,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
