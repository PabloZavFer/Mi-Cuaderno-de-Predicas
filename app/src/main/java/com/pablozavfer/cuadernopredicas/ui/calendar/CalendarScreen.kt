@file:OptIn(ExperimentalMaterial3Api::class)

package com.pablozavfer.cuadernopredicas.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.ui.common.StatusBadge
import com.pablozavfer.cuadernopredicas.ui.common.appViewModel
import com.pablozavfer.cuadernopredicas.util.Dates
import java.time.LocalDate
import java.time.YearMonth

private val WEEK_DAYS = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun CalendarScreen(onOpenSermon: (Long) -> Unit, onNewSermonOn: (LocalDate) -> Unit) {
    val vm = appViewModel { CalendarViewModel(it.sermons) }
    val state by vm.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()
    val selectedSermons = state.byDate[state.selected].orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendario") },
                actions = { TextButton(onClick = vm::goToToday) { Text("Hoy") } },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = padding.calculateTopPadding() + 4.dp,
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonthGrid(
                    month = state.month,
                    selected = state.selected,
                    today = today,
                    datesWithSermons = state.byDate.keys,
                    onPrevious = vm::previousMonth,
                    onNext = vm::nextMonth,
                    onSelect = vm::select,
                )
            }
            item {
                Text(Dates.long(state.selected), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }
            if (selectedSermons.isEmpty()) {
                item {
                    Text(
                        "No hay prédicas para este día.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(selectedSermons, key = { "dia-${it.id}" }) { sermon ->
                SermonEventCard(sermon, onClick = { onOpenSermon(sermon.id) })
            }
            item {
                OutlinedButton(onClick = { onNewSermonOn(state.selected) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text("Nueva prédica para este día", modifier = Modifier.padding(start = 8.dp))
                }
            }

            item {
                Text("Próximas prédicas", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 12.dp))
            }
            if (state.upcoming.isEmpty()) {
                item {
                    Text(
                        "Aún no tienes prédicas programadas. Asigna una fecha prevista en el editor " +
                            "y aparecerá aquí y en el calendario.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            items(state.upcoming, key = { "proxima-${it.id}" }) { sermon ->
                SermonEventCard(sermon, showDate = true, onClick = { onOpenSermon(sermon.id) })
            }
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    selected: LocalDate,
    today: LocalDate,
    datesWithSermons: Set<LocalDate>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSelect: (LocalDate) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Card(colors = CardDefaults.cardColors(containerColor = colors.surfaceContainerLowest)) {
        Column(Modifier.padding(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior")
                }
                Text(
                    Dates.month(month),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onNext) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Mes siguiente")
                }
            }
            Row {
                WEEK_DAYS.forEach { day ->
                    Text(
                        day,
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                    )
                }
            }
            // La semana empieza en lunes, como es habitual en el calendario en español.
            val offset = month.atDay(1).dayOfWeek.value - 1
            val days = month.lengthOfMonth()
            val rows = (offset + days + 6) / 7
            for (row in 0 until rows) {
                Row {
                    for (col in 0 until 7) {
                        val dayNumber = row * 7 + col - offset + 1
                        Box(Modifier.weight(1f).heightIn(min = 48.dp), contentAlignment = Alignment.Center) {
                            if (dayNumber in 1..days) {
                                val date = month.atDay(dayNumber)
                                DayCell(
                                    date = date,
                                    isSelected = date == selected,
                                    isToday = date == today,
                                    hasSermon = date in datesWithSermons,
                                    onClick = { onSelect(date) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, isSelected: Boolean, isToday: Boolean, hasSermon: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val description = buildString {
        append(Dates.long(date))
        if (hasSermon) append(", tiene prédica")
        if (isToday) append(", hoy")
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .clip(CircleShape)
            .background(if (isSelected) colors.primary else colors.surfaceContainerLowest, CircleShape)
            .then(if (isToday && !isSelected) Modifier.border(1.5.dp, colors.tertiary, CircleShape) else Modifier)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .semantics { contentDescription = description },
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) colors.onPrimary else colors.onSurface,
        )
        Box(
            Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .background(
                    when {
                        !hasSermon -> colors.surfaceContainerLowest.copy(alpha = 0f)
                        isSelected -> colors.onPrimary
                        else -> colors.tertiary
                    },
                    CircleShape,
                ),
        )
    }
}

@Composable
private fun SermonEventCard(sermon: Sermon, showDate: Boolean = false, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(sermon.displayTitle, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (sermon.scripture.isNotBlank()) {
                Text(sermon.scripture, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusBadge(sermon.status)
                if (showDate) {
                    sermon.preachDate?.let {
                        Text(Dates.short(it), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
