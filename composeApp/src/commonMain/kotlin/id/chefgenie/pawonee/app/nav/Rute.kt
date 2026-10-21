package id.chefgenie.pawonee.app.nav

/** Satu graf Navigation3 dengan enam layar (FSD 10-alur butir 5). `jiwa` dibawa sebagai argumen rute detail. */
sealed interface Rute {
    data object Beranda : Rute
    data object Stok : Rute
    data object Rekomendasi : Rute
    data class ResepDetail(val id: String, val jiwa: Int) : Rute
    data object Preferensi : Rute
    data object Riwayat : Rute
}
