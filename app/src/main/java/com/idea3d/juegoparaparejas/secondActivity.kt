package com.idea3d.juegoparaparejas

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.idea3d.juegoparaparejas.billing.SubscriptionDialogFragment
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.ActivitySecondBinding
import com.idea3d.juegoparaparejas.util.setUpEdgeToEdge

class secondActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySecondBinding
    private lateinit var subscriptionManager: SubscriptionManager
    private var rewardedAd: RewardedAd? = null

    companion object {
        // TODO: reemplazar por el ad unit ID real de "Rewarded" creado en AdMob
        // (Ad units > Create ad unit > Rewarded) antes de publicar. Este es el
        // ID de test oficial de Google, siempre sirve anuncios de prueba.
        private const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySecondBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEdgeToEdge(binding.root)

        // ...existing code...
        subscriptionManager = SubscriptionManager(this)
        // Recargar los anuncios una vez que Google Play confirme el estado premium real
        subscriptionManager.onPremiumStatusResolved = { initLoadAds() }
        subscriptionManager.setupBillingClient()

        initLoadAds()
        loadRewardedAd()

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
                goToAdultos(jug1, jug2)
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
                        goToAdultos(jug1, jug2)
                    }
                }
            },
            onDismiss = {},
            onWatchAdClick = { showRewardedAd(jug1, jug2) }
        )
        dialog.show(supportFragmentManager, "subscription_dialog")
    }

    private fun goToAdultos(jug1: String?, jug2: String?) {
        val intent = Intent(this, seleccionarAdultos::class.java)
        intent.putExtra("jugador1", jug1)
        intent.putExtra("jugador2", jug2)
        startActivity(intent)
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    private fun showRewardedAd(jug1: String?, jug2: String?) {
        val ad = rewardedAd
        if (ad == null) {
            Toast.makeText(this, getString(R.string.rewarded_ad_not_ready), Toast.LENGTH_SHORT).show()
            loadRewardedAd()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                loadRewardedAd()
            }
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                rewardedAd = null
                loadRewardedAd()
            }
        }

        ad.show(this) {
            // Recompensa obtenida: desbloquear el pack adultos para esta partida
            goToAdultos(jug1, jug2)
        }
    }

    private fun initLoadAds(){
        if (!subscriptionManager.isPremiumUser()) {
            binding.banner.visibility = View.VISIBLE
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





