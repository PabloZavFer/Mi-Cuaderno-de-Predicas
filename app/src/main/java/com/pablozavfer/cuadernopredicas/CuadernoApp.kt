package com.pablozavfer.cuadernopredicas

import android.app.Application
import android.content.ContentResolver
import com.pablozavfer.cuadernopredicas.data.SermonRepository
import com.pablozavfer.cuadernopredicas.data.SettingsRepository
import com.pablozavfer.cuadernopredicas.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CuadernoApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.seedSamplesIfNeeded()
    }
}

/** Dependencias compartidas, creadas a mano para mantener el proyecto simple (sin Hilt). */
class AppContainer(app: Application) {
    /** Sobrevive a las pantallas: se usa para terminar guardados aunque el usuario salga del editor. */
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    val database: AppDatabase by lazy { AppDatabase.build(app) }
    val sermons: SermonRepository by lazy { SermonRepository(database.sermonDao()) }
    val settings = SettingsRepository(app)
    val contentResolver: ContentResolver = app.contentResolver

    fun seedSamplesIfNeeded() {
        if (settings.samplesSeeded) return
        appScope.launch {
            runCatching { sermons.addMissingSamples() }
                .onSuccess { settings.samplesSeeded = true }
        }
    }
}
