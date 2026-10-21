package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.nav.DeepLink
import id.chefgenie.pawonee.app.nav.DeepLinkResep
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DeepLinkTest {
    @Test
    fun uraiResepDenganJiwa() {
        assertEquals(DeepLinkResep("telur-balado", 4), DeepLink.urai("pawonee://resep/telur-balado?jiwa=4"))
    }

    @Test
    fun uraiResepTanpaJiwa() {
        assertEquals(DeepLinkResep("R-03", null), DeepLink.urai("pawonee://resep/R-03"))
    }

    @Test
    fun jiwaTakSahDiabaikan() {
        for (j in listOf("0", "9", "abc", "")) {
            assertEquals(DeepLinkResep("x", null), DeepLink.urai("pawonee://resep/x?jiwa=$j"), j)
        }
    }

    @Test
    fun jiwaDenganParameterLain() {
        assertEquals(DeepLinkResep("x", 6), DeepLink.urai("pawonee://resep/x?utm=a&jiwa=6#frag"))
    }

    @Test
    fun bukanDeepLinkResepDitolak() {
        assertNull(DeepLink.urai("https://example.com/resep/x"))
        assertNull(DeepLink.urai("pawonee://stok/x"))
        assertNull(DeepLink.urai("pawonee://resep/"))
        assertNull(DeepLink.urai("pawonee://resep/a/b"))
    }
}
