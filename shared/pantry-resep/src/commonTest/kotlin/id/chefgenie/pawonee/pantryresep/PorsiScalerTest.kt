package id.chefgenie.pawonee.pantryresep

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PorsiScalerTest {
    @Test
    fun faktorBahanBr105() {
        val harapan = mapOf(1 to 30, 2 to 55, 3 to 80, 4 to 100, 5 to 125, 6 to 150, 8 to 200)
        for ((jiwa, persen) in harapan) assertEquals(persen, PorsiScaler.faktorBahanPersen(jiwa), "jiwa=$jiwa")
    }

    @Test
    fun tujuhJiwaDitandaiAsumsi() {
        assertEquals(setOf(7), PorsiScaler.jiwaAsumsi)
        assertEquals(175, PorsiScaler.faktorBahanPersen(7))
    }

    @Test
    fun jiwaDiLuarRentangDitolak() {
        for (jiwa in listOf(0, 9, -1, 100)) {
            val e = assertFailsWith<JiwaDiLuarRentangException> { PorsiScaler.skala(100, "protein", jiwa) }
            assertEquals(KodeGalat.JIWA_DI_LUAR_RENTANG, e.kode)
        }
    }

    @Test
    fun sayurDibulatkanKeAtasKelipatan50() {
        // 400 g * 30% = 120 -> 150
        assertEquals(150, PorsiScaler.skala(400, "sayur", 1))
        // 400 g * 125% = 500 -> tetap 500
        assertEquals(500, PorsiScaler.skala(400, "sayur", 5))
        assertEquals(50, PorsiScaler.skala(1, "sayur", 1))
    }

    @Test
    fun telurButirGenap() {
        // 4 butir * 55% = 2,2 -> 4 (kelipatan 2 ke atas)
        assertEquals(4, PorsiScaler.skala(4, "telur", 2))
        // 4 butir * 30% = 1,2 -> 2
        assertEquals(2, PorsiScaler.skala(4, "telur", 1))
        assertEquals(8, PorsiScaler.skala(4, "telur", 8))
    }

    @Test
    fun santanKelipatan200() {
        // 400 ml * 85% (faktor air 3 jiwa) = 340 -> 400
        assertEquals(400, PorsiScaler.skala(400, "santan", 3))
        // 400 ml * 35% = 140 -> 200
        assertEquals(200, PorsiScaler.skala(400, "santan", 1))
    }

    @Test
    fun bumbuDanAirMemakaiFaktorSendiri() {
        // 4 g garam (bumbu) 1 jiwa: 35% -> ceil(1,4) = 2
        assertEquals(2, PorsiScaler.skala(4, "bumbu", 1))
        // 600 ml air 1 jiwa: 35% = 210
        assertEquals(210, PorsiScaler.skala(600, "cair", 1))
    }

    @Test
    fun empatJiwaIdentitas() {
        for (jenis in listOf("protein", "sayur", "bumbu", "cair", "telur", "santan")) {
            val jumlah = if (jenis == "sayur") 400 else if (jenis == "santan") 400 else 4
            assertEquals(jumlah, PorsiScaler.skala(jumlah, jenis, 4), jenis)
        }
    }
}
