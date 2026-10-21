package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.profil.DokumenProfil
import id.chefgenie.pawonee.app.domain.ProfilKeluarga
import kotlin.test.Test
import kotlin.test.assertEquals

class DokumenProfilTest {
    private fun p(jiwa: Int) = ProfilKeluarga(jiwa = jiwa)

    @Test
    fun satuAktifMaksEmpatCadangan() {
        var d = DokumenProfil(aktif = p(1))
        for (jiwa in 2..8) d = d.jadikanAktif(p(jiwa))
        assertEquals(8, d.aktif.jiwa)
        assertEquals(4, d.cadangan.size)
        assertEquals(listOf(7, 6, 5, 4), d.cadangan.map { it.jiwa })
    }

    @Test
    fun profilSamaTidakMenggandakan() {
        val d = DokumenProfil(aktif = p(3)).jadikanAktif(p(3))
        assertEquals(emptyList(), d.cadangan)
    }

    @Test
    fun kembaliKeProfilCadanganTidakDuplikat() {
        val d = DokumenProfil(aktif = p(1)).jadikanAktif(p(2)).jadikanAktif(p(1))
        assertEquals(1, d.aktif.jiwa)
        assertEquals(listOf(2), d.cadangan.map { it.jiwa })
    }

    @Test
    fun pulihkanMenukarAktifDenganCadangan() {
        val d = DokumenProfil(aktif = p(1)).jadikanAktif(p(2)).jadikanAktif(p(3)).pulihkan(1)
        assertEquals(1, d.aktif.jiwa)
        assertEquals(listOf(3, 2), d.cadangan.map { it.jiwa })
    }
}
