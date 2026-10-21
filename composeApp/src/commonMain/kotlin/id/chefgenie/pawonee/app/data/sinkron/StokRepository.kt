package id.chefgenie.pawonee.app.data.sinkron

import id.chefgenie.pawonee.app.data.cache.CacheLokal
import id.chefgenie.pawonee.app.domain.SumberStok
import id.chefgenie.pawonee.pantryresep.StokItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Stok dari cache lokal (24 jam). Stok yang sudah kedaluwarsa tidak pernah dilayani (mode hemat). */
class StokRepository(private val cache: CacheLokal) : SumberStok {
    private val _stok = MutableStateFlow(cache.bacaStokSegar())
    override val stok: StateFlow<List<StokItem>> = _stok.asStateFlow()

    /** Muat ulang dari cache (dipanggil setelah sinkron atau saat aplikasi dibuka kembali). */
    fun segarkan() {
        _stok.value = cache.bacaStokSegar()
    }
}
