package com.pablozavfer.cuadernopredicas.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val month: YearMonth = YearMonth.now(),
    val selected: LocalDate = LocalDate.now(),
    val byDate: Map<LocalDate, List<Sermon>> = emptyMap(),
    val upcoming: List<Sermon> = emptyList(),
)

class CalendarViewModel(repository: SermonRepository) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val selected = MutableStateFlow(LocalDate.now())

    val uiState: StateFlow<CalendarUiState> = combine(repository.observeAll(), month, selected) { all, m, sel ->
        val dated = all.filter { it.preachDate != null }
        val today = LocalDate.now()
        CalendarUiState(
            month = m,
            selected = sel,
            byDate = dated.groupBy { it.preachDate!! },
            upcoming = dated.filter { !it.preachDate!!.isBefore(today) }.sortedBy { it.preachDate }.take(5),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun previousMonth() {
        month.value = month.value.minusMonths(1)
    }

    fun nextMonth() {
        month.value = month.value.plusMonths(1)
    }

    fun goToToday() {
        month.value = YearMonth.now()
        selected.value = LocalDate.now()
    }

    fun select(date: LocalDate) {
        selected.value = date
        month.value = YearMonth.from(date)
    }
}
