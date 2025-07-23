package com.kelompok5.sabaraka.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Item(
    var id: String = "",
    var name: String = "",
    var description: String = "",
    var category: String? = "",
    var status: String? = "available",

    // Stock management fields
    var stock: Int = 0, // Total stock
    @get:PropertyName("availableStock") @set:PropertyName("availableStock")
    var availableStock: Int = 0, // Available stock for borrowing

    // Legacy field for backward compatibility
    @get:PropertyName("quantity") @set:PropertyName("quantity")
    var quantity: Int = 0,

    // Optional fields
    var location: String? = "",
    var imageUrl: String? = "",
    var createdBy: String? = "",

    // Timestamps
    var createdAt: Timestamp? = null, // Ganti dari Long ke Timestamp
    var updatedAt: Timestamp? = null  // Ganti dari Long ke Timestamp
) {
    // No-argument constructor untuk Firestore
    constructor() : this(
        id = "",
        name = "",
        description = "",
        category = "",
        status = "available",
        stock = 0,
        availableStock = 0,
        quantity = 0,
        location = "",
        imageUrl = "",
        createdBy = "",
        createdAt = null,
        updatedAt = null
    )

    // Convenience properties for backward compatibility
    val isAvailable: Boolean
        get() = status == "available" && availableStock > 0

    val isBorrowable: Boolean
        get() = availableStock > 0

    val borrowedQuantity: Int
        get() = stock - availableStock
}