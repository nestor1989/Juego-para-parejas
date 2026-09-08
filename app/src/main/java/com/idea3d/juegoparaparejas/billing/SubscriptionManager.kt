package com.idea3d.juegoparaparejas.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.android.billingclient.api.*
import com.idea3d.juegoparaparejas.backend.EntitlementClient

class SubscriptionManager(private val context: Context) {
    private lateinit var billingClient: BillingClient
    private val preferenceManager = PreferenceManager(context)
    private val entitlementClient = EntitlementClient(context)

    /**
     * Se invoca cuando se resuelve el estado premium real contra Google Play.
     * Útil para que las Activities recarguen los anuncios una vez confirmado
     * si el usuario es (o no) premium. Devuelve el valor final de isPremium.
     */
    var onPremiumStatusResolved: ((Boolean) -> Unit)? = null

    companion object {
        private const val SUBSCRIPTION_SKU_MONTHLY = "premium_monthly"
        private const val SUBSCRIPTION_SKU_YEARLY = "premium_yearly"

        private const val TAG = "SubscriptionManager"
    }

    fun setupBillingClient(onReady: (Boolean) -> Unit = {}) {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                handlePurchasesUpdated(billingResult, purchases)
            }
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing client connected successfully")
                    Log.d(TAG, "✓ Billing service is ready - app is properly configured for Google Play Billing")
                    verifySubscriptionStatus()
                    onReady(true)
                } else {
                    val errorMessage = when (billingResult.responseCode) {
                        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
                            "Billing service unavailable - app may not be configured in Google Play Console"
                        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
                            "Billing not available - ensure app is in internal testing track or released"
                        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
                            "Developer error - verify app configuration in Google Play Console"
                        else -> billingResult.debugMessage
                    }
                    Log.e(TAG, "✗ Billing setup failed: $errorMessage (Code: ${billingResult.responseCode})")
                    Log.e(TAG, "  To fix this, you must:")
                    Log.e(TAG, "  1. Register your certificate SHA1 in Google Play Console")
                    Log.e(TAG, "  2. Create subscription SKUs: premium_monthly, premium_yearly")
                    Log.e(TAG, "  3. Upload app to Internal testing track")
                    onReady(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected - attempting to reconnect")
            }
        })
    }

    fun isPremiumUser(): Boolean {
        if (preferenceManager.isSubscriptionExpired()) {
            preferenceManager.setIsPremium(false)
            return false
        }
        return preferenceManager.getIsPremium()
    }

    fun launchSubscriptionFlow(activity: Activity, onResult: (Boolean) -> Unit = {}) {
        try {
            Log.d(TAG, "Launching subscription flow")
            
            // Usar Google Play real - descomentar cuando SKUs estén configurados
            queryAndLaunchBillingFlow(activity, SUBSCRIPTION_SKU_MONTHLY, onResult)

        } catch (e: Exception) {
            Log.e(TAG, "Error launching billing flow: ${e.message}")
            onResult(false)
        }
    }
    
    private fun queryAndLaunchBillingFlow(
        activity: Activity, 
        skuId: String, 
        onResult: (Boolean) -> Unit
    ) {
        try {
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(
                    listOf(
                        QueryProductDetailsParams.Product.newBuilder()
                            .setProductId(skuId)
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build()
                    )
                )
                .build()
            
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                when {
                    billingResult.responseCode == BillingClient.BillingResponseCode.OK &&
                    productDetailsList != null && productDetailsList.isNotEmpty() -> {
                        launchBillingFlowWithDetails(activity, productDetailsList[0], onResult)
                    }
                    else -> {
                        // NO usar SKU de test ni simular compras en producción.
                        // El SKU de test (android.test.purchased) provoca el diálogo
                        // "This version of the application is not configured for billing".
                        Log.e(TAG, "Product details not found for '$skuId': ${billingResult.debugMessage} (code: ${billingResult.responseCode})")
                        onResult(false)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying product details: ${e.message}")
            e.printStackTrace()
            onResult(false)
        }
    }
    
    private fun launchBillingFlowWithDetails(
        activity: Activity,
        productDetails: ProductDetails,
        onResult: (Boolean) -> Unit
    ) {
        try {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            
            if (offerToken != null) {
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
                
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()
                
                val result = billingClient.launchBillingFlow(activity, billingFlowParams)
                Log.d(TAG, "Billing flow launched: ${result.responseCode}")
                onResult(result.responseCode == BillingClient.BillingResponseCode.OK)
            } else {
                Log.e(TAG, "No offer token available")
                onResult(false)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching billing flow: ${e.message}")
            e.printStackTrace()
            onResult(false)
        }
    }
    
    private fun simulateSuccessfulPurchase(onResult: (Boolean) -> Unit) {
        // Eliminado: la simulación marcaba premium=true de forma permanente,
        // ocultando los anuncios para siempre. En producción el estado premium
        // solo debe provenir de compras reales de Google Play.
        onResult(false)
    }

    fun verifySubscriptionStatus() {
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val activeSubs = purchases.filter { isSubscriptionProduct(it.products) }

                    if (activeSubs.isNotEmpty()) {
                        // Hay suscripciones reales: verificar cada una con el backend
                        for (purchase in activeSubs) {
                            verifyWithBackend(purchase)
                        }
                        preferenceManager.setIsPremium(true)
                        notifyPremiumResolved(true)
                    } else {
                        // No hay ninguna suscripción real activa en Google Play.
                        // Resetear cualquier estado premium local obsoleto (por ej.
                        // el que dejó la antigua simulación) para que los ads vuelvan.
                        preferenceManager.setIsPremium(false)
                        notifyPremiumResolved(false)
                        entitlementClient.getCurrentEntitlement { isPremium ->
                            Log.d(TAG, "No active Google Play subscription. Backend entitlement: $isPremium")
                            preferenceManager.setIsPremium(isPremium)
                            notifyPremiumResolved(isPremium)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying subscription: ${e.message}")
        }
    }

    private fun notifyPremiumResolved(isPremium: Boolean) {
        // Asegurar callback en el hilo principal para tocar la UI (recargar ads)
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            onPremiumStatusResolved?.invoke(isPremium)
        }
    }

    private fun verifyWithBackend(purchase: Purchase) {
        val productId = purchase.products.firstOrNull() ?: return
        val token = purchase.purchaseToken

        Log.d(TAG, "Verifying purchase - product: $productId, token: ${token.take(20)}...")

        entitlementClient.verifyPurchase(token, productId) { success, isPremium ->
            if (success) {
                Log.d(TAG, "Backend verification successful: isPremium=$isPremium")
                preferenceManager.setIsPremium(isPremium)
            } else {
                Log.d(TAG, "Backend verification failed, falling back to local state")
            }
        }
    }

    private fun handlePurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (isSubscriptionProduct(purchase.products)) {
                    handleSubscriptionPurchase(purchase)
                }
            }
        } else {
            Log.d(TAG, "Purchase failed: ${billingResult.debugMessage}")
        }
    }

    private fun handleSubscriptionPurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) {
            Log.d(TAG, "Purchase already acknowledged, verifying with backend...")
            verifyWithBackend(purchase)
        } else {
            acknowledgePurchase(purchase)
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { billingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase acknowledged successfully")
                verifyWithBackend(purchase)
            }
        }
    }

    private fun isSubscriptionProduct(products: List<String>): Boolean {
        return products.any {
            it == SUBSCRIPTION_SKU_MONTHLY || it == SUBSCRIPTION_SKU_YEARLY
        }
    }

    fun disconnect() {
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }

    /**
     * Debug helper to print billing configuration status
     * Call this from your activity to see billing setup status in logcat
     */
    fun printBillingStatus(tag: String = "SubscriptionManager") {
        Log.d(tag, "=== Google Play Billing Configuration Status ===")
        Log.d(tag, "Primary Subscription SKUs:")
        Log.d(tag, "  - Monthly: $SUBSCRIPTION_SKU_MONTHLY")
        Log.d(tag, "  - Yearly: $SUBSCRIPTION_SKU_YEARLY")
        Log.d(tag, "Current Premium Status: ${isPremiumUser()}")
        Log.d(tag, "App Package: com.idea3d.juegoparaparejas")
        Log.d(tag, "================================================")
    }
}

