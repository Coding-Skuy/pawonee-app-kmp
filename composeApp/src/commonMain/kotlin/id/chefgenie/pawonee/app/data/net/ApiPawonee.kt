package id.chefgenie.pawonee.app.data.net

import id.chefgenie.pawonee.pantryresep.KodeGrade
import id.chefgenie.pawonee.pantryresep.StokItem
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Kegagalan memanggil API Pawonee. Aplikasi tidak pernah crash karenanya: ia jatuh ke cache. */
sealed class GalatApi(pesan: String, cause: Throwable? = null) : Exception(pesan, cause) {
    data object BelumMasuk : GalatApi("Belum masuk (tanpa token akses)")
    class Http(val status: Int) : GalatApi("HTTP $status")
    class Jaringan(cause: Throwable) : GalatApi("Jaringan tidak tersedia: ${cause.message}", cause)
    class Format(cause: Throwable) : GalatApi("Format respons tak dikenal: ${cause.message}", cause)
}

data class HasilStokApi(val stok: List<StokItem>, val usang: Boolean)

/**
 * Klien REST ringan untuk openapi.yaml v1.0.0: GET /resep (cache 7 hari) dan GET /stok (cache 24 jam).
 * Semua permintaan membawa `X-Kontrak-Versi: v1`.
 */
class ApiPawonee(
    private val client: HttpClient,
    private val baseUrl: String,
    private val sesi: SesiProvider,
) {
    private val json = Json { ignoreUnknownKeys = true }

    /** 12 resep bank, apa adanya (JSON per resep) agar skema server dapat berevolusi tanpa migrasi cache. */
    suspend fun daftarResep(): List<JsonObject> {
        val badan = ambil("/resep", idKeluarga = null).first
        return runCatching { json.parseToJsonElement(badan).jsonObject.getValue("data").jsonArray.map { it.jsonObject } }
            .getOrElse { throw GalatApi.Format(it) }
    }

    suspend fun bacaStok(): HasilStokApi {
        val (badan, usang) = ambil("/stok", idKeluarga = true)
        val stok = runCatching {
            json.parseToJsonElement(badan).jsonObject.getValue("data").jsonArray.map(::keStok)
        }.getOrElse { throw GalatApi.Format(it) }
        return HasilStokApi(stok, usang)
    }

    private fun keStok(e: kotlinx.serialization.json.JsonElement): StokItem {
        val o = e.jsonObject
        val grade = o.getValue("kodeGrade").jsonPrimitive.content
        return StokItem(
            nama = o.getValue("namaBahan").jsonPrimitive.content,
            jumlah = o.getValue("gram").jsonPrimitive.int,
            satuan = "gram",
            kodeGrade = KodeGrade.entries.first { it.kode == grade },
            kedaluwarsaHari = o.getValue("kedaluwarsaHari").jsonPrimitive.int,
        )
    }

    private suspend fun ambil(jalur: String, idKeluarga: Boolean?): Pair<String, Boolean> {
        val s = sesi.sesi() ?: throw GalatApi.BelumMasuk
        val respons: HttpResponse = try {
            client.get(baseUrl.trimEnd('/') + jalur) {
                header("X-Kontrak-Versi", "v1")
                bearerAuth(s.tokenAkses)
                if (idKeluarga == true) parameter("idKeluarga", s.idKeluarga)
            }
        } catch (e: Exception) {
            throw GalatApi.Jaringan(e)
        }
        if (respons.status.value !in 200..299) throw GalatApi.Http(respons.status.value)
        return respons.bodyAsText() to (respons.headers["X-Data-Usang"] == "true")
    }

    companion object {
        fun buatHttpClient(): HttpClient = HttpClient {
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }
    }
}
