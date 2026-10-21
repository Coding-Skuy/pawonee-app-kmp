package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.profil.DokumenProfil
import id.chefgenie.pawonee.app.data.profil.ProfilRepository
import id.chefgenie.pawonee.app.data.profil.buatDataStoreProfil
import id.chefgenie.pawonee.app.domain.ProfilStore
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext

class ProfilDataStoreTest {
    private fun direktori(): File = File.createTempFile("profil", "dir").also { it.delete(); it.mkdirs() }

    /** DataStore menulis di dispatcher IO sendiri, jadi uji menunggu dengan waktu nyata. */
    private suspend fun tunggu(kondisi: suspend () -> Boolean) = withTimeout(10_000) {
        while (!kondisi()) delay(20)
    }

    @Test
    fun profilBertahanSetelahAplikasiDibukaUlang() = runBlocking(Dispatchers.Default) {
        val dir = direktori()
        val store1 = ProfilStore()
        val scope1 = CoroutineScope(Dispatchers.Default + SupervisorJob())
        val repo1 = ProfilRepository(buatDataStoreProfil(dir.path, scope1), store1)
        repo1.mulai(this)
        store1.atur(6)
        tunggu { File(dir, DokumenProfil.NAMA_BERKAS).exists() && repo1.dokumen().aktif.jiwa == 6 }
        assertTrue(File(dir, DokumenProfil.NAMA_BERKAS).readText().contains("\"jiwa\": 6"))

        scope1.cancel() // DataStore hanya boleh satu instans aktif per berkas
        val store2 = ProfilStore()
        ProfilRepository(buatDataStoreProfil(dir.path), store2).mulai(this)
        assertEquals(6, store2.profil.value.jiwa)
        coroutineContext.cancelChildren()
    }

    @Test
    fun perubahanBerulangMenghasilkanSatuAktifDanMaksEmpatCadangan() = runBlocking(Dispatchers.Default) {
        val dir = direktori()
        val store = ProfilStore()
        val repo = ProfilRepository(buatDataStoreProfil(dir.path), store)
        repo.mulai(this)
        for (j in listOf(1, 2, 3, 5, 6, 7, 8)) {
            store.atur(j)
            tunggu { repo.dokumen().aktif.jiwa == j }
        }
        val dok = repo.dokumen()
        assertEquals(8, dok.aktif.jiwa)
        assertEquals(listOf(7, 6, 5, 3), dok.cadangan.map { it.jiwa })
        coroutineContext.cancelChildren()
    }
}
