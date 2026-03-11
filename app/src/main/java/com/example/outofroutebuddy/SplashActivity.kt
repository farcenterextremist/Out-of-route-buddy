package com.example.outofroutebuddy

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

/**
 * Splash screen shown on app launch for a fixed duration (1.2 seconds),
 * then navigates to MainActivity.
 */
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION_MS = 1200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Theme provides windowBackground with truck image; no setContentView needed
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, SPLASH_DURATION_MS)
    }
}
