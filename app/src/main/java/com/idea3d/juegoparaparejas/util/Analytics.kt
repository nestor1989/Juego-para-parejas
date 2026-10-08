package com.idea3d.juegoparaparejas.util

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Eventos que alimentan las métricas del plan ASO:
 * round_start, round_complete, challenge_sent, challenge_opened, result_shared,
 * paywall_view, purchase_complete, rewarded_unlock, review_prompt.
 */
class Analytics(context: Context) {

    private val firebase = FirebaseAnalytics.getInstance(context)

    fun log(event: String, vararg params: Pair<String, Any?>) {
        val bundle = Bundle()
        params.forEach { (key, value) ->
            when (value) {
                is Int -> bundle.putLong(key, value.toLong())
                is Long -> bundle.putLong(key, value)
                is Boolean -> bundle.putString(key, value.toString())
                is String -> bundle.putString(key, value.take(100))
                null -> Unit
                else -> bundle.putString(key, value.toString().take(100))
            }
        }
        firebase.logEvent(event, bundle)
    }
}
