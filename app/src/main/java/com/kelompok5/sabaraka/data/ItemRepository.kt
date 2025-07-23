package com.kelompok5.sabaraka.data

import com.google.firebase.firestore.FirebaseFirestore
import com.kelompok5.sabaraka.model.BorrowRequest
import com.kelompok5.sabaraka.model.Item
import kotlinx.coroutines.tasks.await
import java.util.Date

class ItemRepository {
    private val db = FirebaseFirestore.getInstance()
    private val itemsCollection = db.collection("items")
    private val borrowRequestsCollection = db.collection("borrow_requests")

    suspend fun getItems(): List<Item> {
        return try {
            itemsCollection.get().await().toObjects(Item::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun returnItem(itemId: String, quantity: Int = 1) {
        val itemDoc = itemsCollection.document(itemId).get().await()
        val item = itemDoc.toObject(Item::class.java)
        if (item != null) {
            val newAvailableStock = item.availableStock + quantity
            itemsCollection.document(itemId).update(
                mapOf(
                    "availableStock" to newAvailableStock,
                    "status" to "available",
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
        }
    }

    suspend fun addItem(item: Item) {
        try {
            val itemId = if (item.id.isEmpty()) {
                itemsCollection.document().id
            } else {
                item.id
            }
            val itemWithId = item.copy(id = itemId)
            itemsCollection.document(itemId).set(itemWithId).await()
        } catch (e: Exception) {
            throw Exception("Gagal menambah barang: ${e.message}")
        }
    }

    suspend fun updateItem(item: Item) {
        itemsCollection.document(item.id).set(item).await()
    }

    suspend fun deleteItem(itemId: String) {
        itemsCollection.document(itemId).delete().await()
    }

    suspend fun requestBorrow(itemId: String, userId: String): String {
        val request = BorrowRequest(itemId = itemId, userId = userId, requestDate = Date())
        return borrowRequestsCollection.add(request).await().id
    }

    suspend fun getBorrowRequests(userId: String): List<BorrowRequest> {
        return borrowRequestsCollection.whereEqualTo("userId", userId).get().await().toObjects(BorrowRequest::class.java)
    }

    suspend fun getAllBorrowRequests(): List<BorrowRequest> {
        return borrowRequestsCollection.get().await().toObjects(BorrowRequest::class.java)
    }

    suspend fun updateBorrowRequestStatus(requestId: String, status: String) {
        borrowRequestsCollection.document(requestId).update("status", status).await()
    }
}