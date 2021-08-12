package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.idea3d.juegoparaparejas.databinding.ActivitySecondBinding

class secondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val jug1= intent.getStringExtra("jugador1") //recibo datos de main
        val jug2= intent.getStringExtra("jugador2")



        binding.normalBoton.setOnClickListener {
            val intent = Intent(this, seleccionarActivity::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            startActivity(intent)
        }

        binding.adultosBoton.setOnClickListener {
            val intent = Intent(this, seleccionarAdultos::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            startActivity(intent)
        }



    }

}