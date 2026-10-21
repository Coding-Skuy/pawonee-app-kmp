package id.chefgenie.pawonee.app.nav

import androidx.compose.runtime.mutableStateListOf

/** Tumpukan navigasi (back stack) untuk `NavDisplay`. Murni sehingga bisa diuji tanpa UI. */
class NavigasiState(awal: Rute = Rute.Beranda) {
    val tumpukan = mutableStateListOf(awal)

    val atas: Rute get() = tumpukan.last()

    fun buka(rute: Rute) {
        if (atas != rute) tumpukan.add(rute)
    }

    /** Kembali satu layar; false bila sudah di akar. */
    fun kembali(): Boolean {
        if (tumpukan.size <= 1) return false
        tumpukan.removeAt(tumpukan.lastIndex)
        return true
    }

    /**
     * Buka deep link resep: tumpukan menjadi [Beranda, ResepDetail] agar Kembali menuju beranda.
     * `jiwa` tak sah/hilang memakai [jiwaProfil]. Mengembalikan false bila URI bukan deep link resep.
     */
    fun bukaDeepLink(uri: String, jiwaProfil: Int): Boolean {
        val dl = DeepLink.urai(uri) ?: return false
        tumpukan.clear()
        tumpukan.add(Rute.Beranda)
        tumpukan.add(Rute.ResepDetail(dl.id, dl.jiwa ?: jiwaProfil))
        return true
    }
}
