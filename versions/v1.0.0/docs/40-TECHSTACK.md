> Versi: v1.0.0 | Status: disetujui | Menggantikan: -

# 40-TECHSTACK — Tumpukan Teknologi (pawonee-app-kmp)

Mengacu: Pawonee-TownHall v1.0.0 (https://github.com/Coding-Skuy/Pawonee-TownHall).

## 1. Kondisi saat ini (tercatat)

- Kotlin 2.0.21, Compose Multiplatform 1.7.0, Ktor Client 3.0.3, kotlinx.coroutines 1.9.0, Koin 4.0.0, minSdk 26 dan targetSdk 34 (sumber: `gradle/libs.versions.toml`, `README.md`).

## 2. Target standar emas (rencana)

- Kotlin 2.2.20, Compose Multiplatform 1.8.2, Navigation 3 (nav3) 1.0.0, Ktor Client 3.x terbaru, coroutines 1.9.x terbaru, Koin 4.x terbaru. Satu-satunya sumber versi tetap `gradle/libs.versions.toml`.
- Selaras divisi: KMP Kotlin 2.2.20 dan Compose 1.8.2 dan nav3 1.0.0; Rust axum 0.8.4; Python 3.12; web SvelteKit dan Bun terbaru; Postgres 16.x; database `pawonee`; JWT audiens `pawonee`.

## 3. Langkah penyesuaian (rencana — bukan eksekusi sekarang)

1. Kunci versi eksak pada `gradle/libs.versions.toml` saat fase coding dimulai.
2. Selaraskan matriks `VERSIONS.md` di `pawonee-infra-devops` setelah fase coding dimulai.
3. Jalankan pemeriksaan build dan uji `:shared:pantry-resep` sebelum menaikkan versi.
4. Catat perubahan versi pada dokumen ini dan TownHall Pawonee-TownHall v1.0.0.

## Batasan

- Dokumen versi ini tidak mengubah manifes, kode, atau versi terpasang; semua target adalah rencana.
- Tidak ada migrasi framework atau kenaikan versi dalam dokumen ini.
- Model tetap mengikuti `:shared:pantry-resep` milik Pawonee; tidak ada duplikasi model baru.
- Perencanaan BRD, PRD, FSD, dan roadmap repo ini mengacu Pawonee-TownHall v1.0.0 dan tidak diduplikasi di sini.
