package com.pablozavfer.cuadernopredicas.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pablozavfer.cuadernopredicas.BuildConfig
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.SettingsRepository
import com.pablozavfer.cuadernopredicas.data.ThemeMode
import com.pablozavfer.cuadernopredicas.data.backup.BackupCodec
import com.pablozavfer.cuadernopredicas.data.backup.BackupParseResult
import com.pablozavfer.cuadernopredicas.data.backup.ImportPlan
import com.pablozavfer.cuadernopredicas.data.backup.planImport
import com.pablozavfer.cuadernopredicas.util.FileTooLargeException
import com.pablozavfer.cuadernopredicas.util.openForOverwrite
import com.pablozavfer.cuadernopredicas.util.readTextLimited
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate

sealed interface BackupUiState {
    data object Idle : BackupUiState
    data class Working(val message: String) : BackupUiState
    data class ConfirmImport(val plan: ImportPlan) : BackupUiState
    data class Result(val title: String, val message: String) : BackupUiState
}

class SettingsViewModel(
    private val repository: SermonRepository,
    private val settingsRepository: SettingsRepository,
    private val contentResolver: ContentResolver,
) : ViewModel() {

    val settings = settingsRepository.settings

    var backupState by mutableStateOf<BackupUiState>(BackupUiState.Idle)
        private set

    fun setThemeMode(mode: ThemeMode) = settingsRepository.setThemeMode(mode)
    fun setPreachFontSize(size: Int) = settingsRepository.setPreachFontSize(size)

    fun suggestedBackupName() = "respaldo-cuaderno-predicas-${LocalDate.now()}.json"

    fun exportTo(uri: Uri) = viewModelScope.launch {
        backupState = BackupUiState.Working("Creando respaldo…")
        backupState = try {
            val sermons = repository.getAll()
            val json = BackupCodec.encode(sermons, Instant.now(), BuildConfig.VERSION_NAME)
            withContext(Dispatchers.IO) {
                contentResolver.openForOverwrite(uri).use { it.write(json.toByteArray(Charsets.UTF_8)) }
            }
            BackupUiState.Result(
                "Respaldo creado",
                "Se guardaron ${plural(sermons.size, "prédica", "prédicas")} en el archivo. " +
                    "Guárdalo en un lugar seguro, como tu nube o una memoria externa.",
            )
        } catch (e: Exception) {
            BackupUiState.Result("No se pudo crear el respaldo", "Revisa que haya espacio disponible e inténtalo de nuevo.")
        }
    }

    /** Lee y valida el archivo; si es correcto, muestra un resumen y pide confirmación antes de importar. */
    fun prepareImport(uri: Uri) = viewModelScope.launch {
        backupState = BackupUiState.Working("Revisando el respaldo…")
        val text = try {
            withContext(Dispatchers.IO) { contentResolver.readTextLimited(uri, BackupCodec.MAX_FILE_BYTES) }
        } catch (e: FileTooLargeException) {
            backupState = BackupUiState.Result("No se puede restaurar", "El archivo es demasiado grande para ser un respaldo.")
            return@launch
        } catch (e: Exception) {
            backupState = BackupUiState.Result("No se puede restaurar", "No se pudo leer el archivo seleccionado.")
            return@launch
        }
        backupState = when (val parsed = withContext(Dispatchers.Default) { BackupCodec.decode(text) }) {
            is BackupParseResult.Failure -> BackupUiState.Result("No se puede restaurar", parsed.message)
            is BackupParseResult.Success -> {
                val plan = planImport(parsed.sermons, repository.getAll())
                if (plan.toInsert.isEmpty()) {
                    BackupUiState.Result(
                        "Nada que restaurar",
                        "Todas las prédicas del respaldo ya están en tu biblioteca. No se hizo ningún cambio.",
                    )
                } else {
                    BackupUiState.ConfirmImport(plan)
                }
            }
        }
    }

    fun confirmImport() {
        val plan = (backupState as? BackupUiState.ConfirmImport)?.plan ?: return
        viewModelScope.launch {
            backupState = BackupUiState.Working("Restaurando…")
            backupState = try {
                repository.insertAll(plan.toInsert)
                BackupUiState.Result(
                    "Restauración completada",
                    "Se agregaron ${plural(plan.toInsert.size, "prédica", "prédicas")} a tu biblioteca.",
                )
            } catch (e: Exception) {
                BackupUiState.Result("No se pudo restaurar", "No se agregó ninguna prédica. Inténtalo de nuevo.")
            }
        }
    }

    fun dismissBackupDialog() {
        backupState = BackupUiState.Idle
    }

    fun restoreSamples() = viewModelScope.launch {
        val added = repository.addMissingSamples()
        backupState = BackupUiState.Result(
            "Ejemplos",
            if (added == 0) "Los ejemplos ya están en tu biblioteca." else "Se agregaron ${plural(added, "ejemplo", "ejemplos")}.",
        )
    }
}

fun plural(count: Int, singular: String, pluralForm: String) = "$count ${if (count == 1) singular else pluralForm}"
