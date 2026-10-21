package id.chefgenie.pawonee.app.data.profil

import androidx.datastore.core.DataStore
import id.chefgenie.pawonee.app.domain.ProfilStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Menyambungkan [ProfilStore] (StateFlow) ke DataStore: profil aktif dimuat sekali saat mulai, lalu setiap
 * perubahan ditulis ulang (profil lama masuk cadangan, maksimal 4).
 */
class ProfilRepository(
    private val dataStore: DataStore<DokumenProfil>,
    val store: ProfilStore,
) {
    /** Muat profil tersimpan ke [store] lalu mulai menyimpan perubahan berikutnya. Dipanggil sekali. */
    suspend fun mulai(scope: CoroutineScope) {
        val tersimpan = dataStore.data.first()
        store.perbarui { tersimpan.aktif }
        // Tanpa drop(1): perubahan yang terjadi sebelum kolektor aktif tidak boleh hilang (jadikanAktif idempoten).
        store.profil
            .onEach { baru -> dataStore.updateData { it.jadikanAktif(baru) } }
            .launchIn(scope)
    }

    suspend fun dokumen(): DokumenProfil = dataStore.data.first()

    /** Pulihkan salah satu cadangan menjadi profil aktif (juga memperbarui [store]). */
    suspend fun pulihkan(indeks: Int) {
        val baru = dataStore.updateData { it.pulihkan(indeks) }
        store.perbarui { baru.aktif }
    }
}
