package com.pablozavfer.cuadernopredicas.ui.editor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.SermonTemplate
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

enum class SaveState { IDLE, PENDING, SAVING, SAVED, ERROR }

sealed interface EditorEvent {
    data class OpenPreach(val id: Long) : EditorEvent
    data class Message(val text: String) : EditorEvent
    data object Deleted : EditorEvent
}

/**
 * Guarda automáticamente poco después de cada cambio. Una prédica nueva no se crea en la base
 * de datos hasta que el usuario escribe algo, para no llenar la biblioteca de borradores vacíos.
 */
@OptIn(FlowPreview::class)
class EditorViewModel(
    private val repository: SermonRepository,
    private val appScope: CoroutineScope,
    sermonId: Long,
    template: SermonTemplate,
    initialDate: LocalDate?,
) : ViewModel() {

    /** Estado de Compose (no StateFlow) para que los campos de texto respondan sin retrasos. */
    var sermon by mutableStateOf<Sermon?>(null)
        private set
    var notFound by mutableStateOf(false)
        private set
    var saveState by mutableStateOf(SaveState.IDLE)
        private set

    private val events = Channel<EditorEvent>(Channel.BUFFERED)
    val eventFlow = events.receiveAsFlow()

    private val edits = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    private val saveMutex = Mutex()
    private var dirty = false
    private var deleted = false

    init {
        viewModelScope.launch {
            if (sermonId > 0) {
                val loaded = repository.get(sermonId)
                if (loaded == null) notFound = true else sermon = loaded
            } else {
                sermon = template.build(initialDate)
            }
        }
        viewModelScope.launch {
            edits.debounce(AUTOSAVE_DELAY_MS).collect { persist() }
        }
    }

    // ----- Edición -----

    private fun edit(transform: (Sermon) -> Sermon) {
        val current = sermon ?: return
        val updated = transform(current)
        if (updated == current) return
        sermon = updated
        dirty = true
        saveState = SaveState.PENDING
        edits.tryEmit(Unit)
    }

    fun setTitle(value: String) = edit { it.copy(title = value) }
    fun setScripture(value: String) = edit { it.copy(scripture = value) }
    fun setPreachDate(value: LocalDate?) = edit { it.copy(preachDate = value) }
    fun setStatus(value: SermonStatus) = edit { it.copy(status = value) }
    fun setContext(value: String) = edit { it.copy(context = value) }
    fun setIntroduction(value: String) = edit { it.copy(introduction = value) }
    fun setApplication(value: String) = edit { it.copy(application = value) }
    fun setConclusion(value: String) = edit { it.copy(conclusion = value) }
    fun setNotes(value: String) = edit { it.copy(notes = value) }

    fun addPoint() = edit { it.copy(points = it.points + SermonPoint()) }

    fun removePoint(pointId: String) = edit { s -> s.copy(points = s.points.filterNot { it.id == pointId }) }

    fun updatePoint(pointId: String, title: String? = null, body: String? = null) = edit { s ->
        s.copy(
            points = s.points.map {
                if (it.id == pointId) it.copy(title = title ?: it.title, body = body ?: it.body) else it
            },
        )
    }

    /** Mueve un punto una posición hacia arriba (-1) o hacia abajo (+1). */
    fun movePoint(pointId: String, delta: Int) = edit { s ->
        val from = s.points.indexOfFirst { it.id == pointId }
        val to = from + delta
        if (from < 0 || to !in s.points.indices) return@edit s
        val list = s.points.toMutableList()
        list.add(to, list.removeAt(from))
        s.copy(points = list)
    }

    /** Agrega una o varias etiquetas separadas por comas. */
    fun addTags(raw: String) = edit { s ->
        val newTags = raw.split(',').map { it.trim() }.filter { it.isNotEmpty() }.map { it.take(MAX_TAG_LENGTH) }
        val merged = (s.tags + newTags).distinctBy { it.lowercase() }
        s.copy(tags = merged)
    }

    fun removeTag(tag: String) = edit { s -> s.copy(tags = s.tags - tag) }

    // ----- Guardado -----

    private suspend fun persist() {
        saveMutex.withLock {
            if (!dirty || deleted) return
            val draft = sermon ?: return
            dirty = false
            saveState = SaveState.SAVING
            try {
                val now = System.currentTimeMillis()
                val id = repository.save(draft.copy(updatedAt = now))
                // Conserva cambios hechos mientras se guardaba; solo se actualizan id y fecha.
                sermon = sermon?.copy(id = id, updatedAt = now)
                if (dirty) {
                    saveState = SaveState.PENDING
                    edits.tryEmit(Unit)
                } else {
                    saveState = SaveState.SAVED
                }
            } catch (e: CancellationException) {
                dirty = true
                throw e
            } catch (e: Exception) {
                dirty = true
                saveState = SaveState.ERROR
            }
        }
    }

    /** Guarda ya, aunque la prédica nueva no tenga cambios. Devuelve su id (0 si falló). */
    private suspend fun saveNow(): Long {
        if (sermon?.id == 0L) dirty = true
        persist()
        return sermon?.id ?: 0L
    }

    /** Guarda los cambios pendientes en un ámbito que sobrevive a la pantalla (al salir o pasar a segundo plano). */
    fun flush() {
        if (dirty && !deleted) appScope.launch { persist() }
    }

    fun retrySave() {
        dirty = true
        viewModelScope.launch { persist() }
    }

    // ----- Acciones -----

    fun preach() = viewModelScope.launch {
        val id = saveNow()
        if (id > 0) events.send(EditorEvent.OpenPreach(id)) else events.send(EditorEvent.Message(SAVE_ERROR))
    }

    fun duplicate() = viewModelScope.launch {
        val id = saveNow()
        val copyId = if (id > 0) repository.duplicate(id) else null
        events.send(EditorEvent.Message(if (copyId != null) "Se creó una copia en la biblioteca." else SAVE_ERROR))
    }

    fun delete() = viewModelScope.launch {
        saveMutex.withLock {
            deleted = true
            dirty = false
            sermon?.id?.takeIf { it > 0 }?.let { repository.delete(it) }
        }
        events.send(EditorEvent.Deleted)
    }

    override fun onCleared() {
        flush()
        super.onCleared()
    }

    companion object {
        const val AUTOSAVE_DELAY_MS = 700L
        private const val MAX_TAG_LENGTH = 60
        private const val SAVE_ERROR = "No se pudo guardar la prédica. Inténtalo de nuevo."
    }
}
