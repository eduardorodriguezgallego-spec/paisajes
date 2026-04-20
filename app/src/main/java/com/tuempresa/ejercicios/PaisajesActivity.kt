package com.tuempresa.ejercicios

import android.media.MediaPlayer
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tuempresa.ejercicios.databinding.ActivityPaisajesBinding

class PaisajesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaisajesBinding
    private lateinit var gestureDetector: GestureDetector
    private var mediaPlayer: MediaPlayer? = null

    // Guarda la valoración de cada imagen en memoria.
    // Clave = posición de la imagen, valor = nota
    private val valoraciones = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaisajesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaPlayer = MediaPlayer.create(this, R.raw.cambio)

        binding.viewFlipper.setInAnimation(this, R.anim.slide_in_left)
        binding.viewFlipper.setOutAnimation(this, R.anim.slide_out_right)

        iniciarAnimacionTitulo()
        configurarGestos()
        configurarValidacion()
        actualizarContador()
        actualizarCampoValoracion()
    }

    private fun iniciarAnimacionTitulo() {
        val mover = AnimationUtils.loadAnimation(this, R.anim.slide_in_left)
        val desvanecer = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        binding.tvTituloAnimado.startAnimation(mover)
        reproducirSonido()

        mover.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) = Unit

            override fun onAnimationRepeat(animation: Animation?) = Unit

            override fun onAnimationEnd(animation: Animation?) {
                binding.tvTituloAnimado.startAnimation(desvanecer)

                desvanecer.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) = Unit

                    override fun onAnimationRepeat(animation: Animation?) = Unit

                    override fun onAnimationEnd(animation: Animation?) {
                        binding.tvTituloAnimado.alpha = 1f
                    }
                })
            }
        })
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
                    if (diferenciaX > 0) {
                        binding.viewFlipper.showPrevious()
                    } else {
                        binding.viewFlipper.showNext()
                    }

                    actualizarContador()
                    actualizarCampoValoracion()
                    reproducirSonido()
                    return true
                }
                return false
            }
        })

        binding.viewFlipper.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun configurarValidacion() {
        binding.btnGuardarValoracion.setOnClickListener {
            val texto = binding.etValoracion.text.toString().trim()
            val valor = texto.toIntOrNull()

            if (valor == null || valor !in 1..10) {
                Toast.makeText(
                    this,
                    "Introduce una valoración entre 1 y 10",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val posicion = binding.viewFlipper.displayedChild
                valoraciones[posicion] = valor

                Toast.makeText(
                    this,
                    "Guardado: imagen ${posicion + 1} → $valor",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun actualizarContador() {
        val actual = binding.viewFlipper.displayedChild + 1
        val total = binding.viewFlipper.childCount
        binding.tvContadorImagen.text = "$actual / $total"
    }

    private fun actualizarCampoValoracion() {
        val posicion = binding.viewFlipper.displayedChild
        val valor = valoraciones[posicion]

        if (valor != null) {
            binding.etValoracion.setText(valor.toString())
        } else {
            binding.etValoracion.setText("")
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