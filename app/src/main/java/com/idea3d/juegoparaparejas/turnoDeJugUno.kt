package com.idea3d.juegoparaparejas


import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.databinding.JugadorUnoBinding


class turnoDeJugUno : AppCompatActivity() {

    private lateinit var binding:JugadorUnoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = JugadorUnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadAds()

        val jug1= intent.getStringExtra("jugador1")
        val jug2= intent.getStringExtra("jugador2")
        val prueba= intent.getIntExtra("prueba", 0)
        var pruebaNombre=""

        fun nombrePrueba(prueba:Int){

            if (prueba==1){
                pruebaNombre= "ME GUSTAS"
            }else if (prueba ==2){
                pruebaNombre="PRIMERA CITA"
            }else if (prueba ==3) {
                pruebaNombre = "ENAMORADOS"
            }else if (prueba ==4) {
                pruebaNombre = "SEAMOS NOVIOS"
            }else if (prueba ==5) {
                pruebaNombre = "DE NOVIOS"
            }else if (prueba ==6) {
                pruebaNombre = "CÁSATE CONMIGO"
            }else if (prueba ==7) {
                pruebaNombre = "LA BODA"
            }else if (prueba ==8) {
                pruebaNombre = "BODAS DE PLATA"
            }else if (prueba ==9) {
                pruebaNombre = "BODAS DE ORO"
            }else if (prueba ==10) {
                pruebaNombre = "BODAS DE PLATINO"
            }else if (prueba ==11) {
                pruebaNombre = "JUGUETES"
            }else if (prueba ==12) {
                pruebaNombre = "DISFRACES"
            }else if (prueba ==13) {
                pruebaNombre = "FANTASIAS"
            }else if (prueba ==14) {
                pruebaNombre = "FRAGANCIAS"
            }else if (prueba ==15) {
                pruebaNombre = "ROMANCE"
            }else if (prueba ==16) {
                pruebaNombre = "FETICHES"
            }
        }
        nombrePrueba(prueba)

        binding.turnoView.text= "Turno de $jug1 🥰 \n Prueba $pruebaNombre  "
        binding.textView5.text= "Responde las siguientes 12 preguntas con honestidad. Luego $jug2 intentará adivinar tus respuestas 😍"

        fun aJugar(){
            val intent = Intent(this, docePreguntas::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            startActivity(intent)
        }

        binding.empiezaBoton.setOnClickListener { aJugar() }

    }

    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }

}