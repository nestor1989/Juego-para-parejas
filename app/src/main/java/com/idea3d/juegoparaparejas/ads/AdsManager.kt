package com.idea3d.juegoparaparejas.ads

import android.content.Context
import android.util.Log
import com.idea3d.juegoparaparejas.billing.PreferenceManager

enum class AdType {
    BANNER,
    INTERSTITIAL_BETWEEN_ROUNDS,
    INTERSTITIAL_AFTER_RESULT,
    REWARDED
}

data class AdEvent(
    val type: AdType,
    val adUnit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String // "impression", "click", "failed"
)

class AdsManager(private val context: Context) {
    private val prefManager = PreferenceManager(context)
    private val adEvents = mutableListOf<AdEvent>()
    private val lastAdShowTime = mutableMapOf<AdType, Long>()

    companion object {
        private const val TAG = "AdsManager"
        
        // Frequency caps (milliseconds)
        private const val INTERSTITIAL_MIN_INTERVAL = 30_000L // 30s between interstitials
        private const val BANNER_MIN_INTERVAL = 5_000L // 5s between banner refreshes
        
        // Test ad units (funcionan sin configuración AdMob)
        // Estos son los ad units de test de Google que siempre devuelven ads
        const val INTERSTITIAL_BETWEEN_ROUNDS = "ca-app-pub-3940256099942544/1033173712" // Test interstitial
        const val INTERSTITIAL_AFTER_RESULT = "ca-app-pub-3940256099942544/1033173712" // Test interstitial
        const val BANNER_DEFAULT = "ca-app-pub-3940256099942544/6300978111" // Test banner
    }

    /**
     * Determina si se debe mostrar un anuncio basado en:
     * - Estado premium del usuario
     * - Frecuencia cap
     * - Contexto
     */
    fun shouldShowAd(adType: AdType): Boolean {
        // 1. Check premium status
        if (prefManager.getIsPremium()) {
            Log.d(TAG, "Skipping ad - user is premium")
            return false
        }

        // 2. Check frequency cap
        val lastShown = lastAdShowTime[adType] ?: 0L
        val minInterval = when (adType) {
            AdType.BANNER -> BANNER_MIN_INTERVAL
            AdType.INTERSTITIAL_BETWEEN_ROUNDS -> INTERSTITIAL_MIN_INTERVAL
            AdType.INTERSTITIAL_AFTER_RESULT -> INTERSTITIAL_MIN_INTERVAL
            AdType.REWARDED -> 0L // No frequency cap for rewarded
        }

        val timeSinceLastAd = System.currentTimeMillis() - lastShown
        if (timeSinceLastAd < minInterval) {
            Log.d(TAG, "Skipping $adType - frequency cap active (${timeSinceLastAd}ms < ${minInterval}ms)")
            return false
        }

        return true
    }

    /**
     * Registra que se mostró un anuncio
     */
    fun recordAdImpression(adType: AdType, adUnit: String) {
        lastAdShowTime[adType] = System.currentTimeMillis()

        val event = AdEvent(
            type = adType,
            adUnit = adUnit,
            eventType = "impression"
        )
        adEvents.add(event)

        Log.d(TAG, "Ad impression recorded: $adType")
    }

    /**
     * Registra que el usuario clickeó un anuncio
     */
    fun recordAdClick(adType: AdType, adUnit: String) {
        val event = AdEvent(
            type = adType,
            adUnit = adUnit,
            eventType = "click"
        )
        adEvents.add(event)

        Log.d(TAG, "Ad click recorded: $adType")
    }

    /**
     * Registra que un anuncio falló
     */
    fun recordAdFailure(adType: AdType, adUnit: String, errorMessage: String = "") {
        val event = AdEvent(
            type = adType,
            adUnit = adUnit,
            eventType = "failed"
        )
        adEvents.add(event)

        Log.w(TAG, "Ad failed: $adType - $errorMessage")
    }

    /**
     * Obtiene estadísticas de ads mostrados
     */
    fun getStats(): AdStats {
        val impressions = adEvents.count { it.eventType == "impression" }
        val clicks = adEvents.count { it.eventType == "click" }
        val failures = adEvents.count { it.eventType == "failed" }
        val ctr = if (impressions > 0) (clicks.toDouble() / impressions * 100).toInt() else 0

        return AdStats(
            totalImpressions = impressions,
            totalClicks = clicks,
            totalFailures = failures,
            ctr = ctr
        )
    }

    /**
     * Limpia eventos antiguos (más de 24h)
     */
    fun cleanup() {
        val oneDayAgo = System.currentTimeMillis() - 86_400_000L
        adEvents.removeAll { it.timestamp < oneDayAgo }
    }
}

data class AdStats(
    val totalImpressions: Int,
    val totalClicks: Int,
    val totalFailures: Int,
    val ctr: Int // Click through rate percentage
)

