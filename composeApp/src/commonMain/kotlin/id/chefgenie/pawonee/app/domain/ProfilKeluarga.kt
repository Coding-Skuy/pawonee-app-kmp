package id.chefgenie.pawonee.app.domain

import id.chefgenie.pawonee.pantryresep.PorsiScaler
import id.chefgenie.pawonee.pantryresep.ProfilRekomendasi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Profil keluarga aktif (BRD 20). `jiwa` 1-8; di luar rentang ditolak mesin dengan `JIWA_DI_LUAR_RENTANG`. */
data class ProfilKeluarga(
    val jiwa: Int = 4,
    val alergi: List<String> = emptyList(),
    val pantangan: List<String> = emptyList(),
    val budgetPerMakan: Int = 35000,
    val levelPedas: Int = 3,
    val alatMasak: List<String> = listOf("kompor", "wajan", "panci"),
    val waktuMaksimalMenit: Int = 30,
) {
    fun keProfilRekomendasi(): ProfilRekomendasi =
        ProfilRekomendasi(alergi, pantangan, budgetPerMakan, levelPedas, alatMasak)
}

/** Penyimpan profil aktif; perubahan disiarkan lewat [profil] dan memicu hitung ulang rekomendasi. */
class ProfilStore(awal: ProfilKeluarga = ProfilKeluarga()) {
    private val _profil = MutableStateFlow(awal)
    val profil: StateFlow<ProfilKeluarga> = _profil.asStateFlow()

    fun perbarui(ubah: (ProfilKeluarga) -> ProfilKeluarga) = _profil.update(ubah)

    fun atur(jiwa: Int) = perbarui { it.copy(jiwa = jiwa) }

    /** Naik/turun satu jiwa dalam rentang 1..8 (antarmuka pemilih tidak menghasilkan nilai tak sah). */
    fun geserJiwa(delta: Int) = perbarui {
        it.copy(jiwa = (it.jiwa + delta).coerceIn(PorsiScaler.JIWA_MIN, PorsiScaler.JIWA_MAKS))
    }
}
