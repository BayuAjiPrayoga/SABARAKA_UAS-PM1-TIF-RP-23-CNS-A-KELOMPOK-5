package com.kelompok5.sabaraka

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok5.sabaraka.databinding.ActivityWelcomeBinding
import com.kelompok5.sabaraka.model.User

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Jika user sudah login, langsung ke MainActivity
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // Tombol Login
        binding.btnLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Tombol Register
        binding.btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

//        // Admin login (hidden feature)
//        binding.tvAdminLogin.setOnClickListener {
//            loginAsAdmin()
//        }
    }

    private fun loginAsAdmin() {
        // Login dengan akun admin yang sudah ditentukan
        val adminEmail = "admin@sabaraka.com"
        val adminPassword = "admin123"

        auth.signInWithEmailAndPassword(adminEmail, adminPassword)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        // Pastikan user ini adalah admin
                        createOrUpdateAdminUser(user.uid)
                    }
                } else {
                    // Jika admin belum ada, buat akun admin baru
                    createAdminAccount(adminEmail, adminPassword)
                }
            }
    }

    private fun createAdminAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        createOrUpdateAdminUser(user.uid)
                    }
                } else {
                    Toast.makeText(this, "Gagal membuat akun admin: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun createOrUpdateAdminUser(userId: String) {
        val adminData = User(
            id = userId,
            email = "admin@sabaraka.com",
            name = "Administrator",
            role = "admin",
            nim = ""
        )

        db.collection("users").document(userId)
            .set(adminData)
            .addOnSuccessListener {
                Toast.makeText(this, "Login sebagai admin berhasil", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan data admin: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
