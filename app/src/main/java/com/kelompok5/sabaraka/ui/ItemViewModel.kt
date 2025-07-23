package com.kelompok5.sabaraka.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import com.kelompok5.sabaraka.model.Item
import com.kelompok5.sabaraka.model.BorrowRequest
import java.util.*
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class ItemViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var itemsListener: ListenerRegistration? = null

    private val _items = MutableLiveData<List<Item>>()
    val items: LiveData<List<Item>> = _items

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _saveSuccess = MutableLiveData<Boolean>()
    val saveSuccess: LiveData<Boolean> = _saveSuccess

    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected: LiveData<Boolean> = _isConnected

    private var lastDocument: com.google.firebase.firestore.DocumentSnapshot? = null
    private val pageSize = 50

    init {
        _errorMessage.value = ""
        _isConnected.value = true
        loadItems()
    }

    fun loadItems() {
        _isLoading.value = true
        _errorMessage.value = ""

        if (auth.currentUser == null) {
            _isLoading.value = false
            _errorMessage.value = "Akses ditolak. Silakan login terlebih dahulu."
            _items.value = emptyList()
            itemsListener?.remove()
            return
        }

        itemsListener?.remove()

        try {
            Log.d("ItemViewModel", "Setting up items listener...")
            val query = db.collection("items")
                .limit(pageSize.toLong())
                .orderBy("name")

            itemsListener = query.addSnapshotListener(MetadataChanges.INCLUDE) { documents, exception ->
                _isLoading.value = false

                if (exception != null) {
                    Log.e("ItemViewModel", "Error loading items", exception)
                    _errorMessage.value = "Error loading items: ${exception.message}"
                    _items.value = emptyList()
                    return@addSnapshotListener
                }

                if (documents != null) {
                    _isConnected.value = !documents.metadata.isFromCache
                    lastDocument = documents.documents.lastOrNull()

                    val itemList = documents.mapNotNull { document ->
                        try {
                            val item = document.toObject(Item::class.java)
                            item.id = document.id
                            if (item.availableStock < 0) item.availableStock = 0
                            if (item.stock < 0) item.stock = 0
                            if (item.status.isNullOrEmpty()) item.status = "available"
                            Log.d("ItemViewModel", "Loaded item ${item.id}: stock=${item.stock}, availableStock=${item.availableStock}")
                            item
                        } catch (e: Exception) {
                            Log.w("ItemViewModel", "Error parsing item ${document.id}: ${e.message}")
                            null
                        }
                    }

                    val sortedItems = itemList.sortedBy { it.name }
                    Log.d("ItemViewModel", "Successfully loaded ${sortedItems.size} items")
                    _items.value = sortedItems
                    _errorMessage.value = ""
                } else {
                    Log.w("ItemViewModel", "Received null documents")
                    _items.value = emptyList()
                }
            }
        } catch (e: Exception) {
            _isLoading.value = false
            _errorMessage.value = "Failed to setup listener: ${e.message}"
            _items.value = emptyList()
            Log.e("ItemViewModel", "Failed to setup listener", e)
        }
    }

    private fun retryConnection() {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            if (_items.value?.isEmpty() == true || _isConnected.value == false) {
                Log.d("ItemViewModel", "Retrying connection...")
                loadItems()
            }
        }, 3000)
    }

    fun refreshItems() {
        Log.d("ItemViewModel", "Force refreshing items...")
        lastDocument = null
        loadItems()
    }

    fun addItem(item: Item) {
        _isLoading.value = true
        _errorMessage.value = ""
        _saveSuccess.value = false

        val itemData = hashMapOf(
            "name" to item.name,
            "description" to item.description,
            "category" to (item.category ?: ""),
            "status" to (item.status ?: "available"),
            "stock" to item.stock,
            "availableStock" to item.stock,
            "location" to (item.location ?: ""),
            "imageUrl" to (item.imageUrl ?: ""),
            "createdAt" to Timestamp.now(),
            "updatedAt" to Timestamp.now(),
            "createdBy" to (auth.currentUser?.uid ?: "")
        )

        Log.d("ItemViewModel", "Adding item: ${item.name} with stock: ${item.stock}")

        db.collection("items")
            .add(itemData)
            .addOnSuccessListener { documentRef ->
                _isLoading.value = false
                _saveSuccess.value = true
                Log.d("ItemViewModel", "Item added successfully with ID: ${documentRef.id}")
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Failed to add item: ${exception.message}"
                _saveSuccess.value = false
                Log.e("ItemViewModel", "Failed to add item", exception)
            }
    }

    fun updateItem(item: Item) {
        _isLoading.value = true
        _errorMessage.value = ""
        _saveSuccess.value = false // Reset success state sebelum operasi

        db.runTransaction { transaction ->
            val itemRef = db.collection("items").document(item.id)
            val snapshot = transaction.get(itemRef)

            if (!snapshot.exists()) {
                throw Exception("Item tidak ditemukan")
            }

            val currentItem = snapshot.toObject(Item::class.java)
            val currentBorrowedQuantity = (currentItem?.stock ?: 0) - (currentItem?.availableStock ?: 0)
            val newAvailableStock = item.stock - currentBorrowedQuantity

            val updateData = hashMapOf<String, Any>(
                "name" to item.name,
                "description" to item.description,
                "category" to (item.category ?: ""),
                "status" to if (newAvailableStock > 0) "available" else "unavailable",
                "stock" to item.stock,
                "availableStock" to maxOf(0, newAvailableStock),
                "updatedAt" to Timestamp.now()
            )

            transaction.update(itemRef, updateData)
        }.addOnSuccessListener {
            _isLoading.value = false
            _saveSuccess.value = true
            Log.d("ItemViewModel", "Item updated successfully")
            loadItems() // Sinkronisasi ulang data
            // Reset state setelah sukses untuk mencegah kebocoran ke operasi berikutnya
            _saveSuccess.value = false
        }.addOnFailureListener { exception ->
            _isLoading.value = false
            _saveSuccess.value = false
            _errorMessage.value = "Failed to update item: ${exception.message}"
            Log.e("ItemViewModel", "Failed to update item", exception)
        }
    }

    fun deleteItem(itemId: String) {
        _isLoading.value = true
        _errorMessage.value = ""

        Log.d("ItemViewModel", "Deleting item: $itemId")

        db.collection("items")
            .document(itemId)
            .delete()
            .addOnSuccessListener {
                _isLoading.value = false
                Log.d("ItemViewModel", "Item deleted successfully")
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Failed to delete item: ${exception.message}"
                Log.e("ItemViewModel", "Failed to delete item", exception)
            }
    }

    fun borrowItem(itemId: String, quantity: Int, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    callback(false, "Silakan login terlebih dahulu")
                    return@launch
                }

                val itemDoc = db.collection("items").document(itemId).get().await()
                val item = itemDoc.toObject(Item::class.java)

                if (item == null) {
                    callback(false, "Barang tidak ditemukan")
                    return@launch
                }

                if (item.availableStock < quantity) {
                    callback(false, "Stok tidak mencukupi")
                    return@launch
                }

                val borrowRequest = hashMapOf(
                    "itemId" to itemId,
                    "userId" to currentUser.uid,
                    "quantity" to quantity,
                    "status" to "pending",
                    "requestDate" to Date(),
                    "itemName" to item.name
                )

                db.collection("borrow_requests").add(borrowRequest).await()
                val newAvailableStock = item.availableStock - quantity
                db.collection("items").document(itemId)
                    .update("availableStock", newAvailableStock)
                    .await()

                callback(true, "Permintaan peminjaman berhasil diajukan")
            } catch (e: Exception) {
                callback(false, "Error: ${e.message}")
            }
        }
    }

    fun getBorrowRequests(callback: (List<BorrowRequest>) -> Unit) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    callback(emptyList())
                    return@launch
                }

                val requests = db.collection("borrow_requests")
                    .whereEqualTo("userId", currentUser.uid)
                    .get()
                    .await()
                    .toObjects(BorrowRequest::class.java)

                callback(requests)
            } catch (e: Exception) {
                _errorMessage.value = "Error loading borrow requests: ${e.message}"
                callback(emptyList())
            }
        }
    }

    fun getAllBorrowRequests(callback: (List<BorrowRequest>) -> Unit) {
        viewModelScope.launch {
            try {
                val requests = db.collection("borrow_requests")
                    .get()
                    .await()
                    .toObjects(BorrowRequest::class.java)

                callback(requests)
            } catch (e: Exception) {
                _errorMessage.value = "Error loading all borrow requests: ${e.message}"
                callback(emptyList())
            }
        }
    }

    fun approveBorrowRequest(requestId: String, itemId: String, quantity: Int) {
        _isLoading.value = true
        _errorMessage.value = ""

        db.runTransaction { transaction ->
            val itemRef = db.collection("items").document(itemId)
            val requestRef = db.collection("borrow_requests").document(requestId)

            val item = transaction.get(itemRef).toObject(Item::class.java)
            val request = transaction.get(requestRef).toObject(BorrowRequest::class.java)

            if (item == null || request == null) {
                throw IllegalStateException("Item atau permintaan tidak ditemukan")
            }

            if (item.availableStock < quantity) {
                throw IllegalStateException("Stok tidak mencukupi")
            }

            val newAvailableStock = item.availableStock - quantity
            transaction.update(itemRef, "availableStock", newAvailableStock)
            transaction.update(itemRef, "updatedAt", Timestamp.now())
            transaction.update(requestRef, "status", "approved")

            val borrowHistoryRef = db.collection("borrowHistory").document()
            val borrowHistory = hashMapOf(
                "itemId" to itemId,
                "userId" to request.userId,
                "quantity" to quantity,
                "borrowDate" to request.requestDate,
                "returnDate" to null,
                "status" to "approved"
            )
            transaction.set(borrowHistoryRef, borrowHistory)

            null
        }.addOnSuccessListener {
            _isLoading.value = false
            Log.d("ItemViewModel", "Borrow request approved for item $itemId")
            loadItems()
        }.addOnFailureListener { exception ->
            _isLoading.value = false
            _errorMessage.value = "Gagal menyetujui peminjaman: ${exception.message}"
            Log.e("ItemViewModel", "Error approving borrow request", exception)
        }
    }

    fun returnItem(requestId: String, itemId: String, quantity: Int) {
        _isLoading.value = true
        _errorMessage.value = ""

        val currentUser = auth.currentUser
        if (currentUser == null) {
            _isLoading.value = false
            _errorMessage.value = "Pengguna tidak terautentikasi. Silakan login terlebih dahulu."
            Log.e("ItemViewModel", "No authenticated user")
            return
        }

        Log.d("ItemViewModel", "Returning item - requestId: $requestId, itemId: $itemId, quantity: $quantity")

        db.runTransaction { transaction ->
            // Langkah 1: Ambil semua data yang dibutuhkan dari transaksi
            val itemRef = db.collection("items").document(itemId)
            val requestRef = db.collection("borrow_requests").document(requestId)
            val borrowHistoryRef = db.collection("borrowHistory").document(requestId)

            val itemSnapshot = transaction.get(itemRef)
            val requestSnapshot = transaction.get(requestRef)
            val historySnapshot = transaction.get(borrowHistoryRef)

            // Konversi snapshot ke objek
            val item = itemSnapshot.toObject(Item::class.java)
            val request = requestSnapshot.toObject(BorrowRequest::class.java)
            val historyExists = historySnapshot.exists()

            // Validasi data
            if (item == null || request == null) {
                throw IllegalStateException("Item atau permintaan tidak ditemukan")
            }

            if (request.status != "approved") {
                throw IllegalStateException("Permintaan harus dalam status 'approved' untuk dikembalikan")
            }

            // Hitung stok baru berdasarkan data yang dibaca
            val currentAvailableStock = item.availableStock ?: 0
            val newAvailableStock = currentAvailableStock + quantity
            if (newAvailableStock < 0) {
                throw IllegalStateException("Stok tidak boleh negatif")
            }
            Log.d("ItemViewModel", "Updating item $itemId, old stock: $currentAvailableStock, new stock: $newAvailableStock")

            // Langkah 2: Lakukan semua operasi tulis
            transaction.update(itemRef, mapOf(
                "availableStock" to newAvailableStock,
                "updatedAt" to Timestamp.now()
            ))
            transaction.update(requestRef, mapOf(
                "status" to "returned",
                "returnDate" to Timestamp.now()
            ))

            if (!historyExists) {
                transaction.set(borrowHistoryRef, hashMapOf(
                    "itemId" to itemId,
                    "userId" to (request.userId ?: ""),
                    "quantity" to quantity,
                    "borrowDate" to (request.requestDate ?: Timestamp.now()),
                    "returnDate" to Timestamp.now(),
                    "status" to "returned"
                ))
            } else {
                transaction.update(borrowHistoryRef, mapOf(
                    "returnDate" to Timestamp.now(),
                    "status" to "returned"
                ))
            }

            null // Kembalikan null karena ini transaksi void
        }.addOnSuccessListener {
            _isLoading.value = false
            Log.d("ItemViewModel", "Item returned successfully for item $itemId")
            loadItems() // Sinkronisasi ulang data
        }.addOnFailureListener { exception ->
            _isLoading.value = false
            _errorMessage.value = "Gagal mengembalikan barang: ${exception.message}"
            Log.e("ItemViewModel", "Error returning item", exception)
            // Jika gagal karena izin, coba reload data untuk sinkronisasi
            if (exception is FirebaseFirestoreException && exception.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                loadItems()
            }
        }
    }

    fun updateBorrowRequestStatus(requestId: String, newStatus: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                db.collection("borrow_requests")
                    .document(requestId)
                    .update("status", newStatus)
                    .await()

                callback(true)
            } catch (e: Exception) {
                _errorMessage.value = "Error updating request status: ${e.message}"
                callback(false)
            }
        }
    }

    fun submitBorrowRequest(itemId: String, userId: String, quantity: Int) {
        _isLoading.value = true
        _errorMessage.value = ""

        val currentDate = Date()
        val borrowRequest = hashMapOf(
            "itemId" to itemId,
            "userId" to userId,
            "quantity" to quantity,
            "status" to "pending",
            "requestDate" to currentDate,
            "returnDate" to null,
            "notes" to "",
            "updatedAt" to currentDate,
            "userName" to "",
            "userNim" to "",
            "userEmail" to "",
            "itemName" to "",
            "itemDescription" to "",
            "itemImageUrl" to ""
        )

        Log.d("ItemViewModel", "Submitting borrow request for item: $itemId, quantity: $quantity")

        db.collection("borrow_requests")
            .add(borrowRequest)
            .addOnSuccessListener { documentRef ->
                _isLoading.value = false
                Log.d("ItemViewModel", "Borrow request submitted with ID: ${documentRef.id}")
                _errorMessage.value = "Pengajuan peminjaman berhasil disubmit. Menunggu persetujuan admin."
            }
            .addOnFailureListener { exception ->
                _isLoading.value = false
                _errorMessage.value = "Gagal mengajukan peminjaman: ${exception.message}"
                Log.e("ItemViewModel", "Failed to submit borrow request", exception)
            }
    }

    fun clearError() {
        _errorMessage.value = ""
    }

    fun clearData() {
        Log.d("ItemViewModel", "Clearing data and removing listeners")
        itemsListener?.remove()
        itemsListener = null
        _items.value = emptyList()
        _isLoading.value = false
        _errorMessage.value = ""
        _saveSuccess.value = false
        _isConnected.value = true
    }

    fun reloadAfterLogin() {
        Log.d("ItemViewModel", "Reloading data after login")
        _errorMessage.value = ""
        _isConnected.value = true
        _isLoading.value = true
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            loadItems()
        }, 500)
    }

    fun checkAuthState(): Boolean {
        return auth.currentUser != null
    }

    override fun onCleared() {
        super.onCleared()
        itemsListener?.remove()
        Log.d("ItemViewModel", "ViewModel cleared, listener removed")
    }
}