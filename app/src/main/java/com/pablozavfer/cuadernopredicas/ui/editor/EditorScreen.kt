@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package com.pablozavfer.cuadernopredicas.ui.editor

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.pablozavfer.cuadernopredicas.data.SermonTemplate
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonPoint
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import com.pablozavfer.cuadernopredicas.export.ShareHelper
import com.pablozavfer.cuadernopredicas.ui.common.ConfirmDialog
import com.pablozavfer.cuadernopredicas.ui.common.EmptyState
import com.pablozavfer.cuadernopredicas.ui.common.SermonDatePickerDialog
import com.pablozavfer.cuadernopredicas.ui.common.appViewModel
import com.pablozavfer.cuadernopredicas.util.Dates
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun EditorScreen(
    sermonId: Long,
    template: SermonTemplate,
    initialDate: LocalDate?,
    onBack: () -> Unit,
    onPreach: (Long) -> Unit,
) {
    val vm = appViewModel { EditorViewModel(it.sermons, it.appScope, sermonId, template, initialDate) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }

    val goBack = {
        vm.flush()
        onBack()
    }
    BackHandler(onBack = goBack)
    // Guarda al pasar a segundo plano (por ejemplo, si llega una llamada).
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { vm.flush() }

    LaunchedEffect(vm) {
        vm.eventFlow.collect { event ->
            when (event) {
                is EditorEvent.OpenPreach -> onPreach(event.id)
                is EditorEvent.Message -> snackbar.showSnackbar(event.text)
                EditorEvent.Deleted -> onBack()
            }
        }
    }

    val savePdfLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        val current = vm.sermon
        if (uri != null && current != null) {
            scope.launch {
                exporting = true
                val result = runCatching { ShareHelper.savePdfTo(context, current, uri) }
                exporting = false
                snackbar.showSnackbar(if (result.isSuccess) "PDF guardado." else "No se pudo guardar el PDF.")
            }
        }
    }

    fun sharePdf(sermon: Sermon) = scope.launch {
        exporting = true
        val result = runCatching {
            val file = ShareHelper.createPdfInCache(context, sermon)
            ShareHelper.sharePdf(context, file, sermon.displayTitle)
        }
        exporting = false
        if (result.isFailure) snackbar.showSnackbar("No se pudo generar el PDF.")
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = goBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la biblioteca")
                    }
                },
                title = {
                    Column {
                        Text("Editar prédica", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        SaveIndicator(vm.saveState, onRetry = vm::retrySave)
                    }
                },
                actions = {
                    val sermon = vm.sermon
                    if (sermon != null) {
                        IconButton(onClick = { vm.preach() }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = "Abrir en modo de predicación")
                        }
                        Box {
                            IconButton(onClick = { menuOpen = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                            }
                            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                                DropdownMenuItem(
                                    text = { Text("Compartir como texto") },
                                    onClick = { menuOpen = false; ShareHelper.shareText(context, sermon) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Compartir PDF") },
                                    onClick = { menuOpen = false; sharePdf(sermon) },
                                )
                                DropdownMenuItem(
                                    text = { Text("Guardar PDF en el dispositivo") },
                                    onClick = { menuOpen = false; savePdfLauncher.launch(ShareHelper.pdfFileName(sermon)) },
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    text = { Text("Duplicar") },
                                    onClick = { menuOpen = false; vm.duplicate() },
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                                    onClick = { menuOpen = false; confirmDelete = true },
                                )
                            }
                        }
                    }
                },
            )
        },
    ) { padding ->
        val sermon = vm.sermon
        when {
            vm.notFound -> EmptyState(
                icon = Icons.Filled.Warning,
                title = "No se encontró la prédica",
                message = "Es posible que se haya eliminado.",
                actionLabel = "Volver",
                onAction = onBack,
                modifier = Modifier.padding(padding),
            )

            sermon == null -> Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            else -> Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .consumeWindowInsets(padding)
                    .imePadding(),
            ) {
                if (exporting) LinearProgressIndicator(Modifier.fillMaxWidth())
                EditorForm(sermon, vm)
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = "¿Eliminar esta prédica?",
            message = "Se eliminará «${vm.sermon?.displayTitle.orEmpty()}». Esta acción no se puede deshacer.",
            confirmLabel = "Eliminar",
            destructive = true,
            onConfirm = { confirmDelete = false; vm.delete() },
            onDismiss = { confirmDelete = false },
        )
    }
}

@Composable
private fun SaveIndicator(state: SaveState, onRetry: () -> Unit) {
    val style = MaterialTheme.typography.labelMedium
    val muted = MaterialTheme.colorScheme.onSurfaceVariant
    when (state) {
        SaveState.IDLE -> Unit
        SaveState.PENDING, SaveState.SAVING -> Text("Guardando…", style = style, color = muted)
        SaveState.SAVED -> Text("✓ Guardado en el dispositivo", style = style, color = MaterialTheme.colorScheme.primary)
        SaveState.ERROR -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("No se pudo guardar", style = style, color = MaterialTheme.colorScheme.error)
            TextButton(onClick = onRetry) { Text("Reintentar", style = style) }
        }
    }
}

@Composable
private fun EditorForm(sermon: Sermon, vm: EditorViewModel) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var pointToDelete by rememberSaveable { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        if (sermon.isSample) SampleNotice()

        OutlinedTextField(
            value = sermon.title,
            onValueChange = vm::setTitle,
            label = { Text("Título") },
            textStyle = MaterialTheme.typography.titleMedium,
            singleLine = true,
            keyboardOptions = sentenceKeyboard(ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = sermon.scripture,
            onValueChange = vm::setScripture,
            label = { Text("Referencia bíblica principal") },
            placeholder = { Text("Ej.: Juan 3:16-21") },
            singleLine = true,
            keyboardOptions = sentenceKeyboard(ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )

        DateRow(
            date = sermon.preachDate,
            onPick = { showDatePicker = true },
            onClear = { vm.setPreachDate(null) },
        )

        Text("Estado", style = MaterialTheme.typography.labelLarge)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SermonStatus.entries.forEach { status ->
                FilterChip(
                    selected = sermon.status == status,
                    onClick = { vm.setStatus(status) },
                    label = { Text(status.label) },
                )
            }
        }

        TagsEditor(tags = sermon.tags, onAdd = vm::addTags, onRemove = vm::removeTag)

        SectionField("Contexto del pasaje", sermon.context, vm::setContext)
        SectionField("Introducción", sermon.introduction, vm::setIntroduction)

        Text("Puntos principales", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
        if (sermon.points.isEmpty()) {
            Text(
                "Aún no hay puntos. Agrega el primero con el botón de abajo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        sermon.points.forEachIndexed { index, point ->
            key(point.id) {
                PointCard(
                    index = index,
                    total = sermon.points.size,
                    point = point,
                    onTitle = { vm.updatePoint(point.id, title = it) },
                    onBody = { vm.updatePoint(point.id, body = it) },
                    onMove = { delta -> vm.movePoint(point.id, delta) },
                    onDelete = {
                        if (point.title.isBlank() && point.body.isBlank()) vm.removePoint(point.id) else pointToDelete = point.id
                    },
                )
            }
        }
        OutlinedButton(onClick = vm::addPoint, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.padding(4.dp))
            Text("Agregar punto")
        }

        SectionField("Aplicación práctica", sermon.application, vm::setApplication)
        SectionField("Conclusión", sermon.conclusion, vm::setConclusion)
        SectionField("Notas adicionales", sermon.notes, vm::setNotes)
        Spacer(Modifier.height(32.dp))
    }

    if (showDatePicker) {
        SermonDatePickerDialog(
            initial = sermon.preachDate,
            onPick = vm::setPreachDate,
            onDismiss = { showDatePicker = false },
        )
    }

    pointToDelete?.let { id ->
        ConfirmDialog(
            title = "¿Eliminar este punto?",
            message = "Se borrará el título y el desarrollo del punto.",
            confirmLabel = "Eliminar",
            destructive = true,
            onConfirm = { vm.removePoint(id); pointToDelete = null },
            onDismiss = { pointToDelete = null },
        )
    }
}

private fun sentenceKeyboard(imeAction: ImeAction = ImeAction.Default) =
    KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = imeAction)

@Composable
private fun SampleNotice() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Filled.Info, contentDescription = null)
            Text(
                "Este es un ejemplo editable. Úsalo para conocer el editor: puedes cambiarlo, duplicarlo o eliminarlo.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun SectionField(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        minLines = 3,
        keyboardOptions = sentenceKeyboard(),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun DateRow(date: LocalDate?, onPick: () -> Unit, onClear: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Fecha prevista de predicación", style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = onPick, modifier = Modifier.weight(1f)) {
                Icon(Icons.Filled.DateRange, contentDescription = null)
                Spacer(Modifier.padding(4.dp))
                Text(date?.let(Dates::long) ?: "Elegir fecha")
            }
            if (date != null) {
                IconButton(onClick = onClear) { Icon(Icons.Filled.Close, contentDescription = "Quitar fecha") }
            }
        }
    }
}

@Composable
private fun TagsEditor(tags: List<String>, onAdd: (String) -> Unit, onRemove: (String) -> Unit) {
    var input by rememberSaveable { mutableStateOf("") }
    val submit = {
        if (input.isNotBlank()) onAdd(input)
        input = ""
    }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Etiquetas", style = MaterialTheme.typography.labelLarge)
        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = { onRemove(tag) },
                        label = { Text(tag) },
                        trailingIcon = { Icon(Icons.Filled.Close, contentDescription = "Quitar etiqueta $tag") },
                    )
                }
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Agregar etiqueta") },
            placeholder = { Text("Ej.: Fe, Familia, Navidad") },
            singleLine = true,
            keyboardOptions = sentenceKeyboard(ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            trailingIcon = {
                IconButton(onClick = submit, enabled = input.isNotBlank()) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar etiqueta")
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PointCard(
    index: Int,
    total: Int,
    point: SermonPoint,
    onTitle: (String) -> Unit,
    onBody: (String) -> Unit,
    onMove: (Int) -> Unit,
    onDelete: () -> Unit,
) {
    val number = index + 1
    OutlinedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Punto $number",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onMove(-1) }, enabled = index > 0) {
                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Subir punto $number")
                }
                IconButton(onClick = { onMove(1) }, enabled = index < total - 1) {
                    Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Bajar punto $number")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar punto $number")
                }
            }
            OutlinedTextField(
                value = point.title,
                onValueChange = onTitle,
                label = { Text("Título del punto") },
                singleLine = true,
                keyboardOptions = sentenceKeyboard(ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = point.body,
                onValueChange = onBody,
                label = { Text("Desarrollo") },
                minLines = 3,
                keyboardOptions = sentenceKeyboard(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
