# ADR-0001 — Fondasi teknis pawonee-app-kmp

Status: Diterima | Tanggal: 2026-10-14

## Konteks

Pawonee v1.0.0 membutuhkan aplikasi mobile yang merekomendasikan 12 resep grade-2 dari stok Pedaree meski tanpa internet. Sumber kebenaran: Pawonee-TownHall `platform/versions/v1.0.0` (https://github.com/Coding-Skuy/Pawonee-TownHall/tree/main/platform/versions/v1.0.0), beku 2026-10-07; lintas divisi: [KONTEKS-LINTAS-DIVISI.md](https://github.com/Coding-Skuy/ChefGenie-TownHall/blob/main/KONTEKS-LINTAS-DIVISI.md). Tiket fondasi: PWN-KMP-001 (issue #1).

## Keputusan

1. Stack mobile: Kotlin Multiplatform + Compose Multiplatform + Navigation3 untuk Android dan iOS; dilarang framework UI lain.
2. Offline-first: `RekomendasiEngine` dan `PorsiScaler` berjalan on-device; cache resep 7 hari, stok 24 jam, profil di DataStore; API jarak jauh hanya untuk sinkron (FSD 10).
3. Repo ini PEMILIK `:shared:pantry-resep`; web, backend, dan Pedaree mengonsumsi keluarannya; perubahan model = bump versi artefak.
4. JWT audiens `pawonee` (akses 15 menit, refresh 7 hari) disimpan aman di perangkat; klien tidak pernah menyentuh DB.
5. Navigasi satu graf 6 layar (beranda, stok, rekomendasi, resepDetail, preferensi, riwayat); `jiwa` dibawa sebagai argumen; deep link `pawonee://resep/{id}?jiwa=4`.

## Konsekuensi

- Rumus skor hanya satu sumber (Kotlin); setiap perubahan memaksa pembaruan port TypeScript dan fixture paritas 32 kasus.
- Antrean tulis luring butuh idempotensi sisi server (`PUT profil` memakai versi profil).
- Pengguna Windows desktop tidak didukung pada v1.0.0.
