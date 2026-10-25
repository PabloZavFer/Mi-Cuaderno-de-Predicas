@file:OptIn(ExperimentalMaterial3Api::class)

package com.pablozavfer.cuadernopredicas.ui.preach

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablozavfer.cuadernopredicas.data.AppSettings
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import com.pablozavfer.cuadernopredicas.export.pointHeading
import com.pablozavfer.cuadernopredicas.export.sections
import com.pablozavfer.cuadernopredicas.ui.common.EmptyState
import com.pablozavfer.cuadernopredicas.ui.common.appViewModel
import com.pablozavfer.cuadernopredicas.ui.theme.CuadernoTheme
import com.pablozavfer.cuadernopredicas.util.Dates

@Composable
fun PreachScreen(sermonId: Long, onBack: () -> Unit) {
    val vm = appViewModel { PreachViewModel(it.sermons, it.settings, sermonId) }
    val settings by vm.settings.collectAsStateWithLifecycle()

    KeepScreenOnAndHideBars()

    // Tema propio del modo de predicación, independiente del resto de la app.
    CuadernoTheme(darkTheme = settings.preachDarkTheme) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal),
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Salir del modo de predicación")
                        }
                    },
                    title = {
                        Text(vm.sermon?.displayTitle.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    actions = {
                        ReadingOptions(
                            settings = settings,
                            preached = vm.sermon?.status == SermonStatus.PREACHED,
                            onFontChange = vm::changeFontSize,
                            onDarkChange = vm::setDarkTheme,
                            onMarkPreached = vm::markPreached,
                        )
                    },
                )
            },
            bottomBar = {
                TimerBar(
                    elapsedMs = vm.elapsedMs,
                    running = vm.running,
                    onStart = vm::start,
                    onPause = vm::pause,
                    onReset = vm::reset,
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

                else -> ReadingContent(sermon, settings.preachFontSize, padding)
            }
        }
    }
}

/**
 * La lista se crea solo cuando la prédica ya está cargada: así, al girar la pantalla,
 * la posición guardada se restaura sobre el contenido completo y no se pierde.
 */
@Composable
private fun ReadingContent(sermon: Sermon, fontSize: Int, padding: PaddingValues) {
    val listState = rememberLazyListState()
    val body = TextStyle(fontFamily = FontFamily.Serif, fontSize = fontSize.sp, lineHeight = (fontSize * 1.5f).sp)
    val heading = body.copy(
        fontSize = (fontSize * 1.15f).sp,
        lineHeight = (fontSize * 1.5f).sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
    )
    val pointStyle = body.copy(fontWeight = FontWeight.SemiBold)
    val sections = remember(sermon) { sermon.sections() }
    val direction = LocalLayoutDirection.current

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 24.dp + padding.calculateStartPadding(direction),
            end = 24.dp + padding.calculateEndPadding(direction),
            top = padding.calculateTopPadding() + 8.dp,
            bottom = padding.calculateBottomPadding() + 48.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "encabezado") {
            Column(Modifier.readingWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    sermon.displayTitle,
                    style = body.copy(fontSize = (fontSize * 1.45f).sp, lineHeight = (fontSize * 1.8f).sp, fontWeight = FontWeight.Bold),
                )
                if (sermon.scripture.isNotBlank()) {
                    Text(sermon.scripture, style = body.copy(color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.SemiBold))
                }
                sermon.preachDate?.let {
                    Text(Dates.long(it), style = body.copy(fontSize = (fontSize * 0.8f).sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                }
                HorizontalDivider(Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f))
            }
        }
        if (sections.isEmpty()) {
            item(key = "vacio") {
                Text(
                    "Esta prédica todavía no tiene contenido. Vuelve al editor para escribirla.",
                    style = body.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.readingWidth(),
                )
            }
        }
        sections.forEachIndexed { index, section ->
            item(key = "seccion-$index") {
                Text(section.heading, style = heading, modifier = Modifier.readingWidth().padding(top = 8.dp))
            }
            if (section.body.isNotEmpty()) {
                item(key = "texto-$index") { Text(section.body, style = body, modifier = Modifier.readingWidth()) }
            }
            section.points.forEachIndexed { i, point ->
                item(key = "punto-${point.id}") {
                    Column(Modifier.readingWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(pointHeading(i, point), style = pointStyle)
                        if (point.body.isNotBlank()) Text(point.body.trim(), style = body)
                    }
                }
            }
        }
    }
}

/** En tablets o en horizontal, limita el ancho de línea para que la lectura sea cómoda. */
private fun Modifier.readingWidth() = this.widthIn(max = 720.dp).fillMaxWidth()

@Composable
private fun ReadingOptions(
    settings: AppSettings,
    preached: Boolean,
    onFontChange: (Int) -> Unit,
    onDarkChange: (Boolean) -> Unit,
    onMarkPreached: () -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    Box {
        TextButton(onClick = { open = true }, modifier = Modifier.semantics { contentDescription = "Opciones de lectura" }) {
            Text("Aa", style = MaterialTheme.typography.titleMedium)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tamaño de letra: ${settings.preachFontSize}", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { onFontChange(-2) },
                        enabled = settings.preachFontSize > AppSettings.MIN_PREACH_FONT,
                        modifier = Modifier.semantics { contentDescription = "Letra más pequeña" },
                    ) { Text("A−") }
                    OutlinedButton(
                        onClick = { onFontChange(2) },
                        enabled = settings.preachFontSize < AppSettings.MAX_PREACH_FONT,
                        modifier = Modifier.semantics { contentDescription = "Letra más grande" },
                    ) { Text("A+") }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Tema oscuro", modifier = Modifier.weight(1f))
                    Spacer(Modifier.size(12.dp))
                    Switch(checked = settings.preachDarkTheme, onCheckedChange = onDarkChange)
                }
            }
            HorizontalDivider()
            DropdownMenuItem(
                text = { Text(if (preached) "Ya está marcada como predicada" else "Marcar como predicada") },
                enabled = !preached,
                onClick = { open = false; onMarkPreached() },
            )
        }
    }
}

@Composable
private fun TimerBar(
    elapsedMs: Long,
    running: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal))
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                formatElapsed(elapsedMs),
                style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Monospace),
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Tiempo transcurrido ${formatElapsed(elapsedMs)}" },
            )
            if (running) {
                FilledTonalButton(onClick = onPause) { Text("Pausar") }
            } else {
                FilledTonalButton(onClick = onStart) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Text(if (elapsedMs == 0L) "Iniciar" else "Continuar")
                }
            }
            OutlinedButton(onClick = onReset, enabled = elapsedMs > 0L || running) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Text("Reiniciar")
            }
        }
    }
}

/** Mantiene la pantalla encendida y oculta las barras del sistema solo mientras esta vista esté activa. */
@Composable
private fun KeepScreenOnAndHideBars() {
    val view = LocalView.current
    DisposableEffect(view) {
        view.keepScreenOn = true
        val window = view.context.findActivity()?.window
        val controller = window?.let { WindowCompat.getInsetsController(it, view) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            view.keepScreenOn = false
            controller?.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
