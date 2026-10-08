package com.idea3d.juegoparaparejas

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

class Juegoparaparejasaddapp: Application() {
    override fun onCreate() {
        super.onCreate()

        // Inicializar Google Mobile Ads
        MobileAds.initialize(this) {}

        // Configurar test device para ver test ads
        val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf("33BE2250B43518CCDA7DE426D04EE232"))
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)
    }
}

