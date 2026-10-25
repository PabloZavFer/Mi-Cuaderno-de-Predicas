@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pablozavfer.cuadernopredicas.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablozavfer.cuadernopredicas.data.SampleContent
import com.pablozavfer.cuadernopredicas.data.SermonTemplate
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import com.pablozavfer.cuadernopredicas.ui.common.ConfirmDialog
import com.pablozavfer.cuadernopredicas.ui.common.EmptyState
import com.pablozavfer.cuadernopredicas.ui.common.SampleBadge
import com.pablozavfer.cuadernopredicas.ui.common.StatusBadge
import com.pablozavfer.cuadernopredicas.ui.common.appViewModel
import com.pablozavfer.cuadernopredicas.util.Dates

@Composable
fun LibraryScreen(
    onOpenSermon: (Long) -> Unit,
    onNewSermon: (SermonTemplate) -> Unit,
    onPreach: (Long) -> Unit,
) {
    val vm = appViewModel { LibraryViewModel(it.sermons) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var choosingTemplate by rememberSaveable { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Sermon?>(null) }

    LaunchedEffect(vm) { vm.messageFlow.collect { snackbar.showSnackbar(it) } }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mi Cuaderno de Prédicas") }) },
        snackbarHost = { SnackbarHost(snackbar) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { choosingTemplate = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Nueva prédica") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 96.dp, // espacio para el botón flotante
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.loaded && state.totalCount == 0) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Create,
                        title = "Aún no tienes prédicas",
                        message = "Toca «Nueva prédica» para empezar. Puedes partir de una hoja en blanco " +
                            "o de una plantilla expositiva, temática o para grupo pequeño.",
                        actionLabel = "Crear mi primera prédica",
                        onAction = { choosingTemplate = true },
                    )
                }
                return@LazyColumn
            }

            item {
                SearchField(query = vm.query, onChange = vm::onQueryChange)
            }
            item {
                Filters(state, onStatus = vm::setStatus, onTag = vm::setTag, onSort = vm::setSort)
            }

            if (state.loaded && state.sermons.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Filled.Search,
                        title = "No se encontraron prédicas",
                        message = "Prueba con otras palabras o quita los filtros.",
                        actionLabel = "Quitar filtros",
                        onAction = vm::clearFilters,
                    )
                }
            }

            items(state.sermons, key = { it.id }) { sermon ->
                SermonCard(
                    sermon = sermon,
                    onClick = { onOpenSermon(sermon.id) },
                    onPreach = { onPreach(sermon.id) },
                    onDuplicate = { vm.duplicate(sermon.id) },
                    onDelete = { toDelete = sermon },
                )
            }
        }
    }

    if (choosingTemplate) {
        TemplateChooser(
            onChoose = { choosingTemplate = false; onNewSermon(it) },
            onDismiss = { choosingTemplate = false },
        )
    }

    toDelete?.let { sermon ->
        ConfirmDialog(
            title = "¿Eliminar esta prédica?",
            message = "Se eliminará «${sermon.displayTitle}». Esta acción no se puede deshacer. " +
                "Si quieres conservar una copia, crea un respaldo en Ajustes antes de eliminarla.",
            confirmLabel = "Eliminar",
            destructive = true,
            onConfirm = { vm.delete(sermon); toDelete = null },
            onDismiss = { toDelete = null },
        )
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Buscar por título, cita o contenido") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) { Icon(Icons.Filled.Close, contentDescription = "Borrar búsqueda") }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun Filters(
    state: LibraryUiState,
    onStatus: (SermonStatus?) -> Unit,
    onTag: (String?) -> Unit,
    onSort: (SortOrder) -> Unit,
) {
    val filter = state.filter
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = filter.status == null, onClick = { onStatus(null) }, label = { Text("Todas") })
            SermonStatus.entries.forEach { status ->
                FilterChip(
                    selected = filter.status == status,
                    onClick = { onStatus(if (filter.status == status) null else status) },
                    label = { Text(status.label) },
                )
            }
        }
        if (state.tags.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.tags) { tag ->
                    val selected = filter.tag.equals(tag, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = { onTag(if (selected) null else tag) },
                        label = { Text("#$tag") },
                    )
                }
            }
        }
        SortSelector(filter.sort, onSort)
    }
}

@Composable
private fun SortSelector(sort: SortOrder, onSort: (SortOrder) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }) {
            Text("Ordenar por: ${sort.label}")
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            SortOrder.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { open = false; onSort(option) },
                )
            }
        }
    }
}

@Composable
private fun SermonCard(
    sermon: Sermon,
    onClick: () -> Unit,
    onPreach: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    sermon.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (sermon.scripture.isNotBlank()) {
                    Text(
                        sermon.scripture,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(2.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    StatusBadge(sermon.status)
                    if (sermon.isSample) SampleBadge()
                    Text(
                        sermon.preachDate?.let { "Predicación: ${Dates.short(it)}" } ?: "Sin fecha",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterVertically),
                    )
                }
                val visibleTags = sermon.tags.filterNot { sermon.isSample && it == SampleContent.SAMPLE_TAG }
                if (visibleTags.isNotEmpty()) {
                    Text(
                        visibleTags.joinToString("  ") { "#$it" },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones de ${sermon.displayTitle}")
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text("Predicar") }, onClick = { menuOpen = false; onPreach() })
                    DropdownMenuItem(text = { Text("Duplicar") }, onClick = { menuOpen = false; onDuplicate() })
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        onClick = { menuOpen = false; onDelete() },
                    )
                }
            }
        }
    }
}

@Composable
private fun TemplateChooser(onChoose: (SermonTemplate) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva prédica") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    "Elige cómo empezar. Las plantillas traen indicaciones breves que puedes editar o borrar.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
                SermonTemplate.entries.forEach { template ->
                    ListItem(
                        headlineContent = { Text(template.label) },
                        supportingContent = { Text(template.description) },
                        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { onChoose(template) },
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
