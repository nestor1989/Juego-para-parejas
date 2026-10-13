package com.idea3d.juegoparaparejas.monetization

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.idea3d.juegoparaparejas.BuildConfig
import com.idea3d.juegoparaparejas.data.Prefs
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Reglas de anuncios de la 4.0:
 *  - Sin banners.
 *  - Rewarded opcional: desbloquea un pack premium para una partida.
 *  - Interstitial solo al salir de un resultado, como máximo cada 2 partidas,
 *    con 4 minutos de separación y nunca antes de la segunda partida.
 *  - Nada de anuncios para premium.
 *
 * Consentimiento (UMP): ningún anuncio se pide antes de que canRequestAds() sea verdadero.
 * El flujo corre también para premium, y el acceso "Opciones de privacidad" sigue disponible
 * aunque no haya anuncios.
 */
class AdsManager(private val context: Context, private val prefs: Prefs) {

    companion object {
        private const val TAG = "AdsManager"
        private const val MIN_ROUNDS_BEFORE_FIRST = 2
        private const val ROUNDS_BETWEEN = 2
        private const val MIN_INTERVAL_MS = 4 * 60 * 1000L

        // IDs de prueba de Google en debug; reales en release.
        private val REWARDED_ID = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917"
        else "ca-app-pub-4930505659937183/3093275577"
        private val INTERSTITIAL_ID = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712"
        else "ca-app-pub-4930505659937183/6385055420"
    }

    /** Lo setea App con el estado de Billing. */
    var isPremium: () -> Boolean = { false }

    private val consent: ConsentInformation = UserMessagingPlatform.getConsentInformation(context)
    private val initialized = AtomicBoolean(false)
    private val debugResetDone = AtomicBoolean(false)

    private val _privacyOptionsRequired = MutableStateFlow(false)

    /**
     * Si hay que mostrar el acceso "Opciones de privacidad" (GDPR en el EEE, Reino Unido y Suiza,
     * o estados regulados de EE. UU.). Se actualiza después de cada consulta de consentimiento.
     */
    val privacyOptionsRequired: StateFlow<Boolean> = _privacyOptionsRequired.asStateFlow()

    private var rewarded: RewardedAd? = null
    private var interstitial: InterstitialAd? = null
    private var loadingRewarded = false
    private var loadingInterstitial = false

    /**
     * Se llama en cada inicio (MainActivity.onCreate):
     *  1. Actualiza el estado de consentimiento.
     *  2. Muestra el formulario si hace falta (GDPR).
     *  3. Inicializa AdMob solo si canRequestAds().
     * El mensaje de estados de EE. UU. no aparece acá: se abre desde "Opciones de privacidad".
     */
    fun gatherConsent(activity: Activity) {
        if (BuildConfig.UMP_RESET_ON_LAUNCH && debugResetDone.compareAndSet(false, true)) {
            consent.reset()
        }
        consent.requestConsentInfoUpdate(
            activity,
            consentParameters(activity),
            {
                updatePrivacyOptions()
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    formError?.let { report("consent_form", it) }
                    updatePrivacyOptions()
                    if (consent.canRequestAds()) initialize()
                }
            },
            { requestError ->
                report("consent_update", requestError)
                if (consent.canRequestAds()) initialize()
            },
        )
        // Consentimiento de una sesión anterior: se puede inicializar ya, en paralelo.
        if (consent.canRequestAds()) initialize()
    }

    /** Abre el formulario de opciones de privacidad de UMP (cambiar o retirar el consentimiento). */
    fun showPrivacyOptions(activity: Activity, onError: () -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { formError ->
            if (formError != null) {
                report("privacy_options", formError)
                onError()
            }
            updatePrivacyOptions()
            if (consent.canRequestAds()) initialize()
        }
    }

    private fun updatePrivacyOptions() {
        _privacyOptionsRequired.value = consent.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
    }

    /** En debug se puede forzar la región y el dispositivo de prueba desde local.properties. */
    private fun consentParameters(context: Context): ConsentRequestParameters {
        val builder = ConsentRequestParameters.Builder()
        if (BuildConfig.DEBUG) {
            val geography = when (BuildConfig.UMP_DEBUG_GEOGRAPHY.uppercase()) {
                "EEA" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA
                "US_STATE" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_REGULATED_US_STATE
                "OTHER" -> ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_OTHER
                else -> null
            }
            val testDevice = BuildConfig.UMP_TEST_DEVICE_ID
            if (geography != null || testDevice.isNotBlank()) {
                val debug = ConsentDebugSettings.Builder(context)
                if (geography != null) debug.setDebugGeography(geography)
                if (testDevice.isNotBlank()) debug.addTestDeviceHashedId(testDevice)
                builder.setConsentDebugSettings(debug.build())
            }
        }
        return builder.build()
    }

    /** Errores de UMP: siempre a Logcat; a Crashlytics solo los que no son de conexión. */
    private fun report(where: String, error: FormError) {
        Log.w(TAG, "UMP $where: ${error.errorCode} ${error.message}")
        val networkError = error.errorCode == FormError.ErrorCode.INTERNET_ERROR ||
            error.errorCode == FormError.ErrorCode.TIME_OUT
        if (!networkError) {
            FirebaseCrashlytics.getInstance()
                .recordException(IllegalStateException("UMP $where ${error.errorCode}: ${error.message}"))
        }
    }

    private fun initialize() {
        if (!initialized.compareAndSet(false, true)) return
        MobileAds.initialize(context) {}
        preload()
    }

    fun preload() {
        if (!initialized.get() || isPremium()) return
        loadRewarded()
        loadInterstitial()
    }

    private fun loadRewarded() {
        if (rewarded != null || loadingRewarded) return
        loadingRewarded = true
        RewardedAd.load(context, REWARDED_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewarded = ad
                loadingRewarded = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                rewarded = null
                loadingRewarded = false
            }
        })
    }

    private fun loadInterstitial() {
        if (interstitial != null || loadingInterstitial) return
        loadingInterstitial = true
        InterstitialAd.load(context, INTERSTITIAL_ID, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                loadingInterstitial = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                interstitial = null
                loadingInterstitial = false
            }
        })
    }

    /** Muestra un rewarded. [onReward] solo se llama si el usuario lo vio completo. */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewarded
        if (ad == null) {
            preload()
            onUnavailable()
            return
        }
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                preload()
                if (earned) onReward()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewarded = null
                preload()
                onUnavailable()
            }
        }
        ad.show(activity) { earned = true }
    }

    /** Se llama cada vez que termina una ronda (pantalla de resultado). */
    fun onRoundCompleted() {
        prefs.roundsSinceInterstitial = prefs.roundsSinceInterstitial + 1
    }

    /** Muestra un interstitial si las reglas lo permiten; siempre termina llamando a [then]. */
    fun maybeShowInterstitial(activity: Activity, then: () -> Unit) {
        val ad = interstitial
        val now = System.currentTimeMillis()
        val allowed = !isPremium() &&
            ad != null &&
            prefs.roundsCompleted >= MIN_ROUNDS_BEFORE_FIRST &&
            prefs.roundsSinceInterstitial >= ROUNDS_BETWEEN &&
            now - prefs.lastInterstitialAt >= MIN_INTERVAL_MS
        if (!allowed || ad == null) {
            then()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                preload()
                then()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                interstitial = null
                preload()
                then()
            }
        }
        prefs.lastInterstitialAt = now
        prefs.roundsSinceInterstitial = 0
        ad.show(activity)
    }
}
