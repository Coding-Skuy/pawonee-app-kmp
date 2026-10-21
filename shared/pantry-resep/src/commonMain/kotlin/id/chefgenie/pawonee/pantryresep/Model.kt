package id.chefgenie.pawonee.pantryresep

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** Kode grade bahan (KONTEKS §5). `G2-*` = grade-2 yang diserap Pawonee. */
@Serializable
public enum class KodeGrade(public val kode: String) {
    @SerialName("G2-V") G2_V("G2-V"),
    @SerialName("G2-U") G2_U("G2-U"),
    @SerialName("G2-S") G2_S("G2-S"),
    @SerialName("NON-G2") NON_G2("NON-G2");

    public val grade2: Boolean get() = this != NON_G2
}

/** Bahan satu resep pada takaran 4 jiwa. [jenis]: protein, sayur, bumbu, cair, santan, telur, dst. */
@Serializable
public data class BahanResep(
    val nama: String,
    val jumlah: Int,
    val satuan: String,
    val kodeGrade: KodeGrade,
    val jenis: String,
)

/** Resep bank 12 resep inti (R-01..R-12). */
@Serializable
public data class ResepBank(
    val kode: String,
    val id: String,
    val nama: String,
    val kelompok: String,
    val bahan: List<BahanResep>,
    val langkah: List<String> = emptyList(),
    val menit: Int,
    val jumlahKompor: Int = 1,
    val levelPedas: Int = 0,
    val estimasiBiaya: Int = 0,
    val alergen: List<String> = emptyList(),
    val alat: List<String> = emptyList(),
    val flag: List<String> = emptyList(),
    val grade: Int = 2,
)

@Serializable
public data class KomponenPengganti(
    val nama: String,
    val jumlah: Int,
    val satuan: String,
    val kodeGrade: KodeGrade,
)

@Serializable
public data class Pengganti(
    val komponen: List<KomponenPengganti>,
    val alergen: List<String> = emptyList(),
    val tambahMenit: Int = 0,
)

/** Substitusi baku (BR-103): [hilang] per takaran dasar diganti salah satu [pengganti]. */
@Serializable
public data class Substitusi(
    val id: String,
    val sumber: String = "",
    val hilang: KomponenHilang,
    val pengganti: List<Pengganti>,
)

@Serializable
public data class KomponenHilang(val nama: String, val jumlah: Int, val satuan: String)

/** Bank resep + substitusi (`models/bank_resep_grade2.json` di pawonee-ai-models). */
@Serializable
public data class BankResep(
    val versiBank: String = "",
    val versiSkema: String = "",
    val resep: List<ResepBank>,
    val substitusi: List<Substitusi> = emptyList(),
)

/** Harga referensi per bahan: `harga` rupiah per `per` satuan (ASUMSI sampai kontrak harga terbit). */
@Serializable
public data class HargaBahan(val satuan: String, val per: Int, val harga: Int)

@Serializable
public data class ProfilRekomendasi(
    val alergi: List<String> = emptyList(),
    val pantangan: List<String> = emptyList(),
    val budgetPerMakan: Int,
    val levelPedas: Int,
    val alatMasak: List<String>,
)

@Serializable
public data class StokItem(
    val nama: String,
    val jumlah: Int,
    val satuan: String,
    val kodeGrade: KodeGrade,
    val kedaluwarsaHari: Int? = null,
)

@Serializable
public data class BahanTerskala(val nama: String, val jumlah: Int, val satuan: String)

@Serializable
public data class BelanjaItem(val nama: String, val jumlah: Int, val satuan: String, val rupiah: Int)

@Serializable
public data class Rekomendasi(
    val kode: String,
    val id: String,
    val kelompok: String,
    val menit: Int,
    val skorTotal: Double,
    val skorSerapan: Double,
    val skorKecocokanStok: Double,
    val skorBudget: Int,
    val skorWaktu: Int,
    val belanjaTambahanRp: Int,
    val belanjaTambahan: List<BelanjaItem>,
    val bahanTerskala: List<BahanTerskala>,
)

/** Kode galat modul (selaras `KodeGalat` di openapi.yaml). */
public enum class KodeGalat { JIWA_DI_LUAR_RENTANG }

public data class HasilRekomendasi(
    val galat: KodeGalat?,
    val modeTanpaStok: Boolean,
    val rekomendasi: List<Rekomendasi>,
)

/** Dilempar bila [jiwa] di luar 1..8 (BR-105). */
public class JiwaDiLuarRentangException(public val jiwa: Int) :
    IllegalArgumentException("${KodeGalat.JIWA_DI_LUAR_RENTANG}: jiwa=$jiwa (harus 1..8)") {
    public val kode: KodeGalat = KodeGalat.JIWA_DI_LUAR_RENTANG
}
