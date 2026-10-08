package com.idea3d.juegoparaparejas.util

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.DecelerateInterpolator

/**
 * Soft pulsing ring behind a CTA button, inviting the user to tap it.
 * Call once per ring view; each cycle relaunches itself for an infinite loop.
 */
fun View.startPulseRing(startDelay: Long = 0L) {
    post {
        pivotX = width / 2f
        pivotY = height / 2f
        scaleX = 1f
        scaleY = 1f
        alpha = 0.55f

        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 1.5f)
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 1.5f)
        val alpha = ObjectAnimator.ofFloat(this, View.ALPHA, 0.55f, 0f)

        AnimatorSet().apply {
            playTogether(scaleX, scaleY, alpha)
            duration = 2400
            this.startDelay = startDelay
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (isAttachedToWindow) startPulseRing(0L)
                }
            })
            start()
        }
    }
}
