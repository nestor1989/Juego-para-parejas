package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.databinding.ActivitySeleccionarBinding
import com.idea3d.juegoparaparejas.databinding.SeleccionarAdultosBinding

class seleccionarAdultos : AppCompatActivity() {
    private lateinit var binding: SeleccionarAdultosBinding



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = SeleccionarAdultosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLoadAds()



        binding.onceBoton.setOnClickListener { elegirPrueba(11) }
        binding.doceBoton.setOnClickListener { elegirPrueba(12) }
        binding.treceBoton.setOnClickListener { elegirPrueba(13) }
        binding.catorceBoton.setOnClickListener { elegirPrueba(14) }
        binding.quinceBoton.setOnClickListener { elegirPrueba(15) }
        binding.diesiseisBoton.setOnClickListener { elegirPrueba(16) }


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

    private fun initLoadAds(){
        val adRequest: AdRequest =AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }


}