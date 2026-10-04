# pawonee-app-kmp

Aplikasi mobile rekomendasi resep Pawonee — **Divisi Pawonee (AI Cooking Assistant)**, org `Coding-Skuy`. Bagian dari arsitektur **Opsi A ChefGenie**.

- TownHall: [Coding-Skuy/Pawonee-TownHall](https://github.com/Coding-Skuy/Pawonee-TownHall) — sumber kebenaran visi, roadmap, dan keputusan divisi.
- Modul bersama milik Pawonee: `:shared:pantry-resep` (lokasi: `shared/pantry-resep`) — model dapur, bahan, dan rekomendasi yang dipakai aplikasi ini, web pendamping (`pawonee-web`), dan backend (`pawonee-backend-service`).
- Konsumen hilir: **Pedaree** memakai sinyal resep dan preferensi dari Pawonee sebagai masukan rekomendasi pendampingnya.

## Teknologi (versi dipin)

| Komponen | Versi |
|---|---|
| Kotlin | 2.0.21 |
| Compose Multiplatform | 1.7.0 |
| Ktor Client | 3.0.3 |
| kotlinx.coroutines | 1.9.0 |
| Koin | 4.0.0 |
| Android minSdk / targetSdk | 26 / 34 |
| Xcode / iOS deployment target | 16.0 / 15.0 |

Perubahan versi dicatat di `gradle/libs.versions.toml` — itulah satu-satunya sumber versi dependensi.

## Struktur

```
settings.gradle.kts
gradle/libs.versions.toml
shared/pantry-resep/          # modul milik Pawonee, dipakai semua klien
composeApp/                   # UI Compose Multiplatform (Android + iOS)
```

## Cara jalan (Android)

1. Buka proyek di Android Studio Ladybug atau lebih baru dengan plugin Kotlin 2.0.21.
2. Sinkronisasi Gradle, lalu jalankan konfigurasi `composeApp`.
3. Backend yang dipakai: `pawonee-backend-service` (basis URL default `http://10.0.2.2:8080`, dapat diubah lewat BuildConfig).

## Kontrak dengan backend

- `GET /v1/resep/rekomendasi?bahan=bawang,tempe` → daftar resep dari bank resep grade-2.
- `GET /v1/preferensi/{id}` / `PUT /v1/preferensi/{id}` → preferensi pengguna (pedas, alergi, anggaran).
- Skema respons mengikuti `models/schema.json` di repo `pawonee-ai-models`.

## Repo terkait

- Web pendamping: [pawonee-web](https://github.com/Coding-Skuy/pawonee-web)
- Backend Rust: [pawonee-backend-service](https://github.com/Coding-Skuy/pawonee-backend-service)
- Bank resep: [pawonee-ai-models](https://github.com/Coding-Skuy/pawonee-ai-models)
- Pipa data: [pawonee-data-pipeline](https://github.com/Coding-Skuy/pawonee-data-pipeline)
- Desain kartu resep: [pawonee-design](https://github.com/Coding-Skuy/pawonee-design)
- Infra: [pawonee-infra-devops](https://github.com/Coding-Skuy/pawonee-infra-devops)
