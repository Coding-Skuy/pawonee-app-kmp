package skuy.pawonee

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import skuy.pawonee.pantry.Resep

/** Layar utama rekomendasi: daftar kartu resep ringkas sesuai desain `pawonee-design`. */
@Composable
fun LayarRekomendasi(resep: List<Resep>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Rekomendasi Pawonee", style = MaterialTheme.typography.headlineSmall)
        LazyColumn {
            items(resep, key = { it.id }) { r ->
                Text(r.nama, style = MaterialTheme.typography.titleMedium)
                Text("${r.menit} mnt • pedas ${r.levelPedas}/5 • Rp${r.estimasiBiaya}")
            }
        }
    }
}
