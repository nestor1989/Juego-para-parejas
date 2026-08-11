package com.idea3d.juegoparaparejas.backend

import android.content.Context
import android.util.Log
import com.idea3d.juegoparaparejas.billing.PreferenceManager
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class EntitlementClient(private val context: Context) {
    
    companion object {
        private const val BASE_URL = "https://freemiumapi-production.up.railway.app"
        private const val APP_KEY = "com.idea3d.juegoparaparejas"
        private const val PACKAGE_NAME = "com.idea3d.juegoparaparejas"
        private const val TAG = "EntitlementClient"
    }
    
    private val prefManager = PreferenceManager(context)
    private val api: EntitlementApi
    
    // Dedupe: track tokens in-flight
    private val inFlightTokens = mutableMapOf<String, Long>()
    private val dedupeThresholdMs = 60_000L // 60 second cooldown
    
    init {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor { message ->
                Log.d(TAG, message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(EntitlementApi::class.java)
    }
    
    fun verifyPurchase(
        purchaseToken: String,
        productId: String,
        callback: (success: Boolean, isPremium: Boolean) -> Unit
    ) {
        // Dedupe check
        val lastAttempt = inFlightTokens[purchaseToken] ?: 0
        val now = System.currentTimeMillis()
        if (now - lastAttempt < dedupeThresholdMs) {
            Log.d(TAG, "Skipping verify - token in cooldown: $purchaseToken")
            callback(false, false)
            return
        }
        
        inFlightTokens[purchaseToken] = now
        
        val externalUserId = getOrCreateExternalUserId()
        val request = VerifyPurchaseRequest(
            appKey = APP_KEY,
            externalUserId = externalUserId,
            packageName = PACKAGE_NAME,
            purchaseToken = purchaseToken,
            productId = productId
        )
        
        try {
            val response = api.verifyPurchase(APP_KEY, request).execute()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val isPremium = body.entitlementStatus == "PREMIUM"
                    Log.d(TAG, "Verify success: isPremium=$isPremium, product=$productId")
                    
                    prefManager.setIsPremium(isPremium)
                    prefManager.setLastSync(System.currentTimeMillis())
                    callback(true, isPremium)
                    return
                }
            } else {
                Log.e(TAG, "Verify failed: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Verify error: ${e.message}")
        }
        
        callback(false, false)
    }
    
    fun getCurrentEntitlement(callback: (isPremium: Boolean) -> Unit) {
        val externalUserId = getOrCreateExternalUserId()
        
        try {
            val response = api.getCurrentEntitlement(
                APP_KEY,
                APP_KEY,
                externalUserId
            ).execute()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val isPremium = body.entitlementStatus == "PREMIUM"
                    Log.d(TAG, "Current entitlement: isPremium=$isPremium")
                    
                    prefManager.setIsPremium(isPremium)
                    prefManager.setLastSync(System.currentTimeMillis())
                    callback(isPremium)
                    return
                }
            } else {
                Log.e(TAG, "Current entitlement failed: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Current entitlement error: ${e.message}")
        }
        
        // Fallback to local cache
        callback(prefManager.getIsPremium())
    }
    
    private fun getOrCreateExternalUserId(): String {
        var userId = prefManager.getExternalUserId()
        if (userId.isEmpty()) {
            // Generate stable UUID based on device
            userId = generateStableUserId()
            prefManager.setExternalUserId(userId)
        }
        return userId
    }
    
    private fun generateStableUserId(): String {
        // Could use Android ID or UUID based on device info
        return android.os.Build.DEVICE + "_" + 
               (System.currentTimeMillis() / 1000L).toString()
    }
    
    fun clearDedupeCache() {
        inFlightTokens.clear()
    }
}

