package com.idea3d.juegoparaparejas


import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.JugadorUnoBinding


class turnoDeJugUno : AppCompatActivity() {

    private lateinit var binding:JugadorUnoBinding
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = JugadorUnoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.onPremiumStatusResolved = { initLoadAds() }
        subscriptionManager.setupBillingClient()

        initLoadAds()

        val jug1= intent.getStringExtra("jugador1")
        val jug2= intent.getStringExtra("jugador2")
        val prueba= intent.getIntExtra("prueba", 0)
        var pruebaNombre = ""

        fun nombrePrueba(prueba:Int){

            pruebaNombre = when (prueba) {
                1 -> getString(R.string.pack_1_name)
                2 -> getString(R.string.pack_2_name)
                3 -> getString(R.string.pack_3_name)
                4 -> getString(R.string.pack_4_name)
                5 -> getString(R.string.pack_5_name)
                6 -> getString(R.string.pack_6_name)
                7 -> getString(R.string.pack_7_name)
                8 -> getString(R.string.pack_8_name)
                9 -> getString(R.string.pack_9_name)
                10 -> getString(R.string.pack_10_name)
                11 -> getString(R.string.pack_11_name)
                12 -> getString(R.string.pack_12_name)
                13 -> getString(R.string.pack_13_name)
                14 -> getString(R.string.pack_14_name)
                15 -> getString(R.string.pack_15_name)
                16 -> getString(R.string.pack_16_name)
                else -> ""
            }
        }
        nombrePrueba(prueba)

        binding.turnoView.text = getString(
            R.string.turn_player_with_test,
            jug1 ?: "",
            pruebaNombre
        )
        binding.textView5.text = getString(R.string.player_one_instruction, jug2 ?: "")

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