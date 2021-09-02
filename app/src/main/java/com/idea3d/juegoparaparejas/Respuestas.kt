package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.databinding.ActivityDoceRespuestasBinding
import com.idea3d.juegoparaparejas.databinding.ActivityRespuestasBinding

class Respuestas : AppCompatActivity() {

    private lateinit var binding: ActivityRespuestasBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRespuestasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadAds()

        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val promedio = intent.getDoubleExtra("promedio", 0.0)
        val prueba= intent.getIntExtra("prueba", 0)

        binding.textResult.text= "El resultado es ${promedio.toInt()} % 🤣😅"

        fun roles(){
            val intent = Intent(this, turnoDeJugUno::class.java)
            intent.putExtra("jugador2", jug1) //envio de datos a activities
            intent.putExtra("jugador1", jug2)
            intent.putExtra("prueba", prueba)
            startActivity(intent)//nos vamos
        }

        fun volver(){
            if (prueba<=10) {
                val intent = Intent(this, seleccionarActivity::class.java)
                intent.putExtra("jugador1", jug1) //envio de datos a activities
                intent.putExtra("jugador2", jug2)
                startActivity(intent)//nos vamo
            }else if(prueba>10){
                val intent = Intent(this, seleccionarAdultos::class.java)
                intent.putExtra("jugador1", jug1) //envio de datos a activities
                intent.putExtra("jugador2", jug2)
                startActivity(intent)//nos vamo
            }
        }

        binding.rolesBoton.setOnClickListener { roles() }
        binding.volverBoton.setOnClickListener { volver() }

    }
    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }
}