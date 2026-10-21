package id.chefgenie.pawonee.pantryresep

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RekomendasiEngineTest {
    private fun resep(kode: String, kelompok: String, menit: Int = 10, biaya: Int = 10000, alergen: List<String> = emptyList()) =
        ResepBank(
            kode = kode, id = kode.lowercase(), nama = kode, kelompok = kelompok,
            bahan = listOf(BahanResep("bayam", 400, "gram", KodeGrade.G2_S, "sayur")),
            menit = menit, estimasiBiaya = biaya, alergen = alergen, alat = listOf("kompor"),
        )

    private val harga = mapOf("bayam" to HargaBahan("gram", 100, 1000))
    private val profil = ProfilRekomendasi(budgetPerMakan = 35000, levelPedas = 3, alatMasak = listOf("kompor"))
    private val stok = listOf(StokItem("bayam", 400, "gram", KodeGrade.G2_S, kedaluwarsaHari = 1))

    @Test
    fun jiwaDiLuarRentangMengembalikanGalat() {
        val e = RekomendasiEngine(BankResep(resep = listOf(resep("R-01", "tumis"))), harga)
        for (jiwa in listOf(0, 9)) {
            val h = e.rekomendasikan(profil, stok, jiwa)
            assertEquals(KodeGalat.JIWA_DI_LUAR_RENTANG, h.galat)
            assertTrue(h.rekomendasi.isEmpty())
        }
    }

    @Test
    fun diversifikasiMaksDuaSekelompokDanMaksLima() {
        val bank = BankResep(resep = (1..7).map { resep("R-0$it", "tumis") } + resep("R-08", "kuah"))
        val h = RekomendasiEngine(bank, harga).rekomendasikan(profil, stok, 4)
        assertNull(h.galat)
        assertEquals(3, h.rekomendasi.size)
        assertEquals(2, h.rekomendasi.count { it.kelompok == "tumis" })
    }

    @Test
    fun alergiMenyaringResep() {
        val bank = BankResep(resep = listOf(resep("R-01", "tumis", alergen = listOf("telur")), resep("R-02", "kuah")))
        val h = RekomendasiEngine(bank, harga).rekomendasikan(profil.copy(alergi = listOf("telur")), stok, 4)
        assertEquals(listOf("R-02"), h.rekomendasi.map { it.kode })
    }

    @Test
    fun bonusG2SKedaluwarsaDuaHariDibatasiLimapuluh() {
        val bank = BankResep(resep = listOf(resep("R-01", "tumis")))
        val h = RekomendasiEngine(bank, harga).rekomendasikan(profil, stok, 4)
        assertEquals(50.0, h.rekomendasi.single().skorSerapan)
    }

    @Test
    fun stokKosongModeHemat() {
        val bank = BankResep(resep = listOf(resep("R-01", "tumis", biaya = 30000), resep("R-02", "kuah", biaya = 20000)))
        val h = RekomendasiEngine(bank, harga).rekomendasikan(profil, emptyList(), 4)
        assertTrue(h.modeTanpaStok)
        assertEquals(listOf("R-02", "R-01"), h.rekomendasi.map { it.kode })
        assertEquals(0.0, h.rekomendasi.first().skorTotal)
    }

    @Test
    fun belanjaMelebihiBudgetDibuang() {
        val bank = BankResep(resep = listOf(resep("R-01", "tumis")))
        val h = RekomendasiEngine(bank, harga).rekomendasikan(profil.copy(budgetPerMakan = 100), listOf(StokItem("x", 1, "gram", KodeGrade.NON_G2)), 4)
        assertTrue(h.rekomendasi.isEmpty())
    }
}
