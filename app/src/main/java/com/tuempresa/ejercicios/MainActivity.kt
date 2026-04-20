package com.tuempresa.ejercicios

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tuempresa.ejercicios.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnPaisajes.setOnClickListener {
            startActivity(Intent(this, PaisajesActivity::class.java))
        }

        binding.btnImagenes.setOnClickListener {
            startActivity(Intent(this, ImagenesServidorActivity::class.java))
        }
    }
}