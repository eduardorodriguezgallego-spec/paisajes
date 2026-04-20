package com.tuempresa.ejercicios

import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.tuempresa.ejercicios.databinding.ActivityImagenesServidorBinding
import com.tuempresa.ejercicios.util.ErrorLogger
import com.tuempresa.ejercicios.util.TxtDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImagenesServidorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImagenesServidorBinding
    private lateinit var gestureDetector: GestureDetector
    private var mediaPlayer: MediaPlayer? = null

    // Cambia esta URL si usas otra distinta
    private val urlTxt = "https://dam.org.es/ficheros/imagenes.txt"

    private var totalImagenes = 0
    private var imagenesCargadas = 0
    private var imagenesFallidas = 0
    private var imagenesProcesadas = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityImagenesServidorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.cambio)

        binding.viewFlipperServidor.setInAnimation(this, R.anim.fade_in)
        binding.viewFlipperServidor.setOutAnimation(this, R.anim.fade_out)

        configurarGestos()

        binding.btnDescargar.setOnClickListener {
            descargarImagenesDesdeServidor()
        }
    }

    private fun descargarImagenesDesdeServidor() {
        binding.btnDescargar.isEnabled = false
        binding.viewFlipperServidor.removeAllViews()

        totalImagenes = 0
        imagenesCargadas = 0
        imagenesFallidas = 0
        imagenesProcesadas = 0

        binding.tvEstado.text = "Descargando imagenes.txt..."

        lifecycleScope.launch {
            try {
                val urls = withContext(Dispatchers.IO) {
                    TxtDownloader.descargarLineas(urlTxt)
                }

                if (urls.isEmpty()) {
                    ErrorLogger.log(this@ImagenesServidorActivity, urlTxt, "El fichero de imágenes está vacío")
                    binding.tvEstado.text = "El fichero no contiene rutas"
                    binding.btnDescargar.isEnabled = true
                    return@launch
                }

                totalImagenes = urls.size
                binding.tvEstado.text = "Rutas encontradas: $totalImagenes"

                val imageLoader = ImageLoader(this@ImagenesServidorActivity)

                urls.forEach { ruta ->
                    launch {
                        cargarImagen(imageLoader, ruta)
                    }
                }

            } catch (e: Exception) {
                ErrorLogger.log(
                    this@ImagenesServidorActivity,
                    urlTxt,
                    "No se puede descargar el fichero de imágenes: ${e.message ?: "Error desconocido"}"
                )

                binding.tvEstado.text = "Error al descargar imagenes.txt"
                binding.btnDescargar.isEnabled = true
                Toast.makeText(
                    this@ImagenesServidorActivity,
                    "No se pudo descargar el fichero de imágenes",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private suspend fun cargarImagen(imageLoader: ImageLoader, ruta: String) {
        try {
            val request = ImageRequest.Builder(this)
                .data(ruta)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)

            when (result) {
                is SuccessResult -> {
                    val drawable = result.drawable
                    mostrarImagenCargada(drawable, ruta)
                }

                is ErrorResult -> {
                    val causa = result.throwable.message ?: "Error al descargar la imagen"
                    registrarErrorImagen(ruta, causa)
                }
            }
        } catch (e: Exception) {
            registrarErrorImagen(ruta, e.message ?: "Excepción desconocida")
        }
    }

    private fun mostrarImagenCargada(drawable: Drawable, ruta: String) {
        val imageView = ImageView(this).apply {
            layoutParams = android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT
            )
            scaleType = ImageView.ScaleType.CENTER_CROP
            setImageDrawable(drawable)
            contentDescription = "Imagen descargada: $ruta"
        }

        binding.viewFlipperServidor.addView(imageView)

        imagenesCargadas++
        imagenesProcesadas++
        actualizarEstado()
        actualizarContadorImagenes()

        if (binding.viewFlipperServidor.childCount == 1) {
            reproducirSonido()
        }

        comprobarFinCarga()
    }

    private fun registrarErrorImagen(ruta: String, causaOriginal: String) {
        val causaBonita = traducirCausaError(causaOriginal)

        ErrorLogger.log(
            this,
            ruta,
            causaBonita
        )

        imagenesFallidas++
        imagenesProcesadas++
        actualizarEstado()
        comprobarFinCarga()
    }

    private fun traducirCausaError(causa: String): String {
        val texto = causa.lowercase()

        return when {
            "404" in texto -> "Error HTTP 404: el fichero no existe"
            "403" in texto -> "Error HTTP 403: acceso denegado"
            "500" in texto -> "Error HTTP 500: fallo del servidor"
            "timeout" in texto -> "Tiempo de espera agotado"
            "unable to resolve host" in texto -> "Servidor no encontrado"
            "failed to connect" in texto -> "No se pudo conectar con el servidor"
            "cleartext http traffic" in texto -> "Tráfico HTTP no permitido"
            "ssl" in texto -> "Error SSL en la conexión"
            else -> "No se pudo descargar la imagen: $causa"
        }
    }

    private fun actualizarEstado() {
        binding.tvEstado.text =
            "Total: $totalImagenes | Correctas: $imagenesCargadas | Fallidas: $imagenesFallidas | Procesadas: $imagenesProcesadas"
    }

    private fun actualizarContadorImagenes() {
        val totalVisibles = binding.viewFlipperServidor.childCount
        if (totalVisibles > 0) {
            val actual = binding.viewFlipperServidor.displayedChild + 1
            binding.tvContadorServidor.text = "$actual / $totalVisibles"
        } else {
            binding.tvContadorServidor.text = "0 / 0"
        }
    }

    private fun comprobarFinCarga() {
        if (imagenesProcesadas == totalImagenes) {
            binding.btnDescargar.isEnabled = true

            if (imagenesCargadas == 0) {
                binding.tvEstado.text =
                    "No se pudo cargar ninguna imagen. Revisa errores.txt"
                Toast.makeText(
                    this,
                    "No se pudo cargar ninguna imagen",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                binding.tvEstado.text =
                    "Carga finalizada. Correctas: $imagenesCargadas | Fallidas: $imagenesFallidas"
                Toast.makeText(
                    this,
                    "Carga completada",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun configurarGestos() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            private val umbral = 100
            private val velocidadMin = 100

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                val diferenciaX = e2.x - (e1?.x ?: 0f)

                if (kotlin.math.abs(diferenciaX) > umbral &&
                    kotlin.math.abs(velocityX) > velocidadMin
                ) {
                    if (binding.viewFlipperServidor.childCount > 0) {
                        if (diferenciaX > 0) {
                            binding.viewFlipperServidor.showPrevious()
                        } else {
                            binding.viewFlipperServidor.showNext()
                        }
                        actualizarContadorImagenes()
                        reproducirSonido()
                    }
                    return true
                }
                return false
            }
        })

        binding.viewFlipperServidor.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun reproducirSonido() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.seekTo(0)
            }
            it.start()
        }
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }
}