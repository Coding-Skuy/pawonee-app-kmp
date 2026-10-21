package id.chefgenie.pawonee.app.data.net

/** Sesi pengguna: idKeluarga (`KEL-0001`) dan JWT akses audiens `pawonee`. Alur login belum bagian tiket ini. */
data class Sesi(val idKeluarga: String, val tokenAkses: String)

fun interface SesiProvider {
    suspend fun sesi(): Sesi?
}

/** Belum masuk: seluruh sinkron gagal dengan [GalatApi.BelumMasuk] dan aplikasi tetap berjalan dari cache. */
object SesiKosong : SesiProvider {
    override suspend fun sesi(): Sesi? = null
}
