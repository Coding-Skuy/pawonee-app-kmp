package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.BankBawaan
import id.chefgenie.pawonee.app.domain.ProfilStore
import id.chefgenie.pawonee.app.domain.RekomendasiHolder
import id.chefgenie.pawonee.app.domain.StokMemori
import id.chefgenie.pawonee.pantryresep.KodeGalat
import id.chefgenie.pawonee.pantryresep.KodeGrade
import id.chefgenie.pawonee.pantryresep.RekomendasiEngine
import id.chefgenie.pawonee.pantryresep.StokItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class RekomendasiHolderTest {
    private val stok = listOf(
        StokItem("tempe", 600, "gram", KodeGrade.NON_G2),
        StokItem("bawang merah", 300, "gram", KodeGrade.G2_U),
        StokItem("tomat", 400, "gram", KodeGrade.G2_V),
        StokItem("cabai merah", 100, "gram", KodeGrade.G2_V),
        StokItem("kecap manis", 100, "ml", KodeGrade.NON_G2),
    )

    @Test
    fun perubahanProfilMemicuHitungUlangOtomatis() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val profil = ProfilStore()
        val holder = RekomendasiHolder(
            RekomendasiEngine(BankBawaan.bank, BankBawaan.harga),
            profil, StokMemori(stok), backgroundScope, dispatcher,
        )
        val awal = holder.hasil.value
        assertNull(awal.galat)
        assertTrue(awal.rekomendasi.isNotEmpty())
        val belanjaAwal = awal.rekomendasi.first().bahanTerskala

        profil.atur(2)
        val duaJiwa = holder.hasil.value.rekomendasi.first().bahanTerskala
        assertTrue(belanjaAwal != duaJiwa, "takaran harus berubah saat jiwa berubah")

        profil.atur(9) // di luar rentang: ditolak mesin
        assertEquals(KodeGalat.JIWA_DI_LUAR_RENTANG, holder.hasil.value.galat)
    }

    @Test
    fun perubahanStokMemicuHitungUlang() = runTest {
        val dispatcher = UnconfinedTestDispatcher(testScheduler)
        val sumber = StokMemori(emptyList())
        val holder = RekomendasiHolder(
            RekomendasiEngine(BankBawaan.bank, BankBawaan.harga),
            ProfilStore(), sumber, backgroundScope, dispatcher,
        )
        assertTrue(holder.hasil.value.modeTanpaStok)
        sumber.atur(stok)
        assertEquals(false, holder.hasil.value.modeTanpaStok)
    }

    @Test
    fun geserJiwaTidakMelewatiRentang() {
        val p = ProfilStore()
        repeat(20) { p.geserJiwa(1) }
        assertEquals(8, p.profil.value.jiwa)
        repeat(20) { p.geserJiwa(-1) }
        assertEquals(1, p.profil.value.jiwa)
    }
}
