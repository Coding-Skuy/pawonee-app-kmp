package id.chefgenie.pawonee.app.domain

import id.chefgenie.pawonee.pantryresep.StokItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Sumber stok dapur (dibaca saja; stok milik Pedaree, lihat AGENTS.md). Cache + sinkron di PWN-KMP-004. */
interface SumberStok {
    val stok: StateFlow<List<StokItem>>
}

/** Implementasi memori untuk tahap navigasi; diganti repositori ber-cache di tiket luring-dahulu. */
class StokMemori(awal: List<StokItem> = emptyList()) : SumberStok {
    private val _stok = MutableStateFlow(awal)
    override val stok: StateFlow<List<StokItem>> = _stok.asStateFlow()

    fun atur(daftar: List<StokItem>) {
        _stok.value = daftar
    }
}
