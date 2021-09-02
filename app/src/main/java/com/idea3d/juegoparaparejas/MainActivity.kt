package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.idea3d.juegoparaparejas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        initLoadAds()

        var jug1:String
        var jug2:String
        val text = "Ingresar el nombre de los jugadores" //texto
        val duration = Toast.LENGTH_SHORT // y duracion de toast





        binding.empezarBoton.setOnClickListener {
            jug1 = binding.jugador1.text.toString()
            jug2 = binding.jugador2.text.toString()

            if (jug1 != "" && jug2 != "") {
                val intent = Intent(this, secondActivity::class.java)
                intent.putExtra("jugador1", jug1) //envio de datos a activities
                intent.putExtra("jugador2", jug2)
                startActivity(intent)
            }else {
                val toast = Toast.makeText(applicationContext, text, duration)
                toast.show()
            }
        }




    }

    private fun initLoadAds(){
        val adRequest: AdRequest=AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }

}