# Panduan Menambahkan Data Sample ke Firestore

## 1. Buka Firebase Console
- Buka https://console.firebase.google.com/
- Pilih project "sabaraka" 
- Klik "Firestore Database" di menu sebelah kiri

## 2. Menambahkan Koleksi "users"

### User 1 - Admin
- Klik "Start collection" 
- Collection ID: `users`
- Document ID: `admin1`
- Fields:
  - uid: `admin1` (string)
  - name: `Admin SABARAKA` (string)
  - email: `admin@sabaraka.com` (string)
  - role: `admin` (string)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)

### User 2 - Mahasiswa 1
- Klik "Add document"
- Document ID: `mahasiswa1`
- Fields:
  - uid: `mahasiswa1` (string)
  - name: `Mahasiswa Satu` (string)
  - email: `mahasiswa1@example.com` (string)
  - role: `mahasiswa` (string)
  - nim: `2024001` (string)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)

### User 3 - Mahasiswa 2
- Klik "Add document"
- Document ID: `mahasiswa2`
- Fields:
  - uid: `mahasiswa2` (string)
  - name: `Mahasiswa Dua` (string)
  - email: `mahasiswa2@example.com` (string)
  - role: `mahasiswa` (string)
  - nim: `2024002` (string)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)

## 3. Menambahkan Koleksi "items"

### Item 1 - Laptop
- Klik "Start collection" (atau tambah ke koleksi yang sudah ada)
- Collection ID: `items`
- Document ID: `item1`
- Fields:
  - name: `Laptop ASUS` (string)
  - description: `Laptop untuk kebutuhan mahasiswa` (string)
  - category: `Elektronik` (string)
  - status: `available` (string)
  - quantity: `5` (number)
  - availableQuantity: `5` (number)
  - location: `Lab Komputer A` (string)
  - imageUrl: `` (string - kosong)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - createdBy: `admin1` (string)

### Item 2 - Proyektor
- Document ID: `item2`
- Fields:
  - name: `Proyektor` (string)
  - description: `Proyektor untuk presentasi` (string)
  - category: `Elektronik` (string)
  - status: `available` (string)
  - quantity: `3` (number)
  - availableQuantity: `2` (number)
  - location: `Ruang Multimedia` (string)
  - imageUrl: `` (string - kosong)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - createdBy: `admin1` (string)

### Item 3 - Kamera DSLR
- Document ID: `item3`
- Fields:
  - name: `Kamera DSLR` (string)
  - description: `Kamera untuk dokumentasi kegiatan` (string)
  - category: `Elektronik` (string)
  - status: `available` (string)
  - quantity: `2` (number)
  - availableQuantity: `1` (number)
  - location: `Studio Foto` (string)
  - imageUrl: `` (string - kosong)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - createdBy: `admin1` (string)

### Item 4 - Mikrofon
- Document ID: `item4`
- Fields:
  - name: `Mikrofon Wireless` (string)
  - description: `Mikrofon untuk kegiatan seminar` (string)
  - category: `Audio` (string)
  - status: `available` (string)
  - quantity: `4` (number)
  - availableQuantity: `4` (number)
  - location: `Aula` (string)
  - imageUrl: `` (string - kosong)
  - createdAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - updatedAt: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - createdBy: `admin1` (string)

## 4. Menambahkan Koleksi "borrow_requests"

### Request 1 - Pending
- Collection ID: `borrow_requests`
- Document ID: `request1`
- Fields:
  - userId: `mahasiswa1` (string)
  - userName: `Mahasiswa Satu` (string)
  - userNim: `2024001` (string)
  - itemId: `item2` (string)
  - itemName: `Proyektor` (string)
  - quantity: `1` (number)
  - borrowDate: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - returnDate: `January 26, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - purpose: `Presentasi tugas akhir` (string)
  - status: `pending` (string)
  - requestDate: `January 19, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - approvedBy: `null` (null)
  - approvedDate: `null` (null)
  - returnedDate: `null` (null)
  - notes: `` (string - kosong)

### Request 2 - Approved
- Document ID: `request2`
- Fields:
  - userId: `mahasiswa2` (string)
  - userName: `Mahasiswa Dua` (string)
  - userNim: `2024002` (string)
  - itemId: `item3` (string)
  - itemName: `Kamera DSLR` (string)
  - quantity: `1` (number)
  - borrowDate: `January 17, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - returnDate: `January 24, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - purpose: `Dokumentasi kegiatan UKM` (string)
  - status: `approved` (string)
  - requestDate: `January 16, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - approvedBy: `admin1` (string)
  - approvedDate: `January 17, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - returnedDate: `null` (null)
  - notes: `Hati-hati dalam penggunaan` (string)

### Request 3 - Returned
- Document ID: `request3`
- Fields:
  - userId: `mahasiswa1` (string)
  - userName: `Mahasiswa Satu` (string)
  - userNim: `2024001` (string)
  - itemId: `item1` (string)
  - itemName: `Laptop ASUS` (string)
  - quantity: `1` (number)
  - borrowDate: `January 9, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - returnDate: `January 16, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - purpose: `Mengerjakan project` (string)
  - status: `returned` (string)
  - requestDate: `January 7, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - approvedBy: `admin1` (string)
  - approvedDate: `January 9, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - returnedDate: `January 16, 2025 at 12:00:00 PM UTC+7` (timestamp)
  - notes: `Dikembalikan dalam kondisi baik` (string)

## 5. Verifikasi Data
Setelah semua data ditambahkan, pastikan struktur database terlihat seperti ini:
```
- users (collection)
  - admin1 (document)
  - mahasiswa1 (document)
  - mahasiswa2 (document)
- items (collection)
  - item1 (document)
  - item2 (document)
  - item3 (document)
  - item4 (document)
- borrow_requests (collection)
  - request1 (document)
  - request2 (document)
  - request3 (document)
```

## 6. Catatan Penting
- Pastikan tipe data sesuai dengan yang tertera (string, number, timestamp, null)
- Firestore Rules yang sudah di-deploy akan mengatur akses ke data ini
- Data ini akan cukup untuk menguji semua fitur aplikasi SABARAKA
