package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.databinding.ActivityJugadorDosBinding


class jugadorDos : AppCompatActivity() {

    private lateinit var binding: ActivityJugadorDosBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJugadorDosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initLoadAds()

        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val prueba = intent.getIntExtra("prueba", 0)
        val respuestas= intent.getIntegerArrayListExtra("respuestas")

        binding.turnoView.text= "Turno de $jug2 🥰   "
        binding.textView5.text= "Responde las mismas preguntas que $jug1 y trata de que tus respuestas coincidan con las suyas"

        fun aJugar(){

            val intent = Intent(this, doceRespuestas::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            intent.putIntegerArrayListExtra("respuestas", respuestas)
            startActivity(intent)

        }

        binding.empiezaBoton.setOnClickListener { aJugar() }


    }

    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }



}