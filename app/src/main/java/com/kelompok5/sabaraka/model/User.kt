package com.kelompok5.sabaraka.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "mahasiswa", // "admin" or "mahasiswa"
    val nim: String = "" // Only for mahasiswa
) {
    constructor() : this("", "", "", "mahasiswa", "")
}
