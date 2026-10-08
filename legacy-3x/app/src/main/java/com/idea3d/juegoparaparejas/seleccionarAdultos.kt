package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.SeleccionarAdultosBinding
import com.idea3d.juegoparaparejas.util.setUpEdgeToEdge

class seleccionarAdultos : AppCompatActivity() {
    private lateinit var binding: SeleccionarAdultosBinding
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SeleccionarAdultosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEdgeToEdge(binding.root)

        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.onPremiumStatusResolved = { initLoadAds() }
        subscriptionManager.setupBillingClient()

        initLoadAds()

        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        binding.subtitleNames.text = getString(R.string.player_names_pair, jug1 ?: "", jug2 ?: "")
        binding.backButton.setOnClickListener { finish() }

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
        if (!subscriptionManager.isPremiumUser()) {
            binding.banner.visibility = View.VISIBLE
            val adRequest = AdRequest.Builder().build()
            binding.banner.loadAd(adRequest)
        } else {
            binding.banner.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        subscriptionManager.disconnect()
    }
}