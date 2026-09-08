package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var subscriptionManager: SubscriptionManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.onPremiumStatusResolved = { initLoadAds() }
        subscriptionManager.setupBillingClient()

        initLoadAds()

        var jug1:String
        var jug2:String
        val text = getString(R.string.enter_player_names) //texto
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