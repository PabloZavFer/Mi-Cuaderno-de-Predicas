package com.pablozavfer.cuadernopredicas.ui.preach

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.SettingsRepository
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.data.model.SermonStatus
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Vive mientras la pantalla está abierta, incluso al girar el teléfono: el temporizador no se reinicia. */
class PreachViewModel(
    private val repository: SermonRepository,
    private val settingsRepository: SettingsRepository,
    sermonId: Long,
) : ViewModel() {

    var sermon by mutableStateOf<Sermon?>(null)
        private set
    var notFound by mutableStateOf(false)
        private set

    val settings = settingsRepository.settings

    // ----- Temporizador basado en el reloj del sistema para no acumular desfase -----
    var elapsedMs by mutableLongStateOf(0L)
        private set
    var running by mutableStateOf(false)
        private set
    private var accumulatedMs = 0L
    private var startedAt = 0L
    private var ticker: Job? = null

    init {
        viewModelScope.launch {
            val loaded = repository.get(sermonId)
            if (loaded == null) notFound = true else sermon = loaded
        }
    }

    fun start() {
        if (running) return
        startedAt = SystemClock.elapsedRealtime()
        running = true
        ticker = viewModelScope.launch {
            while (isActive) {
                elapsedMs = accumulatedMs + (SystemClock.elapsedRealtime() - startedAt)
                delay(250)
            }
        }
    }

    fun pause() {
        if (!running) return
        ticker?.cancel()
        accumulatedMs += SystemClock.elapsedRealtime() - startedAt
        elapsedMs = accumulatedMs
        running = false
    }

    fun reset() {
        pause()
        accumulatedMs = 0L
        elapsedMs = 0L
    }

    fun changeFontSize(delta: Int) = settingsRepository.setPreachFontSize(settings.value.preachFontSize + delta)

    fun setDarkTheme(dark: Boolean) = settingsRepository.setPreachDarkTheme(dark)

    fun markPreached() {
        val current = sermon ?: return
        if (current.status == SermonStatus.PREACHED) return
        val updated = current.copy(status = SermonStatus.PREACHED, updatedAt = System.currentTimeMillis())
        sermon = updated
        viewModelScope.launch { repository.save(updated) }
    }
}

fun formatElapsed(ms: Long): String {
    val totalSeconds = ms / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
}
