package id.chefgenie.pawonee.app.data.sinkron

import id.chefgenie.pawonee.app.data.cache.CacheLokal
import id.chefgenie.pawonee.app.data.cache.Jam
import id.chefgenie.pawonee.app.data.cache.KebijakanCache
import id.chefgenie.pawonee.app.data.net.ApiPawonee
import id.chefgenie.pawonee.app.data.net.GalatApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.jsonPrimitive

sealed interface HasilSinkron {
    data class Berhasil(val jumlah: Int, val dataUsang: Boolean = false) : HasilSinkron
    data class Gagal(val penyebab: GalatApi) : HasilSinkron
}

/**
 * Sinkron API -> cache. Gagal jaringan/401 tidak menghapus cache lama: aplikasi tetap jalan luring.
 * [bannerLuring] menyala bila sinkron stok terakhir > 24 jam lalu (atau belum pernah).
 */
class SinkronService(
    private val api: ApiPawonee,
    private val cache: CacheLokal,
    private val stokRepo: StokRepository,
    private val jam: Jam,
) {
    private val _bannerLuring = MutableStateFlow(KebijakanCache.perluBannerLuring(cache.stokSinkronTerakhirMs(), jam.sekarangMs()))
    val bannerLuring: StateFlow<Boolean> = _bannerLuring.asStateFlow()

    fun perbaruiStatus() {
        _bannerLuring.value = KebijakanCache.perluBannerLuring(cache.stokSinkronTerakhirMs(), jam.sekarangMs())
    }

    suspend fun sinkronStok(): HasilSinkron = try {
        val hasil = api.bacaStok()
        cache.simpanStok(hasil.stok)
        stokRepo.segarkan()
        perbaruiStatus()
        HasilSinkron.Berhasil(hasil.stok.size, hasil.usang)
    } catch (e: GalatApi) {
        // Cache lama dipertahankan di disk, tetapi entri yang sudah lewat 24 jam tidak boleh tetap dilayani.
        stokRepo.segarkan()
        perbaruiStatus()
        HasilSinkron.Gagal(e)
    }

    /** Sinkron resep hanya bila cache 7 hari habis (atau [paksa]). */
    suspend fun sinkronResep(paksa: Boolean = false): HasilSinkron {
        if (!paksa && cache.bacaResepSegar() != null) return HasilSinkron.Berhasil(0)
        return try {
            val resep = api.daftarResep()
            cache.simpanResep(resep.associate { it.getValue("id").jsonPrimitive.content to it.toString() })
            HasilSinkron.Berhasil(resep.size)
        } catch (e: GalatApi) {
            HasilSinkron.Gagal(e)
        }
    }
}
