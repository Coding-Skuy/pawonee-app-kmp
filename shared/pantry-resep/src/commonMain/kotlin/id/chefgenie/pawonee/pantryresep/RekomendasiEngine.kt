package id.chefgenie.pawonee.pantryresep

/**
 * Mesin rekomendasi lima tahap (PRD 20-alur): filter keras, serapan (bobot 50, bonus +5 G2-S
 * kedaluwarsa <=2 hari), kemudahan (stok 25, budget 15, waktu 10), ranking maks 5 dengan
 * diversifikasi maks 2 sekelompok. Rumus HANYA ditulis di sini (AGENTS.md butir 9); web mem-port.
 * Rujukan angka: fixtures paritas 32 kasus (selisih skor < 0,01).
 */
public class RekomendasiEngine(
    private val bank: BankResep,
    private val harga: Map<String, HargaBahan>,
) {
    private val penggantiAyam: Substitusi? = bank.substitusi.firstOrNull { it.hilang.nama == "ayam" }

    public fun rekomendasikan(
        profil: ProfilRekomendasi,
        stok: List<StokItem>,
        jiwa: Int,
        waktuMaksimalMenit: Int = 30,
    ): HasilRekomendasi {
        if (!PorsiScaler.jiwaSah(jiwa)) {
            return HasilRekomendasi(KodeGalat.JIWA_DI_LUAR_RENTANG, false, emptyList())
        }
        val gabung = gabungStok(stok)
        val kandidat = mutableListOf<Kandidat>()
        for (resep in bank.resep) {
            val (bahan, alergen) = bahanEfektif(resep, profil) ?: continue
            if (!lolosFilter(resep, profil, alergen, bahan, waktuMaksimalMenit)) continue
            val hitung = hitung(resep, bahan, profil, gabung, jiwa) ?: continue
            kandidat += Kandidat(hitung, resep.estimasiBiaya)
        }

        if (stok.isEmpty()) {
            // Mode hemat: skor tidak dihitung; 5 resep lolos filter dengan estimasiBiaya termurah.
            val pilihan = kandidat
                .sortedWith(compareBy({ it.estimasiBiaya }, { it.rek.menit }, { it.rek.kode }))
                .take(MAKS_REKOMENDASI)
                .map {
                    it.rek.copy(
                        skorTotal = 0.0, skorSerapan = 0.0, skorKecocokanStok = 0.0, skorBudget = 0, skorWaktu = 0,
                    )
                }
            return HasilRekomendasi(null, true, pilihan)
        }

        val pilihan = mutableListOf<Rekomendasi>()
        val perKelompok = mutableMapOf<String, Int>()
        val urut = kandidat.map { it.rek }.sortedWith(
            compareBy<Rekomendasi>({ -kunciSkor(it.skorTotal) }, { it.menit }, { it.belanjaTambahanRp }, { it.kode }),
        )
        for (r in urut) {
            val n = perKelompok[r.kelompok] ?: 0
            if (n >= MAKS_PER_KELOMPOK) continue
            perKelompok[r.kelompok] = n + 1
            pilihan += r
            if (pilihan.size == MAKS_REKOMENDASI) break
        }
        return HasilRekomendasi(null, false, pilihan)
    }

    private class Kandidat(val rek: Rekomendasi, val estimasiBiaya: Int)

    private data class Efektif(val bahan: List<BahanResep>, val alergen: Set<String>)

    /** Tahap 0: substitusi vegetarian-nabati; null bila resep tidak boleh tampil. */
    private fun bahanEfektif(resep: ResepBank, profil: ProfilRekomendasi): Efektif? {
        var bahan = resep.bahan
        var alergen = resep.alergen.toSet()
        if ("vegetarian-nabati" in profil.pantangan && "mengandung-daging" in resep.flag) {
            if ("varian-nabati" !in resep.flag) return null
            val ayam = bahan.firstOrNull { it.nama == "ayam" } ?: return null
            val sub = penggantiAyam ?: return null
            val alt = sub.pengganti.firstOrNull { a -> a.alergen.none { it in profil.alergi } } ?: return null
            val komponen = alt.komponen.map { k ->
                BahanResep(
                    nama = k.nama,
                    jumlah = bulatSetengahNaik(ayam.jumlah.toLong() * k.jumlah, 100L),
                    satuan = k.satuan,
                    kodeGrade = k.kodeGrade,
                    jenis = if (k.nama == "telur") "telur" else "protein",
                )
            }
            val idx = bahan.indexOf(ayam)
            bahan = bahan.subList(0, idx) + komponen + bahan.subList(idx + 1, bahan.size)
            alergen = alergen + alt.alergen
        }
        return Efektif(bahan, alergen)
    }

    /** Tahap 1: filter keras (alergen, pantangan, alat, waktu, cabai). Filter budget ada di [hitung]. */
    private fun lolosFilter(
        resep: ResepBank,
        profil: ProfilRekomendasi,
        alergen: Set<String>,
        bahan: List<BahanResep>,
        waktuMaks: Int,
    ): Boolean {
        if (alergen.any { it in profil.alergi }) return false
        if ("tanpa-santan" in profil.pantangan && "mengandung-santan" in resep.flag) return false
        if ("lunak-lansia" in profil.pantangan && "lunak-lansia" !in resep.flag) return false
        if (!profil.alatMasak.containsAll(resep.alat)) return false
        if (resep.menit > waktuMaks) return false
        val cabai = bahan.filter { it.nama.startsWith("cabai") }.sumOf { it.jumlah }
        if (cabai > BATAS_CABAI_GRAM && profil.levelPedas < 2) return false
        return true
    }

    private fun hargaDari(nama: String, jumlah: Int): Int {
        val e = harga[nama] ?: throw IllegalArgumentException("Harga bahan tidak dikenal: $nama")
        return bulatSetengahNaik(jumlah.toLong() * e.harga, e.per.toLong())
    }

    private fun hitung(
        resep: ResepBank,
        bahan: List<BahanResep>,
        profil: ProfilRekomendasi,
        stok: Map<String, StokItem>,
        jiwa: Int,
    ): Rekomendasi? {
        val terskala = bahan.map { it to PorsiScaler.skala(it.jumlah, it.jenis, jiwa) }
        val g2Tersedia = stok.values.filter { it.satuan == "gram" && it.kodeGrade.grade2 }.sumOf { it.jumlah }
        var g2Terpakai = 0
        var bonus = false
        var dimiliki = 0
        var total = 0
        val belanja = mutableListOf<BelanjaItem>()
        for ((b, jumlah) in terskala) {
            val s = stok[b.nama]
            val ada = if (s != null && s.satuan == b.satuan) s.jumlah else 0
            if (b.nama in BAHAN_DAPUR_POKOK) continue
            total += 1
            if (ada >= jumlah) dimiliki += 1
            val kurang = maxOf(0, jumlah - ada)
            if (kurang > 0) belanja += BelanjaItem(b.nama, kurang, b.satuan, hargaDari(b.nama, kurang))
            if (s != null && b.kodeGrade.grade2 && b.satuan == "gram" && s.kodeGrade.grade2 && ada > 0) {
                g2Terpakai += minOf(jumlah, ada)
                val hari = s.kedaluwarsaHari
                if (s.kodeGrade == KodeGrade.G2_S && hari != null && hari <= HARI_BONUS_MAKS) bonus = true
            }
        }
        val belanjaRp = belanja.sumOf { it.rupiah }
        if (belanjaRp > profil.budgetPerMakan) return null // BR-205

        val dasar = if (g2Tersedia > 0) BOBOT_SERAPAN * g2Terpakai.toDouble() / g2Tersedia else 0.0
        val serapan = minOf(BOBOT_SERAPAN.toDouble(), dasar + if (bonus) BONUS_G2S else 0)
        val kecocokan = if (total > 0) BOBOT_KECOCOKAN * dimiliki.toDouble() / total else 0.0
        val skorBudget = if (belanjaRp.toLong() * 10 <= profil.budgetPerMakan.toLong() * 6) 15 else 8
        val skorWaktu = when {
            resep.menit <= 15 -> 10
            resep.menit <= 30 -> 7
            else -> 4
        }
        return Rekomendasi(
            kode = resep.kode,
            id = resep.id,
            kelompok = resep.kelompok,
            menit = resep.menit,
            skorTotal = serapan + kecocokan + skorBudget + skorWaktu,
            skorSerapan = serapan,
            skorKecocokanStok = kecocokan,
            skorBudget = skorBudget,
            skorWaktu = skorWaktu,
            belanjaTambahanRp = belanjaRp,
            belanjaTambahan = belanja,
            bahanTerskala = terskala.map { (b, j) -> BahanTerskala(b.nama, j, b.satuan) },
        )
    }

    /** Satu entri per nama: jumlah dijumlahkan, kedaluwarsa terkecil, grade/satuan entri pertama. */
    private fun gabungStok(stok: List<StokItem>): Map<String, StokItem> {
        val hasil = LinkedHashMap<String, StokItem>()
        for (s in stok) {
            val h = hasil[s.nama]
            if (h == null) {
                hasil[s.nama] = s
                continue
            }
            val kedaluwarsa = when {
                s.kedaluwarsaHari == null -> h.kedaluwarsaHari
                h.kedaluwarsaHari == null -> s.kedaluwarsaHari
                else -> minOf(h.kedaluwarsaHari, s.kedaluwarsaHari)
            }
            hasil[s.nama] = h.copy(jumlah = h.jumlah + s.jumlah, kedaluwarsaHari = kedaluwarsa)
        }
        return hasil
    }

    private companion object {
        const val MAKS_REKOMENDASI = 5
        const val MAKS_PER_KELOMPOK = 2
        const val BATAS_CABAI_GRAM = 50
        const val BOBOT_SERAPAN = 50
        const val BOBOT_KECOCOKAN = 25
        const val BONUS_G2S = 5
        const val HARI_BONUS_MAKS = 2
        const val ARAH_SKOR = 10000

        /** ASUMSI F3: bahan dapur pokok dianggap selalu ada; tidak dihitung pada kecocokan maupun belanja. */
        val BAHAN_DAPUR_POKOK: Set<String> = setOf("air", "garam", "gula", "merica", "minyak goreng")

        /** Seri skor dibandingkan pada 4 desimal (bulat setengah naik). */
        fun kunciSkor(skor: Double): Long = (skor * ARAH_SKOR + 0.5).toLong()

        fun bulatSetengahNaik(pembilang: Long, penyebut: Long): Int =
            ((2 * pembilang + penyebut) / (2 * penyebut)).toInt()
    }
}
