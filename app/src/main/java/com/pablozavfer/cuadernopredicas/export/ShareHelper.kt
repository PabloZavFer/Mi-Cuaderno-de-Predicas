package com.pablozavfer.cuadernopredicas.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.pablozavfer.cuadernopredicas.data.model.Sermon
import com.pablozavfer.cuadernopredicas.util.TextSearch
import com.pablozavfer.cuadernopredicas.util.openForOverwrite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ShareHelper {

    fun shareText(context: Context, sermon: Sermon) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, sermon.displayTitle)
            putExtra(Intent.EXTRA_TEXT, sermon.toShareText())
        }
        context.startActivity(Intent.createChooser(intent, "Compartir prédica"))
    }

    fun pdfFileName(sermon: Sermon): String {
        val slug = TextSearch.normalize(sermon.displayTitle)
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .take(60)
            .ifEmpty { "predica" }
        return "$slug.pdf"
    }

    /** Genera el PDF en la caché privada de la app (se reemplaza en cada exportación). */
    suspend fun createPdfInCache(context: Context, sermon: Sermon): File = withContext(Dispatchers.IO) {
        val dir = File(context.cacheDir, "pdf").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }
        val file = File(dir, pdfFileName(sermon))
        file.outputStream().use { SermonPdfWriter.write(sermon, it) }
        file
    }

    fun sharePdf(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            clipData = ClipData.newRawUri(title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir PDF"))
    }

    /** Escribe el PDF en el archivo que el usuario eligió con el selector del sistema. */
    suspend fun savePdfTo(context: Context, sermon: Sermon, uri: Uri) = withContext(Dispatchers.IO) {
        context.contentResolver.openForOverwrite(uri).use { SermonPdfWriter.write(sermon, it) }
    }
}
