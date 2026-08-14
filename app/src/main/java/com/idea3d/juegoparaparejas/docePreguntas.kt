package com.idea3d.juegoparaparejas
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.idea3d.juegoparaparejas.billing.SubscriptionManager
import com.idea3d.juegoparaparejas.databinding.ActivityDocePreguntasBinding

class docePreguntas : AppCompatActivity() {
    private lateinit var binding: ActivityDocePreguntasBinding
    private var interstitial: InterstitialAd? = null
    private lateinit var subscriptionManager: SubscriptionManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDocePreguntasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        subscriptionManager = SubscriptionManager(this)
        subscriptionManager.setupBillingClient()
        
        initIntAds()
        initLoadAds()
        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val prueba = intent.getIntExtra("prueba", 0)
        val respuestas: ArrayList<Int?> = arrayListOf()
        val pack = QuestionBank.getPack(prueba)
        var currentIndex = 0
        fun renderQuestion() {
            val item = pack.questionAt(currentIndex)
            binding.preguntaView.text = getString(item.questionRes)
            binding.respuestaUno.text = getString(item.answerRes[0])
            binding.respuestaDos.text = getString(item.answerRes[1])
            binding.respuestaTres.text = getString(item.answerRes[2])
            binding.respuestaCuatro.text = getString(item.answerRes[3])
            binding.contView.text = getString(R.string.question_counter, currentIndex + 1)
        }
        fun goToNextScreen() {
            val intent = Intent(this, jugadorDos::class.java)
            intent.putExtra("jugador1", jug1)
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            intent.putIntegerArrayListExtra("respuestas", respuestas)
            startActivity(intent)
        }
        fun finishRound() {
            showAds()
            interstitial?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    goToNextScreen()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    goToNextScreen()
                }
            }
            if (interstitial == null) {
                goToNextScreen()
            }
        }
        fun answerSelected(answer: Int) {
            respuestas.add(answer)
            if (currentIndex == pack.questions.lastIndex) {
                finishRound()
            } else {
                currentIndex++
                renderQuestion()
            }
        }
        binding.respuestaUno.setOnClickListener { answerSelected(1) }
        binding.respuestaDos.setOnClickListener { answerSelected(2) }
        binding.respuestaTres.setOnClickListener { answerSelected(3) }
        binding.respuestaCuatro.setOnClickListener { answerSelected(4) }
        renderQuestion()
    }
    
    private fun initLoadAds() {
        if (!subscriptionManager.isPremiumUser()) {
            val adRequest = AdRequest.Builder().build()
            binding.banner.loadAd(adRequest)
        } else {
            binding.banner.visibility = View.GONE
        }
    }
    
    private fun initIntAds() {
        if (!subscriptionManager.isPremiumUser()) {
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                this,
                "ca-app-pub-3940256099942544/1033173712", // Test interstitial
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        interstitial = interstitialAd
                    }
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        interstitial = null
                    }
                }
            )
        }
    }
    
    private fun showAds() {
        interstitial?.show(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        subscriptionManager.disconnect()
    }
}
