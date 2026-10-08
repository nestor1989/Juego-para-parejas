package com.idea3d.juegoparaparejas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.idea3d.juegoparaparejas.databinding.ActivityOnboardingBinding
import com.idea3d.juegoparaparejas.databinding.ItemOnboardingPageBinding
import com.idea3d.juegoparaparejas.util.setUpEdgeToEdge

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding
    private var currentPage = 0

    companion object {
        const val PREFS_NAME = "onboarding"
        const val KEY_SEEN = "seen"
        const val EXTRA_REVIEW_MODE = "review_mode"
    }

    private val isReviewMode: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_REVIEW_MODE, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpEdgeToEdge(binding.root)

        bindPage(binding.page1, R.drawable.ic_heart_outline, R.color.pink,
            R.string.onboarding_page1_title, R.string.onboarding_page1_body)
        bindPage(binding.page2, R.drawable.ic_eye_off, R.color.purple_accent,
            R.string.onboarding_page2_title, R.string.onboarding_page2_body)
        bindPage(binding.page3, R.drawable.ic_lock, R.color.gold,
            R.string.onboarding_page3_title, R.string.onboarding_page3_body)

        if (isReviewMode) {
            binding.skipButton.visibility = android.view.View.GONE
        }
        binding.skipButton.setOnClickListener { finishOnboarding() }
        binding.nextButton.setOnClickListener {
            if (currentPage == 2) {
                finishOnboarding()
            } else {
                currentPage++
                renderPage()
            }
        }

        renderPage()
    }

    override fun onBackPressed() {
        if (isReviewMode) {
            finish()
        } else {
            // First run: onboarding can't be dismissed with back, only Skip/Next.
        }
    }

    private fun bindPage(
        page: ItemOnboardingPageBinding,
        iconRes: Int,
        tintRes: Int,
        titleRes: Int,
        bodyRes: Int
    ) {
        page.pageIcon.setImageResource(iconRes)
        page.pageIcon.imageTintList = ContextCompat.getColorStateList(this, tintRes)
        page.pageTitle.text = getString(titleRes)
        page.pageBody.text = getString(bodyRes)
    }

    private fun renderPage() {
        binding.page1.root.visibility = if (currentPage == 0) android.view.View.VISIBLE else android.view.View.GONE
        binding.page2.root.visibility = if (currentPage == 1) android.view.View.VISIBLE else android.view.View.GONE
        binding.page3.root.visibility = if (currentPage == 2) android.view.View.VISIBLE else android.view.View.GONE

        binding.dot1.background = ContextCompat.getDrawable(
            this, if (currentPage == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
        )
        binding.dot2.background = ContextCompat.getDrawable(
            this, if (currentPage == 1) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
        )
        binding.dot3.background = ContextCompat.getDrawable(
            this, if (currentPage == 2) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive
        )
        binding.dot1.layoutParams.width = dp(if (currentPage == 0) 22 else 8)
        binding.dot2.layoutParams.width = dp(if (currentPage == 1) 22 else 8)
        binding.dot3.layoutParams.width = dp(if (currentPage == 2) 22 else 8)
        binding.dotsRow.requestLayout()

        binding.nextButton.text = when {
            currentPage < 2 -> getString(R.string.onboarding_next)
            isReviewMode -> getString(R.string.onboarding_close)
            else -> getString(R.string.onboarding_start)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private fun finishOnboarding() {
        if (isReviewMode) {
            finish()
            return
        }
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_SEEN, true)
            .apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
