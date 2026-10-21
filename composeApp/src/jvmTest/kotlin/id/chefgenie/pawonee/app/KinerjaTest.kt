package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.BankBawaan
import id.chefgenie.pawonee.app.domain.ProfilKeluarga
import id.chefgenie.pawonee.app.nav.NavigasiState
import id.chefgenie.pawonee.pantryresep.KodeGrade
import id.chefgenie.pawonee.pantryresep.PorsiScaler
import id.chefgenie.pawonee.pantryresep.RekomendasiEngine
import id.chefgenie.pawonee.pantryresep.StokItem
import kotlin.system.measureNanoTime
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Anggaran kinerja (FSD 10): rekomendasi 12 resep < 800 ms dan buka detail < 300 ms. Ini ukuran di JVM
 * mesin pengembang, BUKAN di HP RAM 3 GB; pengukuran perangkat dilakukan di siklus uji (Maestro/perangkat).
 */
class KinerjaTest {
    private val stok = listOf(
        StokItem("wortel", 500, "gram", KodeGrade.G2_V), StokItem("kentang", 600, "gram", KodeGrade.G2_U),
        StokItem("bayam", 400, "gram", KodeGrade.G2_S, 1), StokItem("kangkung", 400, "gram", KodeGrade.G2_S, 2),
        StokItem("tomat", 300, "gram", KodeGrade.G2_V), StokItem("cabai merah", 150, "gram", KodeGrade.G2_V),
        StokItem("bawang merah", 200, "gram", KodeGrade.G2_U), StokItem("jagung manis", 500, "gram", KodeGrade.G2_U),
        StokItem("tempe", 300, "gram", KodeGrade.NON_G2), StokItem("telur", 6, "pcs", KodeGrade.NON_G2),
    )

    @Test
    fun rekomendasiDuaBelasResepDiBawah800ms() {
        val engine = RekomendasiEngine(BankBawaan.bank, BankBawaan.harga)
        val profil = ProfilKeluarga().keProfilRekomendasi()
        engine.rekomendasikan(profil, stok, 4, 45) // pemanasan JIT
        val ms = measureNanoTime { engine.rekomendasikan(profil, stok, 4, 45) } / 1_000_000.0
        println("KINERJA rekomendasi(12 resep, dingin-hangat) = $ms ms")
        assertTrue(ms < 800.0, "rekomendasi $ms ms")
    }

    @Test
    fun bukaDetailDiBawah300ms() {
        val resep = BankBawaan.bank.resep.first()
        val ms = measureNanoTime {
            NavigasiState().bukaDeepLink("pawonee://resep/${resep.id}?jiwa=4", 4)
            PorsiScaler.skalaResep(resep, 4)
        } / 1_000_000.0
        println("KINERJA buka detail = $ms ms")
        assertTrue(ms < 300.0, "detail $ms ms")
    }
}
