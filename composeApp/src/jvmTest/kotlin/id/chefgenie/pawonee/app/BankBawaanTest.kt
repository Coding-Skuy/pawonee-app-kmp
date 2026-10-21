package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.BankBawaan
import id.chefgenie.pawonee.pantryresep.BankLoader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

/** Menjaga BankBawaan.kt (dibangkitkan) tetap sama dengan bank fixture hasil salinan. */
class BankBawaanTest {
    private val fixture = File("../shared/pantry-resep/src/commonTest/fixtures/bank_resep_grade2.json").readText()

    @Test
    fun bankBawaanSamaDenganFixture() {
        assertEquals(BankLoader.bankDariJson(fixture), BankBawaan.bank)
        assertEquals(12, BankBawaan.bank.resep.size)
    }

    @Test
    fun semuaBahanBankPunyaHarga() {
        val tanpaHarga = BankBawaan.bank.resep.flatMap { it.bahan }.map { it.nama }.toSet() - BankBawaan.harga.keys
        assertEquals(emptySet(), tanpaHarga)
    }
}
