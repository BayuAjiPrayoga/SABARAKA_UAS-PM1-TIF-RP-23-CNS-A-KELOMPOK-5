package com.kelompok5.sabaraka.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kelompok5.sabaraka.model.BorrowRequest
import java.util.*

class BorrowRequestViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _borrowRequests = MutableLiveData<List<BorrowRequest>>()
    val borrowRequests: LiveData<List<BorrowRequest>> = _borrowRequests

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private var allBorrowRequests = listOf<BorrowRequest>()
    private val _filteredRequests = MutableLiveData<List<BorrowRequest>>()
    val filteredRequests: LiveData<List<BorrowRequest>> = _filteredRequests

    fun searchBorrowRequests(query: String) {
        if (query.isBlank()) {
            _filteredRequests.value = allBorrowRequests
        } else {
            val filtered = allBorrowRequests.filter { request ->
                request.userName.contains(query, ignoreCase = true) ||
                        request.userNim.contains(query, ignoreCase = true) ||
                        request.userEmail.contains(query, ignoreCase = true)
            }
            _filteredRequests.value = filtered
        }
    }

    fun loadAllBorrowRequests() {
        _isLoading.value = true
        _error.value = ""

        db.collection("borrow_requests")
            .orderBy("requestDate", Query.Direction.DESCENDING)
            .addSnapshotListener { documents, exception ->
                _isLoading.value = false

                if (exception != null) {
                    _error.value = "Error loading borrow requests: ${exception.message}"
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val requests = documents.mapNotNull { document ->
                        try {
                            document.toObject(BorrowRequest::class.java).apply {
                                id = document.id
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // Ambil data lengkap untuk setiap request
                    loadDetailedRequestData(requests)
                } else {
                    _borrowRequests.value = emptyList()
                }
            }
    }

    private fun loadDetailedRequestData(requests: List<BorrowRequest>) {
        if (requests.isEmpty()) {
            _borrowRequests.value = emptyList()
            return
        }

        val detailedRequests = mutableListOf<BorrowRequest>()
        var processedCount = 0

        requests.forEach { request ->
            db.collection("users").document(request.userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val nameFromDoc = userDoc.getString("name")
                        val fullNameFromDoc = userDoc.getString("fullName")
                        request.userName = when {
                            fullNameFromDoc != null -> fullNameFromDoc
                            nameFromDoc != null -> nameFromDoc
                            else -> "Nama tidak tersedia"
                        }
                        request.userNim = userDoc.getString("studentId") ?: ""
                        request.userEmail = userDoc.getString("email") ?: ""
                        Log.d("BorrowRequestViewModel", "Loaded user data: userId=${request.userId}, name=${request.userName}, nim=${request.userNim}, nameFromDB=${nameFromDoc}, fullNameFromDB=${fullNameFromDoc}")
                    } else {
                        Log.w("BorrowRequestViewModel", "User document not found for userId=${request.userId}")
                    }

                    db.collection("items").document(request.itemId)
                        .get()
                        .addOnSuccessListener { itemDoc ->
                            if (itemDoc.exists()) {
                                request.itemName = itemDoc.getString("name") ?: "Barang tidak tersedia"
                            }
                            detailedRequests.add(request)
                            processedCount++

                            if (processedCount == requests.size) {
                                val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                                allBorrowRequests = sortedRequests
                                _borrowRequests.value = sortedRequests
                                _filteredRequests.value = sortedRequests
                            }
                        }
                        .addOnFailureListener {
                            detailedRequests.add(request)
                            processedCount++
                            if (processedCount == requests.size) {
                                val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                                allBorrowRequests = sortedRequests
                                _borrowRequests.value = sortedRequests
                                _filteredRequests.value = sortedRequests
                            }
                        }
                }
                .addOnFailureListener {
                    detailedRequests.add(request)
                    processedCount++
                    if (processedCount == requests.size) {
                        val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                        allBorrowRequests = sortedRequests
                        _borrowRequests.value = sortedRequests
                        _filteredRequests.value = sortedRequests
                    }
                }
        }
    }

    fun updateBorrowRequestStatus(requestId: String, newStatus: String) {
        if (requestId.isEmpty()) {
            _error.value = "Invalid request ID"
            return
        }

        // Pertama ambil data request untuk mendapatkan itemId dan quantity
        db.collection("borrow_requests")
            .document(requestId)
            .get()
            .addOnSuccessListener { requestDoc ->
                if (requestDoc.exists()) {
                    val oldStatus = requestDoc.getString("status") ?: ""
                    val itemId = requestDoc.getString("itemId") ?: ""
                    val quantity = requestDoc.getLong("quantity")?.toInt() ?: 1

                    val updates = mutableMapOf<String, Any>(
                        "status" to newStatus,
                        "updatedAt" to Date()
                    )

                    // Add return date if status is returned
                    if (newStatus == "returned") {
                        updates["returnDate"] = Date()
                    }

                    // Update status terlebih dahulu
                    db.collection("borrow_requests")
                        .document(requestId)
                        .update(updates)
                        .addOnSuccessListener {
                            // Kemudian update stok berdasarkan perubahan status
                            updateStockBasedOnStatus(itemId, quantity, oldStatus, newStatus)
                            loadAllBorrowRequests()
                        }
                        .addOnFailureListener { exception ->
                            _error.value = "Failed to update status: ${exception.message}"
                        }
                } else {
                    _error.value = "Request not found"
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Failed to get request: ${exception.message}"
            }
    }

    private fun updateStockBasedOnStatus(itemId: String, quantity: Int, oldStatus: String, newStatus: String) {
        when {
            // Jika disetujui dari pending -> kurangi stok
            oldStatus == "pending" && newStatus == "approved" -> {
                updateItemStock(itemId, quantity, false) // Kurangi stok
            }
            // Jika dikembalikan dari approved -> tambah stok
            oldStatus == "approved" && newStatus == "returned" -> {
                updateItemStock(itemId, quantity, true) // Tambah stok kembali
            }
            // Jika ditolak dari pending -> tidak perlu update stok (belum dikurangi)
            // Jika dibatalkan dari approved -> tambah stok kembali
            oldStatus == "approved" && newStatus == "rejected" -> {
                updateItemStock(itemId, quantity, true) // Tambah stok kembali
            }
        }
    }

    private fun updateItemStock(itemId: String, quantityChange: Int, isReturn: Boolean) {
        db.collection("items")
            .document(itemId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Gunakan field yang konsisten dengan database
                    val currentAvailable = document.getLong("availableStock")?.toInt() ?: 0
                    val totalQuantity = document.getLong("quantity")?.toInt() ?: 0

                    val newAvailable = if (isReturn) {
                        minOf(currentAvailable + quantityChange, totalQuantity) // Tidak boleh lebih dari total
                    } else {
                        maxOf(currentAvailable - quantityChange, 0) // Tidak boleh negatif
                    }

                    val updates = hashMapOf<String, Any>(
                        "availableStock" to newAvailable,
                        "status" to if (newAvailable > 0) "available" else "unavailable",
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )

                    db.collection("items")
                        .document(itemId)
                        .update(updates)
                        .addOnSuccessListener {
                            // Log untuk debugging
                            android.util.Log.d("BorrowRequest", "Stock updated for item $itemId: $currentAvailable -> $newAvailable (${if (isReturn) "returned" else "borrowed"} $quantityChange)")
                        }
                        .addOnFailureListener { exception ->
                            _error.value = "Gagal mengurangi stok: ${exception.message}"
                            android.util.Log.e("BorrowRequest", "Failed to update stock", exception)
                        }
                } else {
                    _error.value = "Item tidak ditemukan saat update stok"
                }
            }
            .addOnFailureListener { exception ->
                _error.value = "Gagal mengakses item: ${exception.message}"
            }
    }

    fun loadUserBorrowRequests(userId: String) {
        _isLoading.value = true
        _error.value = ""

        // Hentikan jika pengguna tidak terautentikasi
        if (auth.currentUser == null) {
            _isLoading.value = false
            _error.value = "Akses ditolak. Silakan login terlebih dahulu."
            _borrowRequests.value = emptyList()
            return
        }

        // Pastikan userId sesuai dengan user yang sedang login untuk keamanan
        if (auth.currentUser?.uid != userId) {
            _isLoading.value = false
            _error.value = "Tidak diizinkan mengakses data pengguna lain."
            _borrowRequests.value = emptyList()
            return
        }

        db.collection("borrow_requests")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { documents, exception ->
                _isLoading.value = false

                if (exception != null) {
                    _error.value = "Error loading user requests: ${exception.message}"
                    return@addSnapshotListener
                }

                if (documents != null) {
                    val requests = documents.mapNotNull { document ->
                        try {
                            document.toObject(BorrowRequest::class.java).apply {
                                id = document.id
                            }
                        } catch (e: Exception) {
                            null
                        }
                    }

                    // Ambil data lengkap untuk setiap request seperti pada loadAllBorrowRequests
                    loadDetailedUserRequestData(requests)
                } else {
                    _borrowRequests.value = emptyList()
                }
            }
    }

    private fun loadDetailedUserRequestData(requests: List<BorrowRequest>) {
        if (requests.isEmpty()) {
            _borrowRequests.value = emptyList()
            return
        }

        val detailedRequests = mutableListOf<BorrowRequest>()
        var processedCount = 0

        requests.forEach { request ->
            // Ambil data user (meskipun ini user yang sama, tetap perlu untuk konsistensi data)
            db.collection("users").document(request.userId)
                .get()
                .addOnSuccessListener { userDoc ->
                    if (userDoc.exists()) {
                        val nameFromDoc = userDoc.getString("name")
                        val fullNameFromDoc = userDoc.getString("fullName")
                        request.userName = when {
                            fullNameFromDoc != null -> fullNameFromDoc
                            nameFromDoc != null -> nameFromDoc
                            else -> "Nama tidak tersedia"
                        }
                        request.userNim = userDoc.getString("studentId") ?: ""
                        request.userEmail = userDoc.getString("email") ?: ""
                    }

                    // Ambil data item
                    db.collection("items").document(request.itemId)
                        .get()
                        .addOnSuccessListener { itemDoc ->
                            if (itemDoc.exists()) {
                                request.itemName = itemDoc.getString("name") ?: "Barang tidak tersedia"
                                request.itemDescription = itemDoc.getString("description") ?: ""
                                request.itemImageUrl = itemDoc.getString("imageUrl") ?: ""
                            }

                            detailedRequests.add(request)
                            processedCount++

                            // Jika semua request sudah diproses, update UI
                            if (processedCount == requests.size) {
                                val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                                _borrowRequests.value = sortedRequests
                            }
                        }
                        .addOnFailureListener {
                            // Tetap tambahkan request meskipun gagal ambil data item
                            detailedRequests.add(request)
                            processedCount++

                            if (processedCount == requests.size) {
                                val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                                _borrowRequests.value = sortedRequests
                            }
                        }
                }
                .addOnFailureListener {
                    // Tetap tambahkan request meskipun gagal ambil data user
                    detailedRequests.add(request)
                    processedCount++

                    if (processedCount == requests.size) {
                        val sortedRequests = detailedRequests.sortedByDescending { it.requestDate }
                        _borrowRequests.value = sortedRequests
                    }
                }
        }
    }

    // Fungsi untuk membersihkan data saat logout
    fun clearData() {
        android.util.Log.d("BorrowRequestViewModel", "Clearing data and removing listeners")

        // Bersihkan semua data
        _borrowRequests.value = emptyList()
        _filteredRequests.value = emptyList()
        _isLoading.value = false
        _error.value = ""
        allBorrowRequests = emptyList()
    }

    // Fungsi untuk memuat ulang data setelah login
    fun reloadAfterLogin() {
        android.util.Log.d("BorrowRequestViewModel", "Reloading data after login")

        // Reset state
        _borrowRequests.value = emptyList()
        _filteredRequests.value = emptyList()
        _error.value = ""
        allBorrowRequests = emptyList()

        // Muat ulang semua borrow requests (untuk admin)
        loadAllBorrowRequests()
    }
}