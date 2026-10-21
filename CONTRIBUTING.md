# Panduan Kontribusi — pawonee-app-kmp

Baca dulu `AGENTS.md` dan semua ADR di `docs/decisions/`. Dokumen `versions/v*` bersifat beku (immutable); jangan diedit.

## Alur: fork -> branch -> atomic commit -> PR

1. **Fork** `Coding-Skuy/pawonee-app-kmp` ke akunmu, clone fork, lalu tambahkan remote `upstream`:
   `git remote add upstream https://github.com/Coding-Skuy/pawonee-app-kmp.git`
2. **Branch** dari `upstream/main`. Penamaan: `feat/<kode-tiket>-<slug>`, `test/...`, `docs/...`, `chore/...`
   (kode tiket huruf kecil, mis. `feat/pwn-kmp-002-rekomendasi-engine`). Satu branch = satu tiket = satu PR.
3. **Atomic commit**: satu perubahan logis per commit, pesan conventional commits berbahasa Indonesia
   (`feat:`, `fix:`, `test:`, `docs:`, `chore:`). Commit yang mengubah perilaku wajib memperbarui `CHANGELOG.md`.
4. **Uji lokal** sebelum PR: `./gradlew :shared:pantry-resep:allTests` (bila wrapper tersedia). Kode baru disertai uji (TDD merah-hijau).
5. **Push** ke fork, buka **PR** ke `Coding-Skuy/pawonee-app-kmp` base `main`, isi template PR, dan tulis `Closes #N`.
6. Tinjauan manusia wajib. Jangan merge sendiri, jangan force-push ke `main`, jangan ubah pengaturan repo.

## Aturan penting

- Rumus skor/porsi hanya di `:shared:pantry-resep` (AGENTS.md aturan 9).
- Perubahan requirement lebih dulu PR di `Pawonee-TownHall`.
- Perubahan arsitektur = ADR baru bernomor berurutan (lihat `docs/decisions/README.md`).
- Jangan commit rahasia (`.env`, kunci, token).
