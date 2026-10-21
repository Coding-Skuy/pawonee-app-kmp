# CHANGELOG

Format: [Keep a Changelog](https://keepachangelog.com/id-ID/1.1.0/). Versi mengikuti `versions/` TownHall.

## [Belum dirilis]

### Ditambahkan
- PWN-KMP-002: modul `:shared:pantry-resep` 1.0.0 (`id.chefgenie.pawonee:pantry-resep`): `PorsiScaler` (BR-105/BR-106, `JIWA_DI_LUAR_RENTANG`) dan `RekomendasiEngine` lima tahap; uji paritas 32 kasus (`kasus-uji.json`, bank `bankSha256` dicek) dan uji unit.
- Wrapper Gradle 8.14.3, katalog versi Kotlin 2.2.20 / Compose Multiplatform 1.8.2 / Navigation3 1.0.0 (selaras AGENTS.md).
- PWN-KMP-003: modul `:composeApp` dengan satu graf Navigation3 berisi enam layar (beranda, stok, rekomendasi, resepDetail, preferensi, riwayat), deep link `pawonee://resep/{id}?jiwa=4` (intent filter Android), argumen `jiwa`, hitung ulang rekomendasi otomatis lewat `StateFlow` profil, `testTag` untuk Maestro, ADR-0003.
- PWN-KMP-004: luring-dahulu. Cache SQLDelight (resep 7 hari, stok 24 jam), profil di DataStore `profil_keluarga.json` (1 aktif + maks 4 cadangan, tanpa TTL), klien Ktor `GET /resep` dan `GET /stok`, banner luring bila sinkron > 24 jam, pekerja harian 07.00 (WorkManager Android: sinkron stok Pedaree, bahan layu G2-S, notifikasi stok kedaluwarsa <= 1 hari), anggaran kinerja dicek di uji JVM.

### Diubah
- Katalog versi: Compose Multiplatform 1.10.0, Navigation3 (JetBrains) 1.0.0-alpha06, AGP 8.9.3, compileSdk 36 menggantikan 1.8.2/1.0.0 (tak pernah terbit)/8.7.2/35 (ADR-0003).
- Model skeleton lama `skuy.pawonee.pantry` diganti paket `id.chefgenie.pawonee.pantryresep`.

## [1.0.0] — 2026-10-14

### Ditambahkan
- Fondasi AI-dev v1.0.0: `AGENTS.md`, `CLAUDE.md`, ADR-0001, `40-TECHSTACK.md`, `60-BLAST-RADIUS.md`.
