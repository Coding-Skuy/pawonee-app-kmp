# ADR-0003 — Versi Navigation3, Compose Multiplatform, dan AGP

Status: Diterima | Tanggal: 2026-10-21

## Konteks

`AGENTS.md` mengunci Navigation3 1.0.0 dan Compose Multiplatform 1.8.2. Saat mengimplementasikan PWN-KMP-003 ditemukan:

- `org.jetbrains.androidx.navigation3:navigation3-ui` tidak pernah terbit sebagai 1.0.0 (hanya `1.0.0-alpha01..06`, lalu `1.1.0`); artefak `navigation3-runtime` hanya terbit di `androidx.navigation3` (Google).
- `navigation3-ui:1.0.0-alpha06` dan `1.1.0` bergantung pada Compose 1.10 (`ui`, `animation`, `runtime` 1.10.x) dan Kotlin 2.2.20, sehingga tidak dapat dipadukan dengan CMP 1.8.2.
- Klib iOS `navigation3-ui:1.1.0` dibangun dengan Kotlin 2.3.20 (abi 2.3.0) sehingga TIDAK terbaca Kotlin 2.2.20 (`KLIB resolver: Could not find ...`); `1.0.0-alpha06` dibangun dengan Kotlin 2.2.20 dan terbukti dikompilasi untuk iOS.
- Dependensi Android turunannya (`navigation3-*-android`, `activity:1.12.0`) mewajibkan Android Gradle Plugin 8.9.1+ dan `compileSdk` 36.
- ADR-0001 butir 1 mewajibkan Navigation3, jadi satu-satunya jalur yang konsisten adalah menaikkan CMP.

## Keputusan

Katalog versi (`gradle/libs.versions.toml`) memakai Compose Multiplatform **1.10.0** dan `org.jetbrains.androidx.navigation3:navigation3-ui` **1.0.0-alpha06** (membawa `androidx.navigation3:navigation3-runtime` 1.0.0 secara transitif; ini rilis terdekat dengan "1.0.0" di AGENTS.md yang kompatibel dengan Kotlin 2.2.20), dengan Kotlin 2.2.20 tidak berubah. Android Gradle Plugin naik ke **8.9.3** dan `compileSdk` ke **36** (`targetSdk` tetap 34, `minSdk` 26). Versi lain tidak berubah. Angka di `AGENTS.md` perlu diperbarui oleh pemilik repo lewat PR dokumen terpisah (ADR ini tidak menyunting `AGENTS.md`).

## Konsekuensi

- Seluruh Compose naik ke 1.10.x; modul `:shared:pantry-resep` tidak bergantung Compose sehingga tidak terpengaruh.
- Web dan Pedaree tidak terdampak (hanya mengonsumsi artefak `pantry-resep`).
- API Navigation3 yang dipakai (`NavDisplay` dengan back stack `List`, `entryProvider`) mengikuti 1.0.0-alpha06 (alpha: API dapat berubah; naik ke 1.1.x menunggu Kotlin 2.3.x lewat ADR baru).
