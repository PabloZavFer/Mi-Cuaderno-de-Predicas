package com.pablozavfer.cuadernopredicas.ui.library

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class LibraryUiState(
    val loaded: Boolean = false,
    val totalCount: Int = 0,
    val sermons: List<Sermon> = emptyList(),
    val tags: List<String> = emptyList(),
    val filter: LibraryFilter = LibraryFilter(),
)

class LibraryViewModel(private val repository: SermonRepository) : ViewModel() {

    /** El texto de búsqueda es estado de Compose para que el campo responda al instante. */
    var query by mutableStateOf("")
        private set

    private val options = MutableStateFlow(LibraryFilter())

    private val messages = Channel<String>(Channel.BUFFERED)
    val messageFlow = messages.receiveAsFlow()

    val uiState: StateFlow<LibraryUiState> = combine(
        repository.observeAll(),
        options,
        snapshotFlow { query },
    ) { all, opts, q ->
        val filter = opts.copy(query = q)
        LibraryUiState(
            loaded = true,
            totalCount = all.size,
            sermons = applyFilter(all, filter, LocalDate.now()),
            tags = allTags(all),
            filter = filter,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryUiState())

    fun onQueryChange(value: String) {
        query = value
    }

    fun setStatus(status: SermonStatus?) = options.update { it.copy(status = status) }
    fun setTag(tag: String?) = options.update { it.copy(tag = tag) }
    fun setSort(sort: SortOrder) = options.update { it.copy(sort = sort) }

    fun clearFilters() {
        query = ""
        options.update { LibraryFilter(sort = it.sort) }
    }

    fun duplicate(id: Long) = viewModelScope.launch {
        val copy = repository.duplicate(id)
        messages.send(if (copy != null) "Se creó una copia como borrador." else "No se pudo duplicar la prédica.")
    }

    fun delete(sermon: Sermon) = viewModelScope.launch {
        repository.delete(sermon.id)
        messages.send("Se eliminó «${sermon.displayTitle}».")
    }
}
