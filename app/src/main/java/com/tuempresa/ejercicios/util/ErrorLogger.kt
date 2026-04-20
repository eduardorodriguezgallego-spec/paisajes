package com.tuempresa.ejercicios.util

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ErrorLogger {

    fun log(context: Context, ruta: String, causa: String) {
        try {
            val directorio = context.getExternalFilesDir(null) ?: context.filesDir
            val fichero = File(directorio, "errores.txt")
            val fecha = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(Date())

            fichero.appendText("$ruta | $fecha | $causa\n")
        } catch (_: Exception) {
        }
    }

    fun obtenerRutaArchivoErrores(context: Context): String {
        val directorio = context.getExternalFilesDir(null) ?: context.filesDir
        return File(directorio, "errores.txt").absolutePath
    }
}