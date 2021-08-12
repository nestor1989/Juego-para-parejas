package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.idea3d.juegoparaparejas.databinding.ActivitySeleccionarBinding

class seleccionarActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySeleccionarBinding


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivitySeleccionarBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.unoBoton.setOnClickListener { elegirPrueba(1) }
        binding.dosBoton.setOnClickListener { elegirPrueba(2) }
        binding.tresBoton.setOnClickListener { elegirPrueba(3) }
        binding.cuatroBoton.setOnClickListener { elegirPrueba(4) }
        binding.cincoBoton.setOnClickListener { elegirPrueba(5) }
        binding.seisBoton.setOnClickListener { elegirPrueba(6) }
        binding.sieteBoton.setOnClickListener { elegirPrueba(7) }
        binding.ochoBoton.setOnClickListener { elegirPrueba(8) }
        binding.nueveBoton.setOnClickListener { elegirPrueba(9) }
        binding.diezBoton.setOnClickListener { elegirPrueba(10) }


    }

    private fun elegirPrueba (prueba:Int){

        val jug1= intent.getStringExtra("jugador1") //recibo datos de second
        val jug2= intent.getStringExtra("jugador2")

        val intent = Intent(this, turnoDeJugUno::class.java)
        intent.putExtra("jugador1", jug1) //envio de datos a activities
        intent.putExtra("jugador2", jug2)
        intent.putExtra("prueba", prueba)
        startActivity(intent)

    }



}