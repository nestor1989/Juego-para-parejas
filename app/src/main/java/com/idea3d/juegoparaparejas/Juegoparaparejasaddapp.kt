package com.idea3d.juegoparaparejas

import android.app.Application
import com.google.android.gms.ads.MobileAds

class Juegoparaparejasaddapp: Application() {
    override fun onCreate() {
        super.onCreate()
       MobileAds.initialize(this){}
    }
}