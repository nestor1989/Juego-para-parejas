package com.idea3d.juegoparaparejas
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.idea3d.juegoparaparejas.databinding.ActivityDoceRespuestasBinding
import java.util.Timer
import kotlin.concurrent.schedule
class doceRespuestas : AppCompatActivity() {
    private lateinit var binding: ActivityDoceRespuestasBinding
    private var interstitial: InterstitialAd? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDoceRespuestasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initIntAds()
        initLoadAds()
        var currentIndex = 0
        val respuestasDos: ArrayList<Int?> = arrayListOf()
        var comparaResp = 0
        var promedio = 0.0
        val jug1 = intent.getStringExtra("jugador1")
        val jug2 = intent.getStringExtra("jugador2")
        val prueba = intent.getIntExtra("prueba", 0)
        val respuestas = intent.getIntegerArrayListExtra("respuestas") ?: arrayListOf()
        val pack = QuestionBank.getPack(prueba)
        fun resetButtons() {
            val pink = ContextCompat.getColor(this, R.color.rosa)
            binding.respuestaUno.background.setTint(pink)
            binding.respuestaDos.background.setTint(pink)
            binding.respuestaTres.background.setTint(pink)
            binding.respuestaCuatro.background.setTint(pink)
        }
        fun renderQuestion() {
            resetButtons()
            val item = pack.questionAt(currentIndex)
            binding.preguntaView.text = getString(item.questionRes)
            binding.respuestaUno.text = getString(item.answerRes[0])
            binding.respuestaDos.text = getString(item.answerRes[1])
            binding.respuestaTres.text = getString(item.answerRes[2])
            binding.respuestaCuatro.text = getString(item.answerRes[3])
            binding.contView.text = getString(R.string.question_counter, currentIndex + 1)
        }
        fun goToResults() {
            promedio = comparaResp.toDouble() / 12 * 100
            val intent = Intent(this, Respuestas::class.java)
            intent.putExtra("jugador1", jug1)
            intent.putExtra("jugador2", jug2)
            intent.putExtra("prueba", prueba)
            intent.putExtra("promedio", promedio)
            startActivity(intent)
        }
        fun finishRound() {
            showAds()
            interstitial?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    goToResults()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    goToResults()
                }
            }
            if (interstitial == null) {
                goToResults()
            }
        }
        fun answerSelected(answer: Int) {
            respuestasDos.add(answer)
            val expected = respuestas[currentIndex] ?: 0
            val selectedTint = ContextCompat.getColor(this, R.color.verde)
            val wrongTint = ContextCompat.getColor(this, R.color.rojo)
            when (expected) {
                1 -> binding.respuestaUno.background.setTint(selectedTint)
                2 -> binding.respuestaDos.background.setTint(selectedTint)
                3 -> binding.respuestaTres.background.setTint(selectedTint)
                4 -> binding.respuestaCuatro.background.setTint(selectedTint)
            }
            if (expected == answer) {
                comparaResp++
            } else {
                when (answer) {
                    1 -> binding.respuestaUno.background.setTint(wrongTint)
                    2 -> binding.respuestaDos.background.setTint(wrongTint)
                    3 -> binding.respuestaTres.background.setTint(wrongTint)
                    4 -> binding.respuestaCuatro.background.setTint(wrongTint)
                }
            }
            if (currentIndex == pack.questions.lastIndex) {
                finishRound()
            } else {
                currentIndex++
                Timer("SettingUp", false).schedule(750) {
                    runOnUiThread { renderQuestion() }
                }
            }
        }
        binding.respuestaUno.setOnClickListener { answerSelected(1) }
        binding.respuestaDos.setOnClickListener { answerSelected(2) }
        binding.respuestaTres.setOnClickListener { answerSelected(3) }
        binding.respuestaCuatro.setOnClickListener { answerSelected(4) }
        renderQuestion()
    }
    private fun initLoadAds() {
        val adRequest = AdRequest.Builder().build()
        binding.banner.loadAd(adRequest)
    }
    private fun initIntAds() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-4930505659937183/6385055420",
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
    private fun showAds() {
        interstitial?.show(this)
    }
}
