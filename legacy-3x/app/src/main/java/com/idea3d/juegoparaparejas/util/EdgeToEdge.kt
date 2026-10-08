package com.idea3d.juegoparaparejas.util

import android.app.Activity
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Android 15 (targetSdk 35) enforces edge-to-edge regardless of what the app does,
 * so every screen needs to consume system bar insets itself or content renders
 * under the status bar / gesture nav bar. This pads [root] by the system bars on
 * top of whatever padding it already had in XML, while its background still draws
 * full-bleed behind the bars.
 */
fun Activity.setUpEdgeToEdge(root: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)

    val initialLeft = root.paddingLeft
    val initialTop = root.paddingTop
    val initialRight = root.paddingRight
    val initialBottom = root.paddingBottom

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.setPadding(
            initialLeft + bars.left,
            initialTop + bars.top,
            initialRight + bars.right,
            initialBottom + bars.bottom
        )
        insets
    }
}
