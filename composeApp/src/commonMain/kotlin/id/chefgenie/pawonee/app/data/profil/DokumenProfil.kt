package id.chefgenie.pawonee.app.data.profil

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.OkioSerializer
import androidx.datastore.core.okio.OkioStorage
import id.chefgenie.pawonee.app.domain.ProfilKeluarga
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okio.BufferedSink
import okio.BufferedSource
import okio.FileSystem
import okio.Path.Companion.toPath

/**
 * Isi berkas `profil_keluarga.json` di DataStore: SATU profil aktif + maksimal [MAKS_CADANGAN] cadangan
 * (terbaru di depan). Profil disimpan selamanya (tidak ada TTL).
 */
@Serializable
data class DokumenProfil(
    val aktif: ProfilKeluarga = ProfilKeluarga(),
    val cadangan: List<ProfilKeluarga> = emptyList(),
) {
    /** Jadikan [baru] aktif; profil aktif lama masuk cadangan (tanpa duplikat), dipangkas ke [MAKS_CADANGAN]. */
    fun jadikanAktif(baru: ProfilKeluarga): DokumenProfil {
        if (baru == aktif) return this
        val daftar = (listOf(aktif) + cadangan).filter { it != baru }.take(MAKS_CADANGAN)
        return DokumenProfil(baru, daftar)
    }

    /** Pulihkan cadangan ke-[indeks] menjadi aktif; profil aktif lama menjadi cadangan paling baru. */
    fun pulihkan(indeks: Int): DokumenProfil {
        val dipilih = cadangan.getOrNull(indeks) ?: return this
        return DokumenProfil(dipilih, (listOf(aktif) + cadangan.filterIndexed { i, _ -> i != indeks }).take(MAKS_CADANGAN))
    }

    companion object {
        const val MAKS_CADANGAN = 4
        const val NAMA_BERKAS = "profil_keluarga.json"
    }
}

internal object DokumenProfilSerializer : OkioSerializer<DokumenProfil> {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
    }

    override val defaultValue: DokumenProfil = DokumenProfil()

    override suspend fun readFrom(source: BufferedSource): DokumenProfil =
        json.decodeFromString(DokumenProfil.serializer(), source.readUtf8())

    override suspend fun writeTo(t: DokumenProfil, sink: BufferedSink) {
        sink.writeUtf8(json.encodeToString(DokumenProfil.serializer(), t))
    }
}

/** DataStore berkas [DokumenProfil.NAMA_BERKAS] di dalam [direktori] (jalur tulis privat aplikasi). */
fun buatDataStoreProfil(
    direktori: String,
    scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
): DataStore<DokumenProfil> =
    DataStoreFactory.create(
        storage = OkioStorage(
            fileSystem = FileSystem.SYSTEM,
            serializer = DokumenProfilSerializer,
            producePath = { "$direktori/${DokumenProfil.NAMA_BERKAS}".toPath() },
        ),
        scope = scope,
    )
