package com.kelompok5.sabaraka

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok5.sabaraka.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            registerUser()
        }

        binding.btnBackToLogin.setOnClickListener {
            finish() // Kembali ke LoginActivity
        }
    }

    private fun registerUser() {
        val fullName = binding.etFullName.text.toString().trim()
        val studentId = binding.etStudentId.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validasi input
        when {
            fullName.isEmpty() -> {
                binding.etFullName.error = "Nama lengkap harus diisi"
                return
            }
            studentId.isEmpty() -> {
                binding.etStudentId.error = "NIM/NPM harus diisi"
                return
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email harus diisi"
                return
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Password harus diisi"
                return
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password minimal 6 karakter"
                return
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Password tidak sama"
                return
            }
        }

        // Disable tombol register saat proses
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Mendaftar..."

        // Create user dengan Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user != null) {
                    // Simpan data user ke Firestore
                    val userData = hashMapOf(
                        "fullName" to fullName,
                        "studentId" to studentId,
                        "email" to email,
                        "role" to "mahasiswa",
                        "createdAt" to System.currentTimeMillis()
                    )

                    Log.d("RegisterActivity", "Attempting to save user data: UID=${user.uid}, Data=$userData")
                    db.collection("users").document(user.uid)
                        .set(userData)
                        .addOnSuccessListener {
                            Log.d("RegisterActivity", "User data saved successfully for UID=${user.uid}")
                            Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finishAffinity() // Tutup semua activity sebelumnya
                        }
                        .addOnFailureListener { e ->
                            Log.e("RegisterActivity", "Failed to save user data: ${e.message}", e)
                            Toast.makeText(this, "Gagal menyimpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                            user.delete() // Hapus user dari Auth jika gagal simpan data
                            resetButton()
                        }
                } else {
                    Toast.makeText(this, "Gagal membuat akun", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Pendaftaran gagal: ${e.message}", Toast.LENGTH_SHORT).show()
                resetButton()
            }
    }

    private fun resetButton() {
        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "DAFTAR"
    }
}