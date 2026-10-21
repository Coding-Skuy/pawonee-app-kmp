package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.kerja.JadwalPagi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

@OptIn(ExperimentalTime::class)
class JadwalPagiTest {
    private val wib = TimeZone.of("Asia/Jakarta")

    @Test
    fun pukul0600MenungguSatuJam() {
        // 06.00 WIB = 23.00 UTC hari sebelumnya
        val sekarang = Instant.parse("2026-10-20T23:00:00Z")
        assertEquals(3_600_000L, JadwalPagi.tundaHinggaPagiBerikutnyaMs(sekarang, wib))
    }

    @Test
    fun pukul0800MenungguSampaiBesokPagi() {
        val sekarang = Instant.parse("2026-10-21T01:00:00Z") // 08.00 WIB
        assertEquals(23 * 3_600_000L, JadwalPagi.tundaHinggaPagiBerikutnyaMs(sekarang, wib))
    }

    @Test
    fun tepat0700Berikutnya24Jam() {
        val sekarang = Instant.parse("2026-10-21T00:00:00Z") // 07.00 WIB
        assertEquals(24 * 3_600_000L, JadwalPagi.tundaHinggaPagiBerikutnyaMs(sekarang, wib))
    }
}
