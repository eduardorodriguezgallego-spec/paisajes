package com.tuempresa.ejercicios.util

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object TxtDownloader {

    fun descargarLineas(urlTexto: String): List<String> {
        val conexion = URL(urlTexto).openConnection() as HttpURLConnection
        conexion.connectTimeout = 5000
        conexion.readTimeout = 5000
        conexion.requestMethod = "GET"
        conexion.instanceFollowRedirects = true

        val codigo = conexion.responseCode

        if (codigo != HttpURLConnection.HTTP_OK) {
            throw Exception("Error HTTP $codigo")
        }

        val lector = BufferedReader(InputStreamReader(conexion.inputStream))
        val lineas = mutableListOf<String>()

        lector.useLines { secuencia ->
            secuencia.forEach { linea ->
                val limpia = linea.trim()
                if (limpia.isNotEmpty()) {
                    lineas.add(limpia)
                }
            }
        }

        conexion.disconnect()
        return lineas
    }
}