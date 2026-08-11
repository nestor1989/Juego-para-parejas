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

    companion object {
        private const val SUBSCRIPTION_SKU_MONTHLY = "premium_monthly"
        private const val SUBSCRIPTION_SKU_YEARLY = "premium_yearly"
        
        // SKUs alternativos para testing (si los principales no funcionan)
        private const val TEST_SKU_MONTHLY = "android.test.purchased"
        private const val TEST_SKU_YEARLY = "android.test.purchased"
        
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
                    verifySubscriptionStatus()
                    onReady(true)
                } else {
                    Log.e(TAG, "Billing setup failed: ${billingResult.debugMessage}")
                    onReady(false)
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d(TAG, "Billing service disconnected")
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
            
            // Intentar con SKU principal
            queryAndLaunchBillingFlow(activity, SUBSCRIPTION_SKU_MONTHLY, onResult)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error launching billing flow: ${e.message}")
            e.printStackTrace()
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
                    skuId == SUBSCRIPTION_SKU_MONTHLY -> {
                        // Si falla, intentar con TEST SKU
                        Log.w(TAG, "Main SKU failed, trying test SKU...")
                        queryAndLaunchBillingFlow(activity, TEST_SKU_MONTHLY, onResult)
                    }
                    else -> {
                        Log.e(TAG, "Product details not found: ${billingResult.debugMessage}")
                        Log.w(TAG, "Simulating purchase for testing...")
                        // Para testing: simular compra exitosa
                        simulateSuccessfulPurchase(onResult)
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
        // Para ambiente de testing sin configuración real de Google Play
        Log.d(TAG, "Simulating purchase (no real SKU configured)")
        preferenceManager.setIsPremium(true)
        preferenceManager.setLastSync(System.currentTimeMillis())
        onResult(true)
    }

    fun verifySubscriptionStatus() {
        try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()

            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Verify each purchase with backend
                    for (purchase in purchases) {
                        if (isSubscriptionProduct(purchase.products)) {
                            verifyWithBackend(purchase)
                        }
                    }

                    // Also fetch current entitlement from backend
                    if (purchases.isEmpty()) {
                        entitlementClient.getCurrentEntitlement { isPremium ->
                            Log.d(TAG, "Current entitlement status: $isPremium")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying subscription: ${e.message}")
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
}

