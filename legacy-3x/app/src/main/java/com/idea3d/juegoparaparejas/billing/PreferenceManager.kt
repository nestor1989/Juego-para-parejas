package com.idea3d.juegoparaparejas.billing

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val preferences: SharedPreferences =
        context.getSharedPreferences("subscription", Context.MODE_PRIVATE)

    companion object {
        private const val IS_PREMIUM = "is_premium"
        private const val SUBSCRIPTION_EXPIRY = "subscription_expiry"
        private const val LAST_CHECK = "last_check"
        private const val EXTERNAL_USER_ID = "external_user_id"
        private const val LAST_SYNC = "last_sync"
    }

    fun setIsPremium(isPremium: Boolean) {
        preferences.edit().putBoolean(IS_PREMIUM, isPremium).apply()
    }

    fun getIsPremium(): Boolean {
        return preferences.getBoolean(IS_PREMIUM, false)
    }

    fun setSubscriptionExpiry(expiryDate: Long) {
        preferences.edit().putLong(SUBSCRIPTION_EXPIRY, expiryDate).apply()
    }

    fun getSubscriptionExpiry(): Long {
        return preferences.getLong(SUBSCRIPTION_EXPIRY, 0)
    }

    fun setLastCheck(timestamp: Long) {
        preferences.edit().putLong(LAST_CHECK, timestamp).apply()
    }

    fun getLastCheck(): Long {
        return preferences.getLong(LAST_CHECK, 0)
    }

    fun setExternalUserId(userId: String) {
        preferences.edit().putString(EXTERNAL_USER_ID, userId).apply()
    }

    fun getExternalUserId(): String {
        return preferences.getString(EXTERNAL_USER_ID, "") ?: ""
    }

    fun setLastSync(timestamp: Long) {
        preferences.edit().putLong(LAST_SYNC, timestamp).apply()
    }

    fun getLastSync(): Long {
        return preferences.getLong(LAST_SYNC, 0)
    }

    fun isSubscriptionExpired(): Boolean {
        val expiryDate = getSubscriptionExpiry()
        return expiryDate > 0 && System.currentTimeMillis() > expiryDate
    }

    fun clear() {
        preferences.edit().clear().apply()
    }
}

