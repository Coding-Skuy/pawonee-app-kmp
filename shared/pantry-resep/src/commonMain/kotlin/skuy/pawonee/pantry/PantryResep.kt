package skuy.pawonee.pantry

import kotlinx.serialization.Serializable

/** Model inti dapur Pawonee. Modul `:shared:pantry-resep` adalah milik Divisi Pawonee. */
@Serializable
data class Bahan(val nama: String, val jumlah: String = "")

@Serializable
data class Resep(
    val id: String,
    val nama: String,
    val bahan: List<Bahan>,
    val langkah: List<String>,
    val menit: Int,
    val levelPedas: Int,
    val estimasiBiaya: Long,
    val grade: Int = 2,
)

@Serializable
data class Preferensi(
    val penggunaId: String,
    val levelPedasMaks: Int = 3,
    val alergi: List<String> = emptyList(),
    val anggaranMaks: Long = 50000L,
)

/** Skor kecocokan resep terhadap isi dapur + preferensi. Murni, mudah diuji. */
fun skorKecocokan(resep: Resep, dapur: Set<String>, preferensi: Preferensi): Double {
    if (resep.levelPedas > preferensi.levelPedasMaks) return 0.0
    if (resep.bahan.any { b -> preferensi.alergi.any { a -> b.nama.contains(a, ignoreCase = true) } }) return 0.0
    if (resep.estimasiBiaya > preferensi.anggaranMaks) return 0.0
    if (resep.bahan.isEmpty()) return 0.0
    val cocok = resep.bahan.count { b -> dapur.any { d -> d.equals(b.nama, ignoreCase = true) } }
    return cocok.toDouble() / resep.bahan.size.toDouble()
}

/** Urutkan resep terbaik lebih dulu untuk bahan dapur yang tersedia. */
fun rekomendasiUntukDapur(kandidat: List<Resep>, dapur: Set<String>, preferensi: Preferensi): List<Resep> =
    kandidat
        .map { it to skorKecocokan(it, dapur, preferensi) }
        .filter { (_, skor) -> skor > 0.0 }
        .sortedByDescending { (_, skor) -> skor }
        .map { (resep, _) -> resep }
