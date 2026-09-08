package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.ActivityJugadorDosBinding
import com.idea3d.juegoparaparejas.util.setUpEdgeToEdge
import com.idea3d.juegoparaparejas.util.startPulseRing


class jugadorDos : AppCompatActivity() {

    private lateinit var binding: ActivityJugadorDosBinding
    private lateinit var subscriptionManager: SubscriptionManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJugadorDosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEdgeToEdge(binding.root)

        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.onPremiumStatusResolved = { initLoadAds() }
        subscriptionManager.setupBillingClient()

        initLoadAds()

        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val prueba = intent.getIntExtra("prueba", 0)
        val respuestas= intent.getIntegerArrayListExtra("respuestas")

        binding.turnoView.text = getString(R.string.turn_player, jug2 ?: "")
        binding.textView5.text = getString(R.string.player_two_instruction, jug1 ?: "")

        fun aJugar(){

            val intent = Intent(this, doceRespuestas::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            intent.putIntegerArrayListExtra("respuestas", respuestas)
            startActivity(intent)

        }

        binding.empiezaBoton.setOnClickListener { aJugar() }

        binding.howToPlayLink.setOnClickListener {
            val onboardingIntent = Intent(this, OnboardingActivity::class.java)
            onboardingIntent.putExtra(OnboardingActivity.EXTRA_REVIEW_MODE, true)
            startActivity(onboardingIntent)
        }

        binding.ctaRing1.startPulseRing()
        binding.ctaRing2.startPulseRing(startDelay = 800L)
    }

    private fun initLoadAds(){
        if (!subscriptionManager.isPremiumUser()) {
            binding.banner.visibility = View.VISIBLE
            val adRequest: AdRequest = AdRequest.Builder().build()
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