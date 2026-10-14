> Versi: v1.0.0 | Status: disetujui | Menggantikan: -

# 60-BLAST-RADIUS — Batas Context (pawonee-app-kmp)

Mengacu: Pawonee-TownHall `platform/versions/v1.0.0` (https://github.com/Coding-Skuy/Pawonee-TownHall/tree/main/platform/versions/v1.0.0), beku 2026-10-07. Baris Pawonee townhall-roadmap §2: point of concern "12 resep inti; serapan ≥1.500 g"; batas context "G2-V/U/S; porsi 1–8 jiwa; DB `pawonee`; JWT audiens `pawonee`". Rujukan lintas divisi: KONTEKS §8/§9.

## 1. Boleh disentuh repo ini

- `shared/pantry-resep` (model, `RekomendasiEngine`, `PorsiScaler`, tabel faktor porsi, pembulatan belanja).
- `composeApp/` (UI, navigasi, cache lokal, pekerja 07.00).
- Pemanggilan API backend `https://api.pawonee.chefgenie.id/v1` dengan header `X-Kontrak-Versi: v1`.

## 2. Dilarang

- Menghitung ulang rumus skor di luar `:shared:pantry-resep`.
- Menyimpan atau mengubah stok inventori (milik Pedaree; hanya dibaca lewat `GET /stok`).
- Menambah framework UI selain Compose Multiplatform atau target Windows.
- Menyentuh DB divisi lain (DB per service; Titeny hanya read-only ke DB sumber).
- Memakai komoditas non-pilot (kentang/wortel/tomat/kol) untuk klaim pasokan agregasi Lumbung; hanya cabai, bawang, kangkung (KONTEKS §4).
- Menambah resep ke-13, mengubah definisi G2, atau mengubah audiens JWT tanpa PR TownHall + ADR.

## 3. Kontrak yang diproduksi

- Artefak `id.chefgenie.pawonee:pantry-resep:1.0.0` (dikonsumsi web-port, backend, dan Pedaree).
- Aksi klien yang memicu event `rekomendasi.dihitung`, `resep.dimasak`, `serapan.dihitung`, `profil.disimpan` (FSD 30).
- Deep link `pawonee://resep/{id}?jiwa=4`.

## 4. Kontrak yang dikonsumsi

- API backend v1: `GET /resep`, `GET /resep/{id}`, `POST /rekomendasi`, `GET /stok`, `PUT /profil/{idKeluarga}`.
- Token desain dan spesifikasi RecipeCard dari `pawonee-design`.
- Fixture paritas 32 kasus dari `pawonee-ai-models`.

## 5. Dampak bila berubah

- Perubahan `:shared:pantry-resep` memecahkan port web, backend, bank resep, dan tampilan Pedaree sekaligus.
- Perubahan rumus memaksa regenerasi fixture 32 kasus dan evaluasi ulang 12/12.

## Batasan

- Dokumen ini hanya memetakan batas dan risiko; tidak mengubah kode atau konfigurasi.
- Keputusan lintas divisi dirujuk ke `KONTEKS-LINTAS-DIVISI.md` dan TownHall Pawonee v1.0.0.
