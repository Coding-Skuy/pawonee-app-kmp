package id.chefgenie.pawonee.app

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import id.chefgenie.pawonee.app.data.cache.CacheLokal
import id.chefgenie.pawonee.app.data.cache.KebijakanCache
import id.chefgenie.pawonee.app.data.net.ApiPawonee
import id.chefgenie.pawonee.app.data.net.Sesi
import id.chefgenie.pawonee.app.data.sinkron.HasilSinkron
import id.chefgenie.pawonee.app.data.sinkron.SinkronService
import id.chefgenie.pawonee.app.data.sinkron.StokRepository
import id.chefgenie.pawonee.app.db.PawoneeDb
import id.chefgenie.pawonee.app.kerja.HasilTugasPagi
import id.chefgenie.pawonee.app.kerja.TugasPagi
import id.chefgenie.pawonee.pantryresep.KodeGrade
import id.chefgenie.pawonee.pantryresep.StokItem
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class CacheDanSinkronTest {
    private val jam = JamPalsu()
    private val db = PawoneeDb(JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).also { PawoneeDb.Schema.create(it) })
    private val cache = CacheLokal(db, jam)
    private val json = headersOf(HttpHeaders.ContentType, "application/json")

    private fun stok(nama: String, hari: Int, g: KodeGrade = KodeGrade.G2_S) = StokItem(nama, 300, "gram", g, hari)

    private fun layanan(client: HttpClient): Pair<SinkronService, StokRepository> {
        val repo = StokRepository(cache)
        val api = ApiPawonee(client, "http://x", { Sesi("KEL-0001", "t") })
        return SinkronService(api, cache, repo, jam) to repo
    }

    private val stokJson = """{"data":[
        {"namaBahan":"bayam","gram":400,"kedaluwarsaHari":1,"kodeGrade":"G2-S"},
        {"namaBahan":"kangkung","gram":300,"kedaluwarsaHari":2,"kodeGrade":"G2-S"},
        {"namaBahan":"kentang","gram":600,"kedaluwarsaHari":5,"kodeGrade":"G2-U"}]}"""

    @Test
    fun stokSegarSelama24JamLaluKedaluwarsa() {
        cache.simpanStok(listOf(stok("bayam", 3)))
        jam.sekarang += KebijakanCache.TTL_STOK_MS - 1
        assertEquals(1, cache.bacaStokSegar().size)
        jam.sekarang += 1
        assertTrue(cache.bacaStokSegar().isEmpty())
    }

    @Test
    fun resepSegarSelama7HariLaluKedaluwarsa() {
        cache.simpanResep(mapOf("R-01" to "{}", "R-02" to "{}"))
        jam.sekarang += KebijakanCache.TTL_RESEP_MS - 1
        assertEquals(setOf("R-01", "R-02"), cache.bacaResepSegar()?.keys)
        jam.sekarang += 1
        assertNull(cache.bacaResepSegar())
    }

    @Test
    fun sinkronBerhasilMengisiCacheDanMenurunkanBanner() = runTest {
        val (svc, repo) = layanan(HttpClient(MockEngine { respond(stokJson, HttpStatusCode.OK, json) }))
        assertTrue(svc.bannerLuring.value, "belum pernah sinkron -> banner")
        val hasil = svc.sinkronStok()
        assertEquals(HasilSinkron.Berhasil(3), hasil)
        assertEquals(3, repo.stok.value.size)
        assertFalse(svc.bannerLuring.value)
    }

    @Test
    fun gagalSinkronMempertahankanCacheLamaDanBannerMuncul() = runTest {
        cache.simpanStok(listOf(stok("bayam", 3)))
        val (svc, repo) = layanan(HttpClient(MockEngine { respondError(HttpStatusCode.InternalServerError) }))
        jam.majuJam(25)
        // stok 25 jam lalu: tidak dilayani, banner muncul
        val hasil = svc.sinkronStok()
        assertTrue(hasil is HasilSinkron.Gagal)
        assertTrue(svc.bannerLuring.value)
        assertTrue(repo.stok.value.isEmpty())
    }

    @Test
    fun sinkronResepMenyimpanDuaBelasResepLaluTidakMengulangSelamaSegar() = runTest {
        var panggilan = 0
        val resep = (1..12).joinToString(",", "{\"data\":[", "]}") { """{"id":"R-%02d","nama":"x"}""".format(it) }
        val (svc, _) = layanan(HttpClient(MockEngine { panggilan++; respond(resep, HttpStatusCode.OK, json) }))
        assertEquals(HasilSinkron.Berhasil(12), svc.sinkronResep())
        svc.sinkronResep()
        assertEquals(1, panggilan)
        assertEquals(12, cache.bacaResepSegar()?.size)
    }

    @Test
    fun tugasPagiNotifikasiStokKedaluwarsaMaks1HariDanBahanLayu() = runTest {
        val (svc, _) = layanan(HttpClient(MockEngine { req ->
            if (req.url.encodedPath == "/stok") respond(stokJson, HttpStatusCode.OK, json) else respond("""{"data":[]}""", HttpStatusCode.OK, json)
        }))
        var diberi: List<String>? = null
        val hasil = TugasPagi(svc, cache) { diberi = it }.jalankan()
        assertEquals(HasilTugasPagi.Selesai(listOf("bayam", "kangkung"), listOf("bayam")), hasil)
        assertEquals(listOf("bayam"), diberi)
    }

    @Test
    fun tugasPagiTanpaStokMendesakTidakNotifikasi() = runTest {
        val aman = """{"data":[{"namaBahan":"kentang","gram":600,"kedaluwarsaHari":5,"kodeGrade":"G2-U"}]}"""
        val (svc, _) = layanan(HttpClient(MockEngine { req ->
            if (req.url.encodedPath == "/stok") respond(aman, HttpStatusCode.OK, json) else respond("""{"data":[]}""", HttpStatusCode.OK, json)
        }))
        var dipanggil = false
        TugasPagi(svc, cache) { dipanggil = true }.jalankan()
        assertFalse(dipanggil)
    }

    @Test
    fun tugasPagiGagalSinkronMemintaUlang() = runTest {
        val (svc, _) = layanan(HttpClient(MockEngine { respondError(HttpStatusCode.ServiceUnavailable) }))
        assertEquals(HasilTugasPagi.Ulangi, TugasPagi(svc, cache) { }.jalankan())
    }
}
