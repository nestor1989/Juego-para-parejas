package com.idea3d.juegoparaparejas


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
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

        binding.textResult.text= "Conoces a tu amor\n en un ${promedio.toInt()}%"



        fun roles(){

            val intent = Intent(this, turnoDeJugUno::class.java)
            intent.putExtra("jugador2", jug1) //envio de datos a activities
            intent.putExtra("jugador1", jug2)
            intent.putExtra("prueba", prueba)
            startActivity(intent)//nos vamos
        }

        fun volver(){

            val intent = Intent(this, secondActivity::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            startActivity(intent)//nos vamo

        }

        fun setAnimation(promedio:Int) {
            when (promedio){
                in 0..19 -> binding.estrellas.setMaxFrame(40)
                in 20..39 -> binding.estrellas.setMaxFrame(70)
                in 40..59 -> binding.estrellas.setMaxFrame(130)
                in 60..79 -> binding.estrellas.setMaxFrame(200)
                else -> binding.estrellas.setMaxFrame(270)
            }
            binding.estrellas.playAnimation()
        }

        binding.rolesBoton.setOnClickListener { roles() }
        binding.volverBoton.setOnClickListener { volver() }
        setAnimation(promedio.toInt())

    }
    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }

}