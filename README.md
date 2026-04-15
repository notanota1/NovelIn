# NovelIn - Aplikasi Pembaca Novel Android

NovelIn adalah aplikasi Android sederhana yang dirancang untuk membaca novel secara digital. Aplikasi ini mengimplementasikan konsep pemrograman berorientasi objek (OOP), penyimpanan data lokal dengan SQLite, dan antarmuka modern menggunakan Fragments dan RecyclerView.

## ✨ Fitur Utama

- **Sistem Login & Register**: Autentikasi pengguna menggunakan database lokal SQLite.
- **Library/Search**: Menampilkan katalog novel yang tersedia dalam bentuk grid (3 kolom) menggunakan RecyclerView.
- **Penyimpanan Favorit (Home)**: Pengguna dapat menyimpan novel pilihan ke halaman Home (Library Pribadi).
- **Detail Novel**: Menampilkan informasi lengkap seperti judul, penulis (biodata), sinopsis, dan cover.
- **Fitur Membaca**: Viewer teks yang bersih untuk membaca isi konten novel.
- **Navigasi Modern**: Menggunakan Bottom Navigation untuk berpindah antar menu (Home, Search, Account).
- **Manajemen Data Terpusat**: Seluruh teks novel dikelola melalui `strings.xml` dan kelas data khusus untuk kemudahan pemeliharaan.

## 🛠️ Teknologi yang Digunakan

- **Bahasa Pemrograman**: Java
- **Database**: SQLite (untuk data User dan daftar novel tersimpan)
- **UI Components**: 
    - RecyclerView (Grid Layout)
    - Fragment Manager
    - BottomNavigationView
    - CardView & ScrollView
- **Data Persistence**: SharedPreferences (untuk manajemen sesi login)

## 📂 Struktur Project Penting

- `LoginActivity.java` & `RegisterActivity.java`: Logika autentikasi.
- `DatabaseHelper.java`: Pengelolaan database SQLite.
- `NovelData.java`: Kelas penyedia data novel yang mengambil resource dari `strings.xml`.
- `HomeFragment.java`: Menampilkan novel yang disimpan oleh user.
- `LibraryFragment.java`: Menampilkan semua daftar novel (fitur Search).
- `DetailFragment.java`: Halaman profil novel dan tombol simpan/baca.
- `ReadingFragment.java`: Viewer teks isi novel.


---
*Dibuat untuk memenuhi tugas/proyek pengembangan aplikasi Android dengan standar OOP dan navigasi Fragment.*
