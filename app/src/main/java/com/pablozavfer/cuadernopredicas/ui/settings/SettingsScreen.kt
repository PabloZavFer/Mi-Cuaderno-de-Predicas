@file:OptIn(ExperimentalMaterial3Api::class)

package com.pablozavfer.cuadernopredicas.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pablozavfer.cuadernopredicas.BuildConfig
import com.pablozavfer.cuadernopredicas.data.AppSettings
import com.pablozavfer.cuadernopredicas.data.ThemeMode
import com.pablozavfer.cuadernopredicas.data.backup.BackupCodec
import com.pablozavfer.cuadernopredicas.ui.common.appViewModel
import kotlin.math.roundToInt

@Composable
fun SettingsScreen() {
    val vm = appViewModel { SettingsViewModel(it.sermons, it.settings, it.contentResolver) }
    val settings by vm.settings.collectAsStateWithLifecycle()

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri != null) vm.exportTo(uri)
    }
    // Algunos gestores de archivos no reconocen .json como application/json; se aceptan también texto y binario.
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) vm.prepareImport(uri)
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Ajustes") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SectionTitle("Apariencia")
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .selectable(
                                selected = settings.themeMode == mode,
                                onClick = { vm.setThemeMode(mode) },
                                role = Role.RadioButton,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = settings.themeMode == mode, onClick = null)
                        Text(mode.label, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }

            HorizontalDivider()
            SectionTitle("Modo de predicación")
            Text("Tamaño de letra: ${settings.preachFontSize}", style = MaterialTheme.typography.bodyLarge)
            Slider(
                value = settings.preachFontSize.toFloat(),
                onValueChange = { vm.setPreachFontSize(it.roundToInt()) },
                valueRange = AppSettings.MIN_PREACH_FONT.toFloat()..AppSettings.MAX_PREACH_FONT.toFloat(),
                steps = (AppSettings.MAX_PREACH_FONT - AppSettings.MIN_PREACH_FONT) / 2 - 1,
            )
            Text(
                "Así se verá el texto al predicar.",
                fontFamily = FontFamily.Serif,
                fontSize = settings.preachFontSize.sp,
                lineHeight = (settings.preachFontSize * 1.4f).sp,
            )
            Hint("También puedes cambiar el tamaño y el tema claro u oscuro desde el botón «Aa» mientras predicas.")

            HorizontalDivider()
            SectionTitle("Respaldo")
            Hint(
                "Tus prédicas se guardan solo en este dispositivo. Crea un respaldo de vez en cuando y " +
                    "guárdalo en un lugar seguro. Restaurar nunca borra ni sobrescribe tus prédicas actuales.",
            )
            Button(onClick = { exportLauncher.launch(vm.suggestedBackupName()) }, modifier = Modifier.fillMaxWidth()) {
                Text("Exportar respaldo (JSON)")
            }
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Restaurar desde un respaldo")
            }

            HorizontalDivider()
            SectionTitle("Ejemplos")
            Hint("Si borraste las prédicas de ejemplo, puedes volver a agregarlas.")
            OutlinedButton(onClick = { vm.restoreSamples() }, modifier = Modifier.fillMaxWidth()) {
                Text("Agregar los ejemplos de nuevo")
            }

            HorizontalDivider()
            SectionTitle("Privacidad")
            Hint(
                "La aplicación funciona sin internet y sin cuenta. No recopila datos personales, no muestra " +
                    "anuncios y no comparte información con nadie. Solo tú decides cuándo compartir una prédica o un respaldo.",
            )

            HorizontalDivider()
            SectionTitle("Acerca de")
            Hint(
                "Mi Cuaderno de Prédicas · versión ${BuildConfig.VERSION_NAME}\n" +
                    "Gratuita, sin anuncios ni suscripciones. Todas las funciones están disponibles para todos.\n" +
                    "Formato de respaldo: versión ${BackupCodec.CURRENT_VERSION}.",
            )
        }
    }

    BackupDialog(state = vm.backupState, onConfirm = vm::confirmImport, onDismiss = vm::dismissBackupDialog)
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun Hint(text: String) {
    Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun BackupDialog(state: BackupUiState, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    when (state) {
        BackupUiState.Idle -> Unit

        is BackupUiState.Working -> AlertDialog(
            onDismissRequest = {},
            title = { Text(state.message) },
            text = { CircularProgressIndicator() },
            confirmButton = {},
        )

        is BackupUiState.ConfirmImport -> {
            val plan = state.plan
            val lines = buildList {
                add("El respaldo contiene ${plural(plan.totalInBackup, "prédica", "prédicas")}.")
                val one = { n: Int -> n == 1 }
                if (plan.newCount > 0) {
                    add(
                        "• Se ${if (one(plan.newCount)) "agregará" else "agregarán"} " +
                            plural(plan.newCount, "prédica nueva", "prédicas nuevas") + ".",
                    )
                }
                if (plan.conflictCount > 0) {
                    add(
                        "• ${plural(plan.conflictCount, "prédica tiene", "prédicas tienen")} cambios respecto a " +
                            "las tuyas: se ${if (one(plan.conflictCount)) "agregará" else "agregarán"} como copia " +
                            "con «(restaurada)» en el título.",
                    )
                }
                if (plan.identicalCount > 0) {
                    add(
                        "• Se ${if (one(plan.identicalCount)) "omitirá" else "omitirán"} " +
                            plural(plan.identicalCount, "prédica que ya tienes", "prédicas que ya tienes") + ".",
                    )
                }
                add("No se modificará ni eliminará ninguna de tus prédicas actuales.")
            }
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("¿Restaurar este respaldo?") },
                text = { Text(lines.joinToString("\n")) },
                confirmButton = { Button(onClick = onConfirm) { Text("Restaurar") } },
                dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
            )
        }

        is BackupUiState.Result -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(state.title) },
            text = { Text(state.message) },
            confirmButton = { TextButton(onClick = onDismiss) { Text("Aceptar") } },
        )
    }
}
