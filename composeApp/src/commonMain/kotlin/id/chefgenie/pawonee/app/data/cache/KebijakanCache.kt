package id.chefgenie.pawonee.app.data.cache

/** Masa berlaku cache yang dikunci FSD 10-alur: resep 7 hari, stok 24 jam, profil selamanya. */
object KebijakanCache {
    const val JAM_MS: Long = 3_600_000L
    const val HARI_MS: Long = 24 * JAM_MS
    const val TTL_RESEP_MS: Long = 7 * HARI_MS
    const val TTL_STOK_MS: Long = 24 * JAM_MS

    /** Banner luring muncul bila sinkron terakhir lebih dari 24 jam lalu (atau belum pernah). */
    const val AMBANG_BANNER_LURING_MS: Long = 24 * JAM_MS

    fun segar(diambilMs: Long, ttlMs: Long, sekarangMs: Long): Boolean = sekarangMs - diambilMs < ttlMs

    fun perluBannerLuring(sinkronTerakhirMs: Long?, sekarangMs: Long): Boolean =
        sinkronTerakhirMs == null || sekarangMs - sinkronTerakhirMs > AMBANG_BANNER_LURING_MS
}
