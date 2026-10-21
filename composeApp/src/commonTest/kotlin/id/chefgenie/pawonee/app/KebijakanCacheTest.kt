package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.cache.KebijakanCache
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class KebijakanCacheTest {
    @Test
    fun ttlTerkunci() {
        assertEquals(7 * 24 * 3_600_000L, KebijakanCache.TTL_RESEP_MS)
        assertEquals(24 * 3_600_000L, KebijakanCache.TTL_STOK_MS)
    }

    @Test
    fun segarSampaiTepatSebelumTtl() {
        assertTrue(KebijakanCache.segar(0, KebijakanCache.TTL_STOK_MS, KebijakanCache.TTL_STOK_MS - 1))
        assertFalse(KebijakanCache.segar(0, KebijakanCache.TTL_STOK_MS, KebijakanCache.TTL_STOK_MS))
    }

    @Test
    fun bannerLuringHanyaBilaLebihDari24Jam() {
        val jam = KebijakanCache.JAM_MS
        assertFalse(KebijakanCache.perluBannerLuring(0, 24 * jam))
        assertTrue(KebijakanCache.perluBannerLuring(0, 24 * jam + 1))
        assertTrue(KebijakanCache.perluBannerLuring(null, 0))
    }
}
