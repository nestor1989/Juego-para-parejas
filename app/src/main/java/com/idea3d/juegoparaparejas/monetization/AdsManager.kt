package com.idea3d.juegoparaparejas.monetization

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
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
import com.idea3d.juegoparaparejas.util.Analytics
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Anuncios de la app:
 *  - Intersticial al salir de la pantalla de resultado (cualquier botón o Atrás), desde la segunda
 *    ronda de la instalación y con al menos 90 s entre uno y otro. La primera ronda nunca tiene
 *    anuncios: también es la de quien llega por un link de desafío.
 *  - Rewarded opcional: desbloquea un pack premium para una partida.
 *  - Sin banners. Nada de anuncios para premium.
 *  - Si un anuncio no carga, se reintenta con espera creciente (30 s, 1 min, 2 min… hasta 10 min)
 *    y el error queda en Analytics (`ad_load_failed`). Se vuelve a pedir al terminar cada ronda.
 *
 * Consentimiento (UMP): ningún anuncio se pide antes de que canRequestAds() sea verdadero.
 * El flujo corre también para premium, y el acceso "Opciones de privacidad" sigue disponible
 * aunque no haya anuncios.
 */
class AdsManager(
    private val context: Context,
    private val prefs: Prefs,
    private val analytics: Analytics,
) {

    companion object {
        private const val TAG = "AdsManager"

        /** Rondas sin anuncios al empezar a usar la app. */
        private const val FREE_ROUNDS = 1

        /** Separación mínima entre intersticiales. */
        private const val MIN_INTERVAL_MS = 90_000L

        /** Un anuncio cargado vence a la hora; se descarta un poco antes. */
        private const val AD_MAX_AGE_MS = 55 * 60_000L

        /** Reintentos de carga: 30 s, 1 min, 2 min… con tope de 10 min. */
        private const val RETRY_BASE_MS = 30_000L
        private const val RETRY_MAX_MS = 10 * 60_000L

        // IDs de prueba de Google en debug; reales en release
        // (AdMob › Couples Quiz: Long Distance › "rewarded_adults" e "Inters 12Resp").
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

    private val handler = Handler(Looper.getMainLooper())
    private var rewarded: RewardedAd? = null
    private var rewardedLoadedAt = 0L
    private var loadingRewarded = false
    private var rewardedFailures = 0
    private var interstitial: InterstitialAd? = null
    private var interstitialLoadedAt = 0L
    private var loadingInterstitial = false
    private var interstitialFailures = 0

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

    /** Deja listos un intersticial y un rewarded, si se pueden pedir anuncios y no es premium. */
    fun preload() {
        if (!initialized.get() || isPremium()) return
        loadRewarded()
        loadInterstitial()
    }

    private fun isFresh(ad: Any?, loadedAt: Long): Boolean =
        ad != null && SystemClock.elapsedRealtime() - loadedAt < AD_MAX_AGE_MS

    private fun loadRewarded() {
        if (isFresh(rewarded, rewardedLoadedAt) || loadingRewarded) return
        rewarded = null
        loadingRewarded = true
        RewardedAd.load(context, REWARDED_ID, AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) {
                rewarded = ad
                rewardedLoadedAt = SystemClock.elapsedRealtime()
                rewardedFailures = 0
                loadingRewarded = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loadingRewarded = false
                rewardedFailures++
                onLoadFailed("rewarded", error, rewardedFailures) { loadRewarded() }
            }
        })
    }

    private fun loadInterstitial() {
        if (isFresh(interstitial, interstitialLoadedAt) || loadingInterstitial) return
        interstitial = null
        loadingInterstitial = true
        InterstitialAd.load(context, INTERSTITIAL_ID, AdRequest.Builder().build(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) {
                interstitial = ad
                interstitialLoadedAt = SystemClock.elapsedRealtime()
                interstitialFailures = 0
                loadingInterstitial = false
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                loadingInterstitial = false
                interstitialFailures++
                onLoadFailed("interstitial", error, interstitialFailures) { loadInterstitial() }
            }
        })
    }

    /** Registra por qué no cargó (Logcat + Analytics) y reintenta con espera creciente. */
    private fun onLoadFailed(format: String, error: LoadAdError, attempt: Int, retry: () -> Unit) {
        Log.w(TAG, "No cargó $format (intento $attempt): ${error.code} ${error.message}")
        analytics.log("ad_load_failed", "format" to format, "code" to error.code, "attempt" to attempt)
        val delayMs = (RETRY_BASE_MS shl (attempt - 1).coerceIn(0, 5)).coerceAtMost(RETRY_MAX_MS)
        handler.postDelayed({ if (initialized.get() && !isPremium()) retry() }, delayMs)
    }

    /** Muestra un rewarded. [onReward] solo se llama si el usuario lo vio completo. */
    fun showRewarded(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewarded?.takeIf { isFresh(it, rewardedLoadedAt) }
        if (ad == null) {
            preload()
            onUnavailable()
            return
        }
        rewarded = null
        var earned = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
                if (earned) onReward()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "No se mostró el rewarded: ${error.code} ${error.message}")
                preload()
                onUnavailable()
            }
        }
        ad.show(activity) { earned = true }
    }

    /** Al terminar cada ronda: deja un intersticial listo para cuando salgan del resultado. */
    fun onRoundCompleted() = preload()

    /** Intersticial al salir del resultado, si corresponde; siempre termina llamando a [then]. */
    fun maybeShowInterstitial(activity: Activity, then: () -> Unit) {
        val ad = interstitial?.takeIf { isFresh(it, interstitialLoadedAt) }
        val now = System.currentTimeMillis()
        val allowed = !isPremium() &&
            prefs.roundsCompleted > FREE_ROUNDS &&
            now - prefs.lastInterstitialAt >= MIN_INTERVAL_MS
        if (!allowed || ad == null) {
            if (ad == null) preload()
            then()
            return
        }
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preload()
                then()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "No se mostró el intersticial: ${error.code} ${error.message}")
                preload()
                then()
            }
        }
        prefs.lastInterstitialAt = now
        ad.show(activity)
    }
}
