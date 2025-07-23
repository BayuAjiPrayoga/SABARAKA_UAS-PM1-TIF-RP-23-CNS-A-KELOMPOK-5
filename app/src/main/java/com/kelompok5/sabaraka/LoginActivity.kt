package com.kelompok5.sabaraka

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok5.sabaraka.databinding.ActivityLoginBinding
import com.kelompok5.sabaraka.model.User

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
        // Setup click listeners untuk semua tombol
        binding.ivBackArrow.setOnClickListener {
            finish() // Kembali ke activity sebelumnya
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, password)
        }

        // Navigate to sign up
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Forgot password
        binding.tvForgetPassword.setOnClickListener {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Fitur lupa password belum tersedia", Toast.LENGTH_SHORT).show()
        }


        // Social Login
        binding.ivGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google login belum tersedia", Toast.LENGTH_SHORT).show()
        }

        binding.ivFacebookLogin.setOnClickListener {
            Toast.makeText(this, "Facebook login belum tersedia", Toast.LENGTH_SHORT).show()
        }

        binding.ivFingerprintLogin.setOnClickListener {
            Toast.makeText(this, "Fingerprint login belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Login berhasil
                    val user = auth.currentUser
                    if (user != null) {
                        checkUserRole(user.uid)
                    }
                } else {
                    // Login gagal
                    Toast.makeText(this, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun checkUserRole(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        Toast.makeText(this, "Login berhasil sebagai ${user.role}", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                } else {
                    // User belum ada di Firestore, buat sebagai mahasiswa
                    val userData = User(
                        id = userId,
                        email = auth.currentUser?.email ?: "",
                        name = auth.currentUser?.email?.substringBefore("@") ?: "",
                        role = "mahasiswa",
                        nim = ""
                    )

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Login berhasil sebagai mahasiswa", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
