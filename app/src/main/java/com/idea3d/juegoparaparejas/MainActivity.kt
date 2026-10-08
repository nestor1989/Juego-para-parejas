package com.idea3d.juegoparaparejas

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import com.idea3d.juegoparaparejas.game.ChallengeLinks
import com.idea3d.juegoparaparejas.ui.GameViewModel
import com.idea3d.juegoparaparejas.ui.JuegoApp
import com.idea3d.juegoparaparejas.ui.theme.JuegoTheme

class MainActivity : ComponentActivity() {

    private val vm: GameViewModel by viewModels()

    private val app: App get() = application as App

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            JuegoTheme {
                JuegoApp(vm)
            }
        }
        app.ads.gatherConsent(this)
        if (savedInstanceState == null) {
            val openedFromLink = handleLink(intent)
            if (!openedFromLink) checkInstallReferrer()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleLink(intent)
    }

    override fun onResume() {
        super.onResume()
        app.billing.refresh()
    }

    /** Link de desafío o de resultado: https://<dominio>/j/#<código> o juegoparejas://j?c=<código>. */
    private fun handleLink(intent: Intent?): Boolean {
        val code = ChallengeLinks.codeFromUrl(intent?.dataString) ?: return false
        app.prefs.referrerChecked = true
        vm.openCode(code, "link")
        return true
    }

    /**
     * Deep link diferido: si la persona instaló la app desde la página de un desafío,
     * Play guarda el código en el referrer y la partida arranca sola en el primer uso.
     */
    private fun checkInstallReferrer() {
        val prefs = app.prefs
        if (prefs.referrerChecked) return
        val client = InstallReferrerClient.newBuilder(this).build()
        try {
            client.startConnection(object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            val referrer = runCatching { client.installReferrer.installReferrer }.getOrNull()
                            prefs.referrerChecked = true
                            ChallengeLinks.codeFromReferrer(referrer)?.let { code ->
                                runOnUiThread { vm.openCode(code, "install_referrer") }
                            }
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED,
                        InstallReferrerClient.InstallReferrerResponse.DEVELOPER_ERROR -> prefs.referrerChecked = true
                        else -> Unit // SERVICE_UNAVAILABLE: se reintenta en el próximo inicio
                    }
                    runCatching { client.endConnection() }
                }

                override fun onInstallReferrerServiceDisconnected() = Unit
            })
        } catch (e: SecurityException) {
            prefs.referrerChecked = true
        }
    }
}
