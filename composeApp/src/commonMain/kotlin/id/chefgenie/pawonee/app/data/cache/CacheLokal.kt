package id.chefgenie.pawonee.app.data.cache

import id.chefgenie.pawonee.app.db.PawoneeDb
import id.chefgenie.pawonee.pantryresep.KodeGrade
import id.chefgenie.pawonee.pantryresep.StokItem

/** Cache SQLDelight untuk resep (7 hari) dan stok (24 jam). Entri kedaluwarsa tidak pernah dilayani. */
class CacheLokal(private val db: PawoneeDb, private val jam: Jam) {
    private val q get() = db.pawoneeQueries

    // ---- resep ----

    fun simpanResep(jsonPerResep: Map<String, String>) {
        val now = jam.sekarangMs()
        db.transaction {
            q.hapusResep()
            jsonPerResep.forEach { (id, json) -> q.simpanResep(id, json, now) }
            q.simpanMeta(META_RESEP, now.toString())
        }
    }

    /** JSON resep per id bila SELURUH entri masih segar (7 hari); null bila kosong atau kedaluwarsa. */
    fun bacaResepSegar(): Map<String, String>? {
        val baris = q.semuaResep().executeAsList()
        if (baris.isEmpty()) return null
        val now = jam.sekarangMs()
        if (baris.any { !KebijakanCache.segar(it.diambil_ms, KebijakanCache.TTL_RESEP_MS, now) }) return null
        return baris.associate { it.id to it.json }
    }

    fun resepSinkronTerakhirMs(): Long? = q.bacaMeta(META_RESEP).executeAsOneOrNull()?.toLongOrNull()

    // ---- stok ----

    fun simpanStok(stok: List<StokItem>) {
        val now = jam.sekarangMs()
        db.transaction {
            q.hapusStok()
            stok.forEach {
                q.simpanStok(it.nama, it.jumlah.toLong(), (it.kedaluwarsaHari ?: DEFAULT_KEDALUWARSA).toLong(), it.kodeGrade.kode, now)
            }
            q.simpanMeta(META_STOK, now.toString())
        }
    }

    /** Stok bila masih segar (24 jam); daftar kosong bila kedaluwarsa atau belum pernah disinkron. */
    fun bacaStokSegar(): List<StokItem> {
        val now = jam.sekarangMs()
        return q.semuaStok().executeAsList()
            .filter { KebijakanCache.segar(it.diambil_ms, KebijakanCache.TTL_STOK_MS, now) }
            .map {
                StokItem(
                    nama = it.nama_bahan,
                    jumlah = it.gram.toInt(),
                    satuan = "gram",
                    kodeGrade = KodeGrade.entries.first { k -> k.kode == it.kode_grade },
                    kedaluwarsaHari = it.kedaluwarsa_hari.toInt(),
                )
            }
    }

    fun stokSinkronTerakhirMs(): Long? = q.bacaMeta(META_STOK).executeAsOneOrNull()?.toLongOrNull()

    private companion object {
        const val META_RESEP = "resep_sinkron_ms"
        const val META_STOK = "stok_sinkron_ms"
        const val DEFAULT_KEDALUWARSA = 99
    }
}
