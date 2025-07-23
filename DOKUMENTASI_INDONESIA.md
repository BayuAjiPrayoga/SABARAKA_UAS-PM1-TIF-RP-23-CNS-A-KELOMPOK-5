# 📦 SABARAKA - Sistem Administrasi Barang Kampus

## 📖 Dokumentasi Proyek Android (Bahasa Indonesia)

## 📖 Use Case Diagram
@startuml
left to right direction
actor Mahasiswa
actor Admin

rectangle SABARAKA {
Mahasiswa --> (Login/Register)
Mahasiswa --> (Lihat Daftar Barang)
Mahasiswa --> (Ajukan Peminjaman)
Mahasiswa --> (Lihat Riwayat Peminjaman)
Mahasiswa --> (Lihat Status Peminjaman)

    Admin --> (Login)
    Admin --> (Kelola Barang)
    Admin --> (Kelola Permintaan)
    (Kelola Permintaan) --> (Setujui/Tolak Permintaan)
    Admin --> (Lihat Laporan Peminjaman)
}
@enduml


---

## 📱 Gambaran Umum Proyek

**SABARAKA** adalah aplikasi Android untuk sistem administrasi barang kampus. Aplikasi ini memungkinkan mahasiswa untuk meminjam peralatan kampus secara online melalui sistem persetujuan dari admin. Dikembangkan dengan arsitektur **MVVM** dan backend **Firebase**.

---

## 🎯 Fitur-Fitur Utama

### 👥 Role Pengguna
- **Admin:**
  - CRUD inventaris barang
  - Menyetujui/menolak permintaan peminjaman
  - Melihat laporan dan riwayat
- **Mahasiswa:**
  - Melihat daftar barang tersedia
  - Mengajukan dan tracking permintaan
  - Melihat riwayat peminjaman

### 🔐 Autentikasi & Otorisasi
- Firebase Authentication (Login/Register)
- Navigasi berbasis role
- Firebase Security Rules

### 📦 Manajemen Inventaris
- CRUD barang (Admin only)
- Stok real-time & lokasi barang
- Status barang: *available/unavailable*
- Upload gambar barang (siap implementasi)

### 📋 Sistem Peminjaman
- Mahasiswa mengajukan peminjaman
- Admin menyetujui/menolak
- Status: pending, approved, rejected, returned
- Riwayat lengkap & notifikasi (siap)

---

## 🏗️ Arsitektur Proyek

### 🛠️ Tech Stack
- **Bahasa:** Kotlin
- **Framework:** Android SDK (Min SDK 25, Target SDK 34)
- **Arsitektur:** MVVM
- **Database:** Firebase Firestore
- **UI:** Material Design, View Binding, Navigation Components

### 📦 Dependencies
```kotlin
// Firebase
firebase-auth
firebase-firestore

// Architecture Components
navigation-fragment-ktx
lifecycle-viewmodel-ktx
lifecycle-livedata-ktx

// UI
material
constraintlayout
recyclerview
swiperefreshlayout

// Async
kotlinx-coroutines
