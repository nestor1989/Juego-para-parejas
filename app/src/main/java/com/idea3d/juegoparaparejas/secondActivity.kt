package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.idea3d.juegoparaparejas.billing.SubscriptionDialogFragment
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.ActivitySecondBinding

class secondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private lateinit var subscriptionManager: SubscriptionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // ...existing code...
        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.setupBillingClient()
        
        initLoadAds()

        val jug1= intent.getStringExtra("jugador1") //recibo datos de main
        val jug2= intent.getStringExtra("jugador2")

        binding.normalBoton.setOnClickListener {
            val intent = Intent(this, seleccionarActivity::class.java)
            intent.putExtra("jugador1", jug1) //envio de datos a activities
            intent.putExtra("jugador2", jug2)
            startActivity(intent)
        }

        binding.adultosBoton.setOnClickListener {
            if (subscriptionManager.isPremiumUser()) {
                val intent = Intent(this, seleccionarAdultos::class.java)
                intent.putExtra("jugador1", jug1) //envio de datos a activities
                intent.putExtra("jugador2", jug2)
                startActivity(intent)
            } else {
                showSubscriptionDialog(jug1, jug2)
            }
        }
    }

    private fun showSubscriptionDialog(jug1: String?, jug2: String?) {
        val dialog = SubscriptionDialogFragment(
            onSubscribeClick = {
                subscriptionManager.launchSubscriptionFlow(this) { success ->
                    if (success && subscriptionManager.isPremiumUser()) {
                        val intent = Intent(this, seleccionarAdultos::class.java)
                        intent.putExtra("jugador1", jug1)
                        intent.putExtra("jugador2", jug2)
                        startActivity(intent)
                    }
                }
            },
            onDismiss = {}
        )
        dialog.show(supportFragmentManager, "subscription_dialog")
    }

    private fun initLoadAds(){
        if (!subscriptionManager.isPremiumUser()) {
            val adRequest: AdRequest =AdRequest.Builder().build()
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





