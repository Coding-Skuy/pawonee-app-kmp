package id.chefgenie.pawonee.pantryresep

/**
 * Skala porsi (BR-105) dan pembulatan (BR-106). Seluruh hitungan integer agar identik
 * dengan implementasi web dan fixture paritas.
 *
 * Faktor per jiwa dalam persen (bahan, bumbu, air): 1=30, 2=55, 3=80, 4=100, 5=125, 6=150, 8=200
 * untuk bahan. 7 jiwa TIDAK ada di BR-105; nilainya interpolasi (ASUMSI F2 di fixtures/README.md).
 */
public object PorsiScaler {
    public const val JIWA_MIN: Int = 1
    public const val JIWA_MAKS: Int = 8

    /** Faktor persen (bahan, bumbu, air). */
    private val faktor: Map<Int, Triple<Int, Int, Int>> = mapOf(
        1 to Triple(30, 35, 35),
        2 to Triple(55, 60, 60),
        3 to Triple(80, 80, 85),
        4 to Triple(100, 100, 100),
        5 to Triple(125, 115, 120),
        6 to Triple(150, 130, 140),
        7 to Triple(175, 145, 160), // ASUMSI: interpolasi 6 dan 8 jiwa
        8 to Triple(200, 160, 180),
    )

    /** Jiwa yang faktornya merupakan asumsi (belum ada di BR-105). */
    public val jiwaAsumsi: Set<Int> = setOf(7)

    private val kelipatan: Map<String, Int> = mapOf("sayur" to 50, "telur" to 2, "santan" to 200)

    public fun jiwaSah(jiwa: Int): Boolean = jiwa in JIWA_MIN..JIWA_MAKS

    /** Faktor bahan dalam persen untuk [jiwa]; lempar [JiwaDiLuarRentangException] bila tidak sah. */
    public fun faktorBahanPersen(jiwa: Int): Int = ambil(jiwa).first

    private fun ambil(jiwa: Int): Triple<Int, Int, Int> = faktor[jiwa] ?: throw JiwaDiLuarRentangException(jiwa)

    /**
     * Skala satu bahan: faktor persen sesuai [jenis] (bumbu, air/santan, atau bahan), lalu
     * dibulatkan KE ATAS ke kelipatan jenis (sayur 50 g, telur butir genap, santan 200 ml).
     */
    public fun skala(jumlah: Int, jenis: String, jiwa: Int): Int {
        val (bahan, bumbu, air) = ambil(jiwa)
        val persen = when (jenis) {
            "bumbu" -> bumbu
            "cair", "santan" -> air
            else -> bahan
        }
        val k = kelipatan[jenis] ?: 1
        return ceilBagi(jumlah.toLong() * persen, 100L * k).toInt() * k
    }

    /** Skala seluruh bahan resep untuk [jiwa]. */
    public fun skalaResep(resep: ResepBank, jiwa: Int): List<BahanTerskala> =
        resep.bahan.map { BahanTerskala(it.nama, skala(it.jumlah, it.jenis, jiwa), it.satuan) }

    /** Pembagian pembulatan ke atas untuk bilangan non-negatif. */
    private fun ceilBagi(a: Long, b: Long): Long = (a + b - 1) / b
}
