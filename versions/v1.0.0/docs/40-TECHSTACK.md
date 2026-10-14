> Versi: v1.0.0 | Status: disetujui | Menggantikan: -

# 40-TECHSTACK — Tumpukan Teknologi (pawonee-app-kmp)

Mengacu: Pawonee-TownHall `platform/versions/v1.0.0` (https://github.com/Coding-Skuy/Pawonee-TownHall/tree/main/platform/versions/v1.0.0), beku 2026-10-07. Tidak menduplikasi BRD/PRD/FRD/FSD/SRS; hanya mengunci stack repo ini.

## 1. Matriks stack

| Komponen | Kondisi saat ini (tercatat) | Target terkunci v1.0.0 |
|---|---|---|
| Bahasa/KMP | Kotlin 2.0.21 | Kotlin 2.2.20 |
| UI | Compose Multiplatform 1.7.0 | Compose Multiplatform 1.8.2 |
| Navigasi | belum ada | Navigation3 1.0.0 (satu graf 6 layar) |
| Jaringan | Ktor Client 3.0.3 | Ktor Client (versi dikunci saat kickoff) |
| DI | Koin 4.0.0 | Koin (versi dikunci saat kickoff) |
| Penyimpanan lokal | belum ada | SQLDelight (cache + antrean) + DataStore (profil) |
| Android | minSdk 26 / targetSdk 34 | minSdk 26 / targetSdk 34 |
| iOS | Xcode 16.0 / target 15.0 | Xcode 16.0 / target 15.0 |

## 2. Matriks fitur per platform

| Fitur | Android | iOS | Windows |
|---|---|---|---|
| Rekomendasi luring (maks 5 item, <800 ms) | Ya | Ya | Tidak (di luar cakupan) |
| Detail resep + skala 1–8 jiwa | Ya | Ya | Tidak |
| Profil keluarga (DataStore) | Ya | Ya | Tidak |
| Tandai dimasak + riwayat serapan | Ya | Ya | Tidak |
| Pekerja harian 07.00 + notifikasi stok kedaluwarsa ≤1 hari | WorkManager | BGTaskScheduler | Tidak |
| Deep link `pawonee://resep/{id}?jiwa=4` | Ya | Ya | Tidak |
| Distribusi | Play Store + APK sideload | TestFlight | Tidak ada |

## 3. Navigation graph ringkas (Navigation3, satu graf)

```
beranda ──> rekomendasi ──> resepDetail(id, jiwa) ──> [Tandai dimasak] ──> riwayat
   ├──────> stok ─────────────────────────────────┘
   └──────> preferensi (StateFlow: ubah profil -> hitung ulang otomatis)
deep link pawonee://resep/{id}?jiwa=4  ──> resepDetail
```

Enam layar: `beranda`, `stok`, `rekomendasi`, `resepDetail`, `preferensi`, `riwayat` (FSD 10 butir 5).

## Batasan

- Dokumen ini tidak mengubah manifes atau kode; kondisi saat ini dicatat apa adanya dari manifes/README repo.
- Versi dikunci eksak di manifes saat kickoff coding; perubahan versi dicatat di dokumen ini lewat versi dokumen berikutnya dan ADR baru.
- JWT audiens `pawonee`, DB logis `pawonee`, dan `:shared:pantry-resep` milik Pawonee tidak berubah oleh dokumen ini.
