package com.idea3d.juegoparaparejas

import android.app.Application
import com.idea3d.juegoparaparejas.data.PackRepository
import com.idea3d.juegoparaparejas.data.Prefs
import com.idea3d.juegoparaparejas.monetization.AdsManager
import com.idea3d.juegoparaparejas.monetization.BillingManager
import com.idea3d.juegoparaparejas.util.Analytics

/** Contenedor simple de dependencias; la app es chica y no necesita un framework de DI. */
class App : Application() {

    lateinit var prefs: Prefs
        private set
    lateinit var packs: PackRepository
        private set
    lateinit var billing: BillingManager
        private set
    lateinit var ads: AdsManager
        private set
    lateinit var analytics: Analytics
        private set

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        packs = PackRepository(this)
        analytics = Analytics(this)
        billing = BillingManager(this, prefs)
        ads = AdsManager(this, prefs).also { manager ->
            manager.isPremium = { billing.isPremium.value }
        }
        billing.start()
    }
}
