package com.kelompok5.sabaraka.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.kelompok5.sabaraka.LoginActivity
import com.kelompok5.sabaraka.MainActivity
import com.kelompok5.sabaraka.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding
    private lateinit var auth: FirebaseAuth
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

        auth = FirebaseAuth.getInstance()

        // Hide status bar for immersive experience
        supportActionBar?.hide()

        // Delay for 3 seconds then check authentication
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d(TAG, "Delay finished, checking authentication")
            checkAuthenticationAndNavigate()
        }, 3000)
    }

    private fun checkAuthenticationAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "User logged in, navigating to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            Log.d(TAG, "User not logged in, navigating to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        }
        finish()
    }
}