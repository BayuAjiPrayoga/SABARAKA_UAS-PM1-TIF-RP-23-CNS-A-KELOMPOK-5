package com.kelompok5.sabaraka.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.kelompok5.sabaraka.WelcomeActivity
import com.kelompok5.sabaraka.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)
            Log.d(TAG, "Layout binding successful. Root view: ${binding.root}")
            binding.root.post {
                Log.d(TAG, "View layout completed")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind layout: ${e.message}")
        }

        // Hide status bar for immersive experience
        supportActionBar?.hide()

        // Delay for 3 seconds then navigate to WelcomeActivity
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Delay finished, navigating to WelcomeActivity")
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 3000)
    }
}
