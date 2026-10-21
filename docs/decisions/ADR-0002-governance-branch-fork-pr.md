# ADR-0002 — Governance branch: fork, atomic commit, PR, dan usulan proteksi `main`

Status: Diterima | Tanggal: 2026-10-21

## Konteks

Beberapa kontributor (manusia dan agent AI) mengubah repo ini bersamaan. Tanpa alur baku, `main` rentan berisi commit campur-aduk. Tiket: PWN-KMP-GOV-BRANCH. Melengkapi ADR-0001 (tidak menggantikannya).

## Keputusan

1. Semua perubahan lewat fork -> branch -> PR ke `main`; tidak ada commit langsung ke `main`.
2. Penamaan branch `feat|test|docs|chore/<kode-tiket-huruf-kecil>-<slug>`; satu branch = satu tiket = satu PR.
3. Atomic commit dengan conventional commits berbahasa Indonesia; PR memakai `.github/pull_request_template.md` dan `Closes #N`.
4. `.github/CODEOWNERS` menetapkan `@Coding-Skuy` sebagai pemilik sampai ada tim.
5. **Usulan (belum diterapkan)** proteksi branch `main`: wajib PR, minimal 1 persetujuan, tinjauan CODEOWNERS, status check build/uji wajib lulus, dilarang force-push dan penghapusan branch. Penerapan di pengaturan repo dilakukan pemilik repo, bukan lewat PR ini.

## Konsekuensi

- Riwayat `main` bersih dan dapat ditelusuri ke tiket.
- Satu langkah manusia tersisa: pemilik repo menerapkan proteksi pada butir 5.
- Tinjauan manusia tetap diperlukan sebelum merge.
