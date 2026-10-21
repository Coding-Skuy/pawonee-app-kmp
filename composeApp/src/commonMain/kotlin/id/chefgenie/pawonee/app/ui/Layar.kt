package id.chefgenie.pawonee.app.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import id.chefgenie.pawonee.app.domain.ProfilStore
import id.chefgenie.pawonee.app.domain.RekomendasiHolder
import id.chefgenie.pawonee.app.domain.SumberStok
import id.chefgenie.pawonee.pantryresep.BankResep
import id.chefgenie.pawonee.pantryresep.KodeGalat
import id.chefgenie.pawonee.pantryresep.PorsiScaler

@Composable
private fun Kerangka(tag: String, judul: String, kembali: (() -> Unit)?, isi: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp).testTag(tag), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            if (kembali != null) {
                OutlinedButton(onClick = kembali, modifier = Modifier.testTag(Tag.KEMBALI)) { Text("Kembali") }
            }
            Text(judul, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 12.dp))
        }
        isi()
    }
}

@Composable
fun LayarBeranda(profilStore: ProfilStore, holder: RekomendasiHolder, nav: (Tujuan) -> Unit) {
    val profil by profilStore.profil.collectAsState()
    val hasil by holder.hasil.collectAsState()
    Kerangka(Tag.LAYAR_BERANDA, "Pawonee", null) {
        Text("Keluarga ${profil.jiwa} jiwa; ${hasil.rekomendasi.size} rekomendasi hari ini")
        Button(onClick = { nav(Tujuan.Rekomendasi) }, Modifier.fillMaxWidth().testTag(Tag.NAV_REKOMENDASI)) {
            Text("Rekomendasi masak")
        }
        Button(onClick = { nav(Tujuan.Stok) }, Modifier.fillMaxWidth().testTag(Tag.NAV_STOK)) { Text("Stok dapur") }
        Button(onClick = { nav(Tujuan.Preferensi) }, Modifier.fillMaxWidth().testTag(Tag.NAV_PREFERENSI)) {
            Text("Preferensi keluarga")
        }
        Button(onClick = { nav(Tujuan.Riwayat) }, Modifier.fillMaxWidth().testTag(Tag.NAV_RIWAYAT)) {
            Text("Riwayat serapan")
        }
    }
}

/** Tujuan dari beranda; dipisah dari [id.chefgenie.pawonee.app.nav.Rute] agar layar tak bergantung pada graf. */
enum class Tujuan { Rekomendasi, Stok, Preferensi, Riwayat }

@Composable
fun LayarStok(sumber: SumberStok, kembali: () -> Unit) {
    val stok by sumber.stok.collectAsState()
    Kerangka(Tag.LAYAR_STOK, "Stok dapur", kembali) {
        if (stok.isEmpty()) {
            Text("Stok kosong. Rekomendasi memakai mode hemat.", Modifier.testTag(Tag.STOK_KOSONG))
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(stok, key = { it.nama }) { s ->
                    val kedaluwarsa = s.kedaluwarsaHari?.let { " (kedaluwarsa $it hari)" } ?: ""
                    Text("${s.nama}: ${s.jumlah} ${s.satuan}$kedaluwarsa", Modifier.testTag(Tag.stokItem(s.nama)))
                }
            }
        }
    }
}

@Composable
fun LayarRekomendasi(
    holder: RekomendasiHolder,
    bukaDetail: (kode: String, id: String) -> Unit,
    kembali: () -> Unit,
) {
    val hasil by holder.hasil.collectAsState()
    Kerangka(Tag.LAYAR_REKOMENDASI, "Rekomendasi", kembali) {
        when {
            hasil.galat == KodeGalat.JIWA_DI_LUAR_RENTANG ->
                Text("Jumlah jiwa harus 1 sampai 8.", Modifier.testTag(Tag.REKOMENDASI_KOSONG))
            hasil.rekomendasi.isEmpty() ->
                Text("Belum ada resep yang cocok.", Modifier.testTag(Tag.REKOMENDASI_KOSONG))
            else -> {
                if (hasil.modeTanpaStok) Text("Mode hemat: stok kosong.", Modifier.testTag(Tag.MODE_HEMAT))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(hasil.rekomendasi, key = { it.kode }) { r ->
                        Card(
                            Modifier.fillMaxWidth().testTag(Tag.kartuResep(r.kode)).clickable { bukaDetail(r.kode, r.id) },
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(r.id.replace('-', ' '), style = MaterialTheme.typography.titleMedium)
                                Text("${r.menit} mnt; skor ${satuDesimal(r.skorTotal)}; belanja Rp${r.belanjaTambahanRp}")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Format satu desimal tanpa bergantung pada String.format (tidak ada di common). */
private fun satuDesimal(nilai: Double): String {
    val bulat = kotlin.math.round(nilai * 10).toLong()
    return "${bulat / 10}.${kotlin.math.abs(bulat % 10)}"
}

@Composable
fun LayarResepDetail(id: String, jiwa: Int, bank: BankResep, kembali: () -> Unit) {
    val resep = bank.resep.firstOrNull { it.id == id || it.kode.equals(id, ignoreCase = true) }
    Kerangka(Tag.LAYAR_RESEP_DETAIL, resep?.nama ?: "Resep", kembali) {
        if (resep == null) {
            Text("Resep tidak ditemukan.", Modifier.testTag(Tag.DETAIL_TIDAK_ADA))
            return@Kerangka
        }
        Text(resep.nama, Modifier.testTag(Tag.DETAIL_NAMA), style = MaterialTheme.typography.titleLarge)
        Text("Untuk $jiwa jiwa", Modifier.testTag(Tag.DETAIL_JIWA))
        val terskala = if (PorsiScaler.jiwaSah(jiwa)) PorsiScaler.skalaResep(resep, jiwa) else emptyList()
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(terskala, key = { it.nama }) { b ->
                Text("${b.nama}: ${b.jumlah} ${b.satuan}", Modifier.testTag(Tag.bahan(b.nama)))
            }
            items(resep.langkah.withIndex().toList(), key = { it.index }) { (i, l) -> Text("${i + 1}. $l") }
        }
    }
}

@Composable
fun LayarPreferensi(profilStore: ProfilStore, kembali: () -> Unit) {
    val profil by profilStore.profil.collectAsState()
    Kerangka(Tag.LAYAR_PREFERENSI, "Preferensi", kembali) {
        Text("Jumlah jiwa")
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = { profilStore.geserJiwa(-1) }, Modifier.testTag(Tag.JIWA_KURANG)) { Text("-") }
            Text("${profil.jiwa}", Modifier.testTag(Tag.JIWA_NILAI), style = MaterialTheme.typography.headlineMedium)
            OutlinedButton(onClick = { profilStore.geserJiwa(1) }, Modifier.testTag(Tag.JIWA_TAMBAH)) { Text("+") }
        }
    }
}

@Composable
fun LayarRiwayat(kembali: () -> Unit) {
    Kerangka(Tag.LAYAR_RIWAYAT, "Riwayat serapan", kembali) {
        Text("Belum ada riwayat masak.")
    }
}
