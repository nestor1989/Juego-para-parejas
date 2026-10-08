package com.idea3d.juegoparaparejas.data

import android.content.Context
import androidx.core.content.edit

/** Preferencias locales. Todo lo que no requiere sincronizarse entre dispositivos. */
class Prefs(context: Context) {

    private val sp = context.getSharedPreferences("jpp_v4", Context.MODE_PRIVATE)

    var player1: String
        get() = sp.getString("player1", "").orEmpty()
        set(value) = sp.edit { putString("player1", value) }

    var player2: String
        get() = sp.getString("player2", "").orEmpty()
        set(value) = sp.edit { putString("player2", value) }

    /** Rondas terminadas (llegaron a la pantalla de resultado). */
    var roundsCompleted: Int
        get() = sp.getInt("rounds_completed", 0)
        set(value) = sp.edit { putInt("rounds_completed", value) }

    var roundsSinceInterstitial: Int
        get() = sp.getInt("rounds_since_interstitial", 0)
        set(value) = sp.edit { putInt("rounds_since_interstitial", value) }

    var lastInterstitialAt: Long
        get() = sp.getLong("last_interstitial_at", 0L)
        set(value) = sp.edit { putLong("last_interstitial_at", value) }

    /** Último estado premium conocido, para no mostrar anuncios mientras conecta Billing. */
    var premiumCached: Boolean
        get() = sp.getBoolean("premium_cached", false)
        set(value) = sp.edit { putBoolean("premium_cached", value) }

    var adultConfirmed: Boolean
        get() = sp.getBoolean("adult_confirmed", false)
        set(value) = sp.edit { putBoolean("adult_confirmed", value) }

    /** versionCode en el que ya se pidió reseña (se pide una vez por versión como máximo). */
    var reviewAskedVersion: Int
        get() = sp.getInt("review_asked_version", 0)
        set(value) = sp.edit { putInt("review_asked_version", value) }

    var referrerChecked: Boolean
        get() = sp.getBoolean("referrer_checked", false)
        set(value) = sp.edit { putBoolean("referrer_checked", value) }
}
