package id.chefgenie.pawonee.pantryresep

import java.io.File
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Uji paritas 32 kasus (PWN-AI-004 / README fixtures di pawonee-ai-models). Berkas fixture dan bank
 * disalin apa adanya dan tidak boleh disunting; selisih skor harus < ambangSelisih (0,01).
 */
class ParitasFixtureTest {
    private val dir = File("src/commonTest/fixtures")
    private val fixtureTeks = File(dir, "kasus-uji.json").readText()
    private val bankTeks = File(dir, "bank_resep_grade2.json").readText()
    private val fixture: JsonObject = Json.parseToJsonElement(fixtureTeks).jsonObject
    private val json = Json { ignoreUnknownKeys = true }

    private val bank = BankLoader.bankDariJson(bankTeks)
    private val harga: Map<String, HargaBahan> = json.decodeFromJsonElement(
        MapSerializer(String.serializer(), HargaBahan.serializer()),
        fixture.getValue("konfigurasi").jsonObject.getValue("harga"),
    )
    private val engine = RekomendasiEngine(bank, harga)
    private val ambang = fixture.getValue("ambangSelisih").jsonPrimitive.doubleOrNull!!
    private val kasus = fixture.getValue("kasus").jsonArray

    @Test
    fun bankSepaketDenganFixture() {
        val sha = MessageDigest.getInstance("SHA-256").digest(bankTeks.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
        assertEquals(fixture.getValue("bankSha256").jsonPrimitive.content, sha)
    }

    @Test
    fun tigaPuluhDuaKasus() {
        assertEquals(32, kasus.size)
        assertEquals(0.01, ambang)
    }

    @Test
    fun semuaKasusParitasDenganKotlin() {
        val gagal = mutableListOf<String>()
        for (k in kasus) {
            val o = k.jsonObject
            val id = o.getValue("id").jsonPrimitive.content
            runCatching { cocokkan(id, o) }.onFailure { gagal += "$id: ${it.message}" }
        }
        assertTrue(gagal.isEmpty(), "Kasus gagal (${gagal.size}/32):\n" + gagal.joinToString("\n"))
    }

    private fun cocokkan(id: String, kasus: JsonObject) {
        val m = kasus.getValue("masukan").jsonObject
        val profil = json.decodeFromJsonElement(ProfilRekomendasi.serializer(), m.getValue("profil"))
        val stok = json.decodeFromJsonElement(
            ListSerializer(StokItem.serializer()),
            m.getValue("stok"),
        )
        val jiwa = m.getValue("jiwa").jsonPrimitive.int
        val waktu = m.getValue("waktuMaksimalMenit").jsonPrimitive.int
        val hasil = engine.rekomendasikan(profil, stok, jiwa, waktu)

        val h = kasus.getValue("harapan").jsonObject
        assertEquals(h["galat"].teksAtauNull(), hasil.galat?.name, "galat")
        assertEquals(h.getValue("modeTanpaStok").jsonPrimitive.booleanOrNull, hasil.modeTanpaStok, "modeTanpaStok")
        val harapan = h.getValue("rekomendasi").jsonArray
        assertEquals(
            harapan.map { it.jsonObject.getValue("kode").jsonPrimitive.content },
            hasil.rekomendasi.map { it.kode },
            "daftar kode berurutan",
        )
        harapan.forEachIndexed { i, e ->
            val eo = e.jsonObject
            val r = hasil.rekomendasi[i]
            fun skor(nama: String, nilai: Double) {
                val sel = abs(eo.getValue(nama).jsonPrimitive.doubleOrNull!! - nilai)
                assertTrue(sel < ambang, "${r.kode} $nama selisih $sel")
            }
            skor("skorTotal", r.skorTotal)
            skor("skorSerapan", r.skorSerapan)
            skor("skorKecocokanStok", r.skorKecocokanStok)
            skor("skorBudget", r.skorBudget.toDouble())
            skor("skorWaktu", r.skorWaktu.toDouble())
            assertEquals(eo.getValue("belanjaTambahanRp").jsonPrimitive.int, r.belanjaTambahanRp, "${r.kode} belanjaRp")
            assertEquals(
                eo.getValue("belanjaTambahan").jsonArray.map {
                    val b = it.jsonObject
                    BelanjaItem(
                        b.getValue("nama").jsonPrimitive.content,
                        b.getValue("jumlah").jsonPrimitive.int,
                        b.getValue("satuan").jsonPrimitive.content,
                        b.getValue("rupiah").jsonPrimitive.int,
                    )
                },
                r.belanjaTambahan,
                "${r.kode} belanjaTambahan",
            )
            assertEquals(
                eo.getValue("bahanTerskala").jsonArray.map {
                    val b = it.jsonObject
                    BahanTerskala(
                        b.getValue("nama").jsonPrimitive.content,
                        b.getValue("jumlah").jsonPrimitive.int,
                        b.getValue("satuan").jsonPrimitive.content,
                    )
                },
                r.bahanTerskala,
                "${r.kode} bahanTerskala",
            )
        }
    }

    private fun JsonElement?.teksAtauNull(): String? = when (this) {
        null, JsonNull -> null
        is JsonPrimitive -> contentOrNull
        is JsonArray, is JsonObject -> error("tak terduga")
    }
}
