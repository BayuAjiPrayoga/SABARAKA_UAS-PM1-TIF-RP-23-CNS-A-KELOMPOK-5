package com.kelompok5.sabaraka.model

import java.util.Date

data class BorrowRequest(
    var id: String = "",
    var itemId: String = "",
    var userId: String = "",
    var quantity: Int = 0,
    var status: String = "pending",
    var requestDate: Date = Date(),
    var returnDate: Date? = null, // Sesuaikan dengan nama field di Firestore (returnDate atau returnedDate)
    var approvedDate: Date? = null, // Tambahkan jika digunakan di Firestore
    var notes: String = "",
    var userName: String = "",
    var userNim: String = "",
    var userEmail: String = "",
    var itemName: String = "",
    var itemDescription: String = "",
    var itemImageUrl: String = "",
    var updatedAt: Date = Date()
)