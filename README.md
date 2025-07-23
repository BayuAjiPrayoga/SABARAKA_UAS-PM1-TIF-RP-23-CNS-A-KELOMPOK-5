# 📦 SABARAKA - Sistem Administrasi Barang Kampus

## 📖 Dokumentasi Proyek Android (Bahasa Indonesia)


## 📱 Gambaran Umum Proyek

**SABARAKA** adalah aplikasi Android untuk sistem administrasi barang kampus. Aplikasi ini memungkinkan mahasiswa untuk meminjam peralatan kampus secara online melalui sistem persetujuan dari admin. Dikembangkan dengan arsitektur **MVVM** dan backend **Firebase**.

---
### 🧩 Use Case Diagram – SABARAKA

<ul>
        <li><img src="https://github.com/BayuAjiPrayoga/SABARAKA_UAS_PM1_TIF-RP-23-CNS-A_KELOMPOK-5/blob/master/USE%20CASE%20SABARAKA.png" alt="Input Presensi"></li>
      </ul>

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
