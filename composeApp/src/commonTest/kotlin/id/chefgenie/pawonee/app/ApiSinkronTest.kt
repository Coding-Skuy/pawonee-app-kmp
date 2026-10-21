package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.net.ApiPawonee
import id.chefgenie.pawonee.app.data.net.GalatApi
import id.chefgenie.pawonee.app.data.net.Sesi
import id.chefgenie.pawonee.app.data.net.SesiKosong
import id.chefgenie.pawonee.pantryresep.KodeGrade
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondError
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class ApiSinkronTest {
    private val json = headersOf(HttpHeaders.ContentType, "application/json")
    private val sesi = { Sesi("KEL-0001", "token-uji") }

    private val stokJson = """{"data":[
        {"namaBahan":"bayam","gram":400,"kedaluwarsaHari":1,"kodeGrade":"G2-S"},
        {"namaBahan":"kentang","gram":600,"kedaluwarsaHari":5,"kodeGrade":"G2-U"}]}"""

    @Test
    fun bacaStokMengirimHeaderDanParameter() = runTest {
        var url = ""
        var kontrak: String? = null
        var auth: String? = null
        val client = HttpClient(MockEngine { req ->
            url = req.url.toString()
            kontrak = req.headers["X-Kontrak-Versi"]
            auth = req.headers[HttpHeaders.Authorization]
            respond(stokJson, HttpStatusCode.OK, json)
        })
        val hasil = ApiPawonee(client, "http://x", { sesi() }).bacaStok()
        assertEquals("http://x/stok?idKeluarga=KEL-0001", url)
        assertEquals("v1", kontrak)
        assertEquals("Bearer token-uji", auth)
        assertEquals(2, hasil.stok.size)
        assertEquals(KodeGrade.G2_S, hasil.stok.first().kodeGrade)
        assertEquals(false, hasil.usang)
    }

    @Test
    fun headerDataUsangDiteruskan() = runTest {
        val client = HttpClient(MockEngine {
            respond(stokJson, HttpStatusCode.OK, headersOf(HttpHeaders.ContentType to listOf("application/json"), "X-Data-Usang" to listOf("true")))
        })
        assertTrue(ApiPawonee(client, "http://x", { sesi() }).bacaStok().usang)
    }

    @Test
    fun tanpaSesiBelumMasuk() = runTest {
        val client = HttpClient(MockEngine { respond("{}", HttpStatusCode.OK, json) })
        assertFailsWith<GalatApi.BelumMasuk> { ApiPawonee(client, "http://x", SesiKosong).bacaStok() }
    }

    @Test
    fun status401MenjadiGalatHttp() = runTest {
        val client = HttpClient(MockEngine { respondError(HttpStatusCode.Unauthorized) })
        val e = assertFailsWith<GalatApi.Http> { ApiPawonee(client, "http://x", { sesi() }).bacaStok() }
        assertEquals(401, e.status)
    }

    @Test
    fun jaringanMatiMenjadiGalatJaringan() = runTest {
        val client = HttpClient(MockEngine { throw RuntimeException("tidak ada jaringan") })
        assertFailsWith<GalatApi.Jaringan> { ApiPawonee(client, "http://x", { sesi() }).daftarResep() }
    }
}
