package id.chefgenie.pawonee.app.ui

/** testTag stabil untuk Maestro (siklus 2). Di Android dipaparkan sebagai resource-id lewat testTagsAsResourceId. */
object Tag {
    const val LAYAR_BERANDA = "layar_beranda"
    const val LAYAR_STOK = "layar_stok"
    const val LAYAR_REKOMENDASI = "layar_rekomendasi"
    const val LAYAR_RESEP_DETAIL = "layar_resep_detail"
    const val LAYAR_PREFERENSI = "layar_preferensi"
    const val LAYAR_RIWAYAT = "layar_riwayat"

    const val NAV_STOK = "nav_stok"
    const val NAV_REKOMENDASI = "nav_rekomendasi"
    const val NAV_PREFERENSI = "nav_preferensi"
    const val NAV_RIWAYAT = "nav_riwayat"
    const val BANNER_LURING = "banner_luring"
    const val KEMBALI = "tombol_kembali"

    const val JIWA_NILAI = "jiwa_nilai"
    const val JIWA_KURANG = "jiwa_kurang"
    const val JIWA_TAMBAH = "jiwa_tambah"

    const val REKOMENDASI_KOSONG = "rekomendasi_kosong"
    const val MODE_HEMAT = "mode_hemat"
    const val DETAIL_NAMA = "detail_nama"
    const val DETAIL_JIWA = "detail_jiwa"
    const val DETAIL_TIDAK_ADA = "detail_tidak_ada"
    const val STOK_KOSONG = "stok_kosong"

    fun kartuResep(kode: String) = "kartu_resep_$kode"
    fun bahan(nama: String) = "bahan_$nama"
    fun stokItem(nama: String) = "stok_$nama"
}
