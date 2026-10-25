package com.pablozavfer.cuadernopredicas.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    SYSTEM("Según el sistema"),
    LIGHT("Claro"),
    DARK("Oscuro"),
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val preachFontSize: Int = DEFAULT_PREACH_FONT,
    val preachDarkTheme: Boolean = false,
) {
    companion object {
        const val MIN_PREACH_FONT = 16
        const val MAX_PREACH_FONT = 44
        const val DEFAULT_PREACH_FONT = 22
    }
}

/** Preferencias simples guardadas en el dispositivo. */
class SettingsRepository(context: Context) {
    private val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    var samplesSeeded: Boolean
        get() = prefs.getBoolean(KEY_SAMPLES, false)
        set(value) = prefs.edit().putBoolean(KEY_SAMPLES, value).apply()

    fun setThemeMode(mode: ThemeMode) = write(_settings.value.copy(themeMode = mode))

    fun setPreachFontSize(size: Int) = write(
        _settings.value.copy(
            preachFontSize = size.coerceIn(AppSettings.MIN_PREACH_FONT, AppSettings.MAX_PREACH_FONT),
        ),
    )

    fun setPreachDarkTheme(dark: Boolean) = write(_settings.value.copy(preachDarkTheme = dark))

    private fun read() = AppSettings(
        themeMode = ThemeMode.entries.firstOrNull { it.name == prefs.getString(KEY_THEME, null) } ?: ThemeMode.SYSTEM,
        preachFontSize = prefs.getInt(KEY_FONT, AppSettings.DEFAULT_PREACH_FONT)
            .coerceIn(AppSettings.MIN_PREACH_FONT, AppSettings.MAX_PREACH_FONT),
        preachDarkTheme = prefs.getBoolean(KEY_PREACH_DARK, false),
    )

    private fun write(settings: AppSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putInt(KEY_FONT, settings.preachFontSize)
            .putBoolean(KEY_PREACH_DARK, settings.preachDarkTheme)
            .apply()
        _settings.value = settings
    }

    private companion object {
        const val KEY_THEME = "tema"
        const val KEY_FONT = "tamano_letra_predicacion"
        const val KEY_PREACH_DARK = "predicacion_oscuro"
        const val KEY_SAMPLES = "ejemplos_creados"
    }
}
