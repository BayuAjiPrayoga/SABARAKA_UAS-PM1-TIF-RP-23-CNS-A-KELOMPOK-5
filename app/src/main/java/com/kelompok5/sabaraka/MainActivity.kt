package com.kelompok5.sabaraka

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.kelompok5.sabaraka.databinding.ActivityMainBinding
import com.kelompok5.sabaraka.model.User
import com.kelompok5.sabaraka.ui.BorrowRequestViewModel
import com.kelompok5.sabaraka.ui.ItemViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private var currentUser: User? = null
    private var userListener: ListenerRegistration? = null

    // ViewModels untuk cleanup saat logout
    private lateinit var itemViewModel: ItemViewModel
    private lateinit var borrowRequestViewModel: BorrowRequestViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inisialisasi ViewModels
        itemViewModel = ViewModelProvider(this)[ItemViewModel::class.java]
        borrowRequestViewModel = ViewModelProvider(this)[BorrowRequestViewModel::class.java]

        // Cek apakah user sudah login
        if (auth.currentUser == null) {
            navigateToLogin()
            return
        }

        // Setup toolbar jika ada
        try {
            setSupportActionBar(binding.appBarMain.toolbar)
        } catch (e: Exception) {
            // Ignore if no toolbar found
            android.util.Log.w("MainActivity", "No toolbar found in layout")
        }

        // Ambil data user untuk menentukan role
        getCurrentUserData()
    }

    private fun getCurrentUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Gunakan listener untuk real-time updates
            userListener = db.collection("users").document(userId)
                .addSnapshotListener { document, error ->
                    if (error != null) {
                        android.util.Log.e("MainActivity", "Error getting user data", error)
                        // Jika terjadi permission error, logout user
                        if (error.message?.contains("PERMISSION_DENIED") == true) {
                            handlePermissionDenied()
                        }
                        return@addSnapshotListener
                    }

                    if (document != null && document.exists()) {
                        currentUser = document.toObject(User::class.java)
                        setupNavigation()
                    } else {
                        // User tidak ditemukan di Firestore, buat data user baru
                        createUserData()
                    }
                }
        } else {
            navigateToLogin()
        }
    }

    private fun createUserData() {
        val user = auth.currentUser
        if (user != null) {
            val userData = User(
                id = user.uid,
                email = user.email ?: "",
                name = user.email?.substringBefore("@") ?: "User",
                role = "mahasiswa", // Default role
                nim = ""
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    currentUser = userData
                    setupNavigation()
                }
                .addOnFailureListener { e ->
                    android.util.Log.e("MainActivity", "Error creating user data", e)
                    Toast.makeText(this, "Error creating user profile", Toast.LENGTH_SHORT).show()
                    navigateToLogin()
                }
        }
    }

    private fun setupNavigation() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)

            // Reload ViewModels data setelah user login berhasil
            try {
                if (::itemViewModel.isInitialized) {
                    itemViewModel.reloadAfterLogin()
                    android.util.Log.d("MainActivity", "ItemViewModel reloaded after login")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Error reloading ItemViewModel: ${e.message}")
            }

            try {
                if (::borrowRequestViewModel.isInitialized) {
                    borrowRequestViewModel.reloadAfterLogin()
                    android.util.Log.d("MainActivity", "BorrowRequestViewModel reloaded after login")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Error reloading BorrowRequestViewModel: ${e.message}")
            }

            // Navigate to appropriate fragment based on user role
            when (currentUser?.role) {
                "admin" -> {
                    if (navController.currentDestination?.id != R.id.adminItemListFragment) {
                        navController.navigate(R.id.adminItemListFragment)
                    }
                }
                else -> {
                    if (navController.currentDestination?.id != R.id.itemListFragment) {
                        navController.navigate(R.id.itemListFragment)
                    }
                }
            }

            // Setup navigation berdasarkan role user - menggunakan ID yang ada
            val topLevelDestinations = when (currentUser?.role) {
                "admin" -> setOf(
                    R.id.adminItemListFragment,
                    R.id.adminBorrowRequestFragment
                )
                else -> setOf(
                    R.id.itemListFragment,
                    R.id.borrowHistoryFragment
                )
            }

            // Setup AppBarConfiguration dengan drawer layout jika ada
            try {
                appBarConfiguration = AppBarConfiguration(topLevelDestinations, binding.drawerLayout)
            } catch (e: Exception) {
                appBarConfiguration = AppBarConfiguration(topLevelDestinations)
            }

            setupActionBarWithNavController(navController, appBarConfiguration)

            // Setup navigation view dengan menu yang sesuai berdasarkan role
            try {
                // Set menu yang sesuai dengan role user
                val menuResId = when (currentUser?.role) {
                    "admin" -> R.menu.activity_admin_drawer
                    else -> R.menu.activity_main_drawer
                }
                binding.navView.menu.clear()
                binding.navView.inflateMenu(menuResId)

                binding.navView.setupWithNavController(navController)
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "No navigation view found")
            }

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up navigation", e)
            // Fallback: setup basic navigation tanpa drawer
            setupBasicNavigation()
        }
    }

    private fun setupBasicNavigation() {
        try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            appBarConfiguration = AppBarConfiguration(navController.graph)
            setupActionBarWithNavController(navController, appBarConfiguration)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to setup basic navigation", e)
        }
    }

    private fun handlePermissionDenied() {
        Toast.makeText(this, "Akses ditolak. Silakan login ulang.", Toast.LENGTH_LONG).show()
        logout()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Hanya inflate menu jika file menu ada
        return try {
            menuInflater.inflate(R.menu.menu_main, menu)
            true
        } catch (e: Exception) {
            // Jika menu file tidak ada, buat menu sederhana secara programatis
            menu.add(0, R.id.action_logout, 0, "Logout")
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        try {
            android.util.Log.d("MainActivity", "Starting logout process")

            // Hapus semua listeners untuk mencegah permission errors
            userListener?.remove()
            userListener = null

            // Bersihkan data ViewModels jika sudah diinisialisasi untuk mencegah PERMISSION_DENIED error
            try {
                if (::itemViewModel.isInitialized) {
                    itemViewModel.clearData()
                    android.util.Log.d("MainActivity", "ItemViewModel data cleared")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Error clearing ItemViewModel: ${e.message}")
            }

            try {
                if (::borrowRequestViewModel.isInitialized) {
                    borrowRequestViewModel.clearData()
                    android.util.Log.d("MainActivity", "BorrowRequestViewModel data cleared")
                }
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Error clearing BorrowRequestViewModel: ${e.message}")
            }

            // Sign out dari Firebase Auth
            auth.signOut()
            android.util.Log.d("MainActivity", "Firebase Auth signed out")

            // Clear user data
            currentUser = null

            // Navigate to login
            navigateToLogin()

        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error during logout", e)
            // Force navigation to login even if logout fails
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onSupportNavigateUp(): Boolean {
        return try {
            val navController = findNavController(R.id.nav_host_fragment_content_main)
            navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } catch (e: Exception) {
            super.onSupportNavigateUp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cleanup listeners
        userListener?.remove()
    }
}
