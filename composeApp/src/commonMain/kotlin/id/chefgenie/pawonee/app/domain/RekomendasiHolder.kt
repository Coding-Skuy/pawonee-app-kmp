package id.chefgenie.pawonee.app.domain

import id.chefgenie.pawonee.pantryresep.HasilRekomendasi
import id.chefgenie.pawonee.pantryresep.RekomendasiEngine
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Menghitung ulang rekomendasi otomatis tiap profil atau stok berubah. Rumus ada di
 * `:shared:pantry-resep`; kelas ini hanya merangkai aliran.
 */
class RekomendasiHolder(
    private val engine: RekomendasiEngine,
    profilStore: ProfilStore,
    sumberStok: SumberStok,
    scope: CoroutineScope,
    dispatcherHitung: CoroutineDispatcher = Dispatchers.Default,
) {
    val hasil: StateFlow<HasilRekomendasi> =
        combine(profilStore.profil, sumberStok.stok) { profil, stok ->
            engine.rekomendasikan(profil.keProfilRekomendasi(), stok, profil.jiwa, profil.waktuMaksimalMenit)
        }
            .flowOn(dispatcherHitung)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                engine.rekomendasikan(
                    profilStore.profil.value.keProfilRekomendasi(),
                    sumberStok.stok.value,
                    profilStore.profil.value.jiwa,
                    profilStore.profil.value.waktuMaksimalMenit,
                ),
            )
}
