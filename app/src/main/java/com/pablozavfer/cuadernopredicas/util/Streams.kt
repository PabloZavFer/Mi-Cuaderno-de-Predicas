package com.pablozavfer.cuadernopredicas.util

import android.content.ContentResolver
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.OutputStream

/** Abre un archivo elegido por el usuario reemplazando su contenido. Algunos proveedores no aceptan "wt". */
fun ContentResolver.openForOverwrite(uri: Uri): OutputStream {
    val stream = try {
        openOutputStream(uri, "wt")
    } catch (e: FileNotFoundException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    } ?: openOutputStream(uri, "w")
    return stream ?: throw IOException("No se pudo abrir el archivo de destino")
}

/** Lee el archivo completo, rechazándolo si supera [maxBytes]. */
fun ContentResolver.readTextLimited(uri: Uri, maxBytes: Int): String {
    val input = openInputStream(uri) ?: throw IOException("No se pudo abrir el archivo")
    input.use {
        val out = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0
        while (true) {
            val read = it.read(buffer)
            if (read < 0) break
            total += read
            if (total > maxBytes) throw FileTooLargeException()
            out.write(buffer, 0, read)
        }
        return out.toString(Charsets.UTF_8.name())
    }
}

class FileTooLargeException : IOException("Archivo demasiado grande")
