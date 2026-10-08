package com.idea3d.juegoparaparejas.monetization

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.idea3d.juegoparaparejas.data.Prefs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Premium de por vida (compra única, no consumible).
 * También reconoce las suscripciones de la 3.x (premium_monthly / premium_yearly)
 * para que quienes ya pagaron no pierdan nada.
 */
class BillingManager(context: Context, private val prefs: Prefs) : PurchasesUpdatedListener {

    companion object {
        const val PRODUCT_LIFETIME = "premium_lifetime"
        private val LEGACY_SUBSCRIPTIONS = setOf("premium_monthly", "premium_yearly")
        private const val TAG = "BillingManager"
    }

    sealed interface PurchaseEvent {
        data object Success : PurchaseEvent
        data object Failed : PurchaseEvent
    }

    private val main = Handler(Looper.getMainLooper())

    private val _isPremium = MutableStateFlow(prefs.premiumCached)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _price = MutableStateFlow<String?>(null)
    val price: StateFlow<String?> = _price.asStateFlow()

    /** Se emite tras un intento de compra iniciado desde la app. */
    private val _purchaseEvents = MutableStateFlow<PurchaseEvent?>(null)
    val purchaseEvents: StateFlow<PurchaseEvent?> = _purchaseEvents.asStateFlow()

    private var lifetimeDetails: ProductDetails? = null
    // Arranca desde el último estado conocido; si una consulta falla, se conserva.
    private var ownsLifetime = prefs.premiumCached
    private var ownsLegacySubscription = false

    private val client: BillingClient = BillingClient.newBuilder(context.applicationContext)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        if (client.isReady) {
            refresh()
            return
        }
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    refresh()
                } else {
                    Log.w(TAG, "Billing no disponible: ${result.responseCode} ${result.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                // enableAutoServiceReconnection() reconecta en la próxima llamada.
            }
        })
    }

    fun consumeEvent() {
        _purchaseEvents.value = null
    }

    /** Vuelve a consultar compras (al abrir la app y al volver a primer plano). */
    fun refresh() {
        if (!client.isReady) return
        queryLifetimeProduct()

        // Se publica el estado recién cuando respondieron las dos consultas,
        // para no mostrar anuncios un instante a quien tiene una suscripción vieja.
        var pending = 2
        fun onQueryDone() {
            pending--
            if (pending == 0) publishPremium()
        }

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()
        ) { result, purchases ->
            val ok = result.responseCode == BillingClient.BillingResponseCode.OK
            if (ok) purchases.forEach { acknowledgeIfNeeded(it) }
            val owned = ok && purchases.any { it.isActiveFor(setOf(PRODUCT_LIFETIME)) }
            main.post {
                if (ok) ownsLifetime = owned
                onQueryDone()
            }
        }

        client.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { result, purchases ->
            val ok = result.responseCode == BillingClient.BillingResponseCode.OK
            val owned = ok && purchases.any { it.isActiveFor(LEGACY_SUBSCRIPTIONS) }
            main.post {
                if (ok) ownsLegacySubscription = owned
                onQueryDone()
            }
        }
    }

    fun launchPurchase(activity: Activity) {
        val details = lifetimeDetails
        if (!client.isReady || details == null) {
            _purchaseEvents.value = PurchaseEvent.Failed
            start()
            return
        }
        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
        details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.offerToken?.let { token ->
            paramsBuilder.setOfferToken(token)
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsBuilder.build()))
            .build()
        val result = client.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _purchaseEvents.value = PurchaseEvent.Failed
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        when (result.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                val bought = purchases.orEmpty().filter { it.isActiveFor(setOf(PRODUCT_LIFETIME)) }
                bought.forEach { acknowledgeIfNeeded(it) }
                main.post {
                    if (bought.isNotEmpty()) {
                        ownsLifetime = true
                        publishPremium()
                        _purchaseEvents.value = PurchaseEvent.Success
                    }
                }
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> refresh()
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> main.post { _purchaseEvents.value = PurchaseEvent.Failed }
        }
    }

    private fun queryLifetimeProduct() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PRODUCT_LIFETIME)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        client.queryProductDetailsAsync(params) { result, productDetailsResult ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return@queryProductDetailsAsync
            val details = productDetailsResult.productDetailsList.firstOrNull() ?: return@queryProductDetailsAsync
            val formatted = details.oneTimePurchaseOfferDetailsList?.firstOrNull()?.formattedPrice
                ?: details.oneTimePurchaseOfferDetails?.formattedPrice
            main.post {
                lifetimeDetails = details
                _price.value = formatted
            }
        }
    }

    private fun acknowledgeIfNeeded(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED || purchase.isAcknowledged) return
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        client.acknowledgePurchase(params) { result ->
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                Log.w(TAG, "No se pudo reconocer la compra: ${result.debugMessage}")
            }
        }
    }

    private fun publishPremium() {
        val premium = ownsLifetime || ownsLegacySubscription
        prefs.premiumCached = premium
        _isPremium.value = premium
    }

    private fun Purchase.isActiveFor(productIds: Set<String>): Boolean =
        purchaseState == Purchase.PurchaseState.PURCHASED && products.any { it in productIds }
}
