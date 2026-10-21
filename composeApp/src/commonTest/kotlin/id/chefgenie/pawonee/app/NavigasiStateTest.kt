package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.nav.NavigasiState
import id.chefgenie.pawonee.app.nav.Rute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NavigasiStateTest {
    @Test
    fun awalBeranda() {
        assertEquals(listOf<Rute>(Rute.Beranda), NavigasiState().tumpukan.toList())
    }

    @Test
    fun enamRuteLayar() {
        // Satu graf, enam layar: beranda, stok, rekomendasi, resepDetail, preferensi, riwayat.
        val rute = listOf(Rute.Beranda, Rute.Stok, Rute.Rekomendasi, Rute.ResepDetail("x", 4), Rute.Preferensi, Rute.Riwayat)
        assertEquals(6, rute.map { it::class }.toSet().size)
    }

    @Test
    fun bukaDanKembali() {
        val n = NavigasiState()
        n.buka(Rute.Rekomendasi)
        n.buka(Rute.ResepDetail("telur-balado", 3))
        assertEquals(Rute.ResepDetail("telur-balado", 3), n.atas)
        assertTrue(n.kembali())
        assertEquals(Rute.Rekomendasi, n.atas)
        assertTrue(n.kembali())
        assertFalse(n.kembali())
        assertEquals(Rute.Beranda, n.atas)
    }

    @Test
    fun rutePertamaTidakGanda() {
        val n = NavigasiState()
        n.buka(Rute.Stok)
        n.buka(Rute.Stok)
        assertEquals(2, n.tumpukan.size)
    }

    @Test
    fun deepLinkMembukaDetailLangsungDenganJiwa() {
        val n = NavigasiState()
        n.buka(Rute.Stok)
        assertTrue(n.bukaDeepLink("pawonee://resep/telur-balado?jiwa=5", jiwaProfil = 4))
        assertEquals(listOf(Rute.Beranda, Rute.ResepDetail("telur-balado", 5)), n.tumpukan.toList())
        assertTrue(n.kembali())
        assertEquals(Rute.Beranda, n.atas)
    }

    @Test
    fun deepLinkTanpaJiwaMemakaiProfil() {
        val n = NavigasiState()
        n.bukaDeepLink("pawonee://resep/R-03?jiwa=0", jiwaProfil = 6)
        assertEquals(Rute.ResepDetail("R-03", 6), n.atas)
    }

    @Test
    fun deepLinkTakDikenalDiabaikan() {
        val n = NavigasiState()
        assertFalse(n.bukaDeepLink("pawonee://lain/x", 4))
        assertEquals(1, n.tumpukan.size)
    }
}
