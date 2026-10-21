package id.chefgenie.pawonee.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import id.chefgenie.pawonee.app.data.BankBawaan
import id.chefgenie.pawonee.app.domain.ProfilStore
import id.chefgenie.pawonee.app.domain.RekomendasiHolder
import id.chefgenie.pawonee.app.domain.SumberStok
import id.chefgenie.pawonee.app.domain.StokMemori
import id.chefgenie.pawonee.app.nav.NavigasiState
import id.chefgenie.pawonee.app.nav.Rute
import id.chefgenie.pawonee.pantryresep.RekomendasiEngine

/**
 * Akar aplikasi: satu graf Navigation3 berisi enam layar. [deepLinkAwal] diisi platform
 * (mis. `pawonee://resep/telur-balado?jiwa=4` dari intent Android).
 */
@Composable
fun App(
    deepLinkAwal: String? = null,
    profilStore: ProfilStore = remember { ProfilStore() },
    sumberStok: SumberStok = remember { StokMemori() },
) {
    val scope = rememberCoroutineScope()
    val bank = BankBawaan.bank
    val holder = remember(profilStore, sumberStok) {
        RekomendasiHolder(RekomendasiEngine(bank, BankBawaan.harga), profilStore, sumberStok, scope)
    }
    val navigasi = remember { NavigasiState() }

    LaunchedEffect(deepLinkAwal) {
        if (deepLinkAwal != null) navigasi.bukaDeepLink(deepLinkAwal, profilStore.profil.value.jiwa)
    }

    MaterialTheme {
        NavDisplay(
            backStack = navigasi.tumpukan,
            onBack = { navigasi.kembali() },
            entryProvider = entryProvider<Rute> {
                entry<Rute.Beranda> {
                    LayarBeranda(profilStore, holder) { tujuan ->
                        navigasi.buka(
                            when (tujuan) {
                                Tujuan.Rekomendasi -> Rute.Rekomendasi
                                Tujuan.Stok -> Rute.Stok
                                Tujuan.Preferensi -> Rute.Preferensi
                                Tujuan.Riwayat -> Rute.Riwayat
                            },
                        )
                    }
                }
                entry<Rute.Stok> { LayarStok(sumberStok) { navigasi.kembali() } }
                entry<Rute.Rekomendasi> {
                    LayarRekomendasi(
                        holder,
                        bukaDetail = { _, id -> navigasi.buka(Rute.ResepDetail(id, profilStore.profil.value.jiwa)) },
                        kembali = { navigasi.kembali() },
                    )
                }
                entry<Rute.ResepDetail> { rute ->
                    LayarResepDetail(rute.id, rute.jiwa, bank) { navigasi.kembali() }
                }
                entry<Rute.Preferensi> { LayarPreferensi(profilStore) { navigasi.kembali() } }
                entry<Rute.Riwayat> { LayarRiwayat { navigasi.kembali() } }
            },
        )
    }
}
