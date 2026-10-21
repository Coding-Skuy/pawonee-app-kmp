package id.chefgenie.pawonee.app.kerja

import id.chefgenie.pawonee.app.data.cache.CacheLokal
import id.chefgenie.pawonee.app.data.sinkron.HasilSinkron
import id.chefgenie.pawonee.app.data.sinkron.SinkronService
import id.chefgenie.pawonee.pantryresep.KodeGrade
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime

/** Penampil notifikasi stok hampir kedaluwarsa (implementasi per platform). */
fun interface Notifikator {
    fun tampilkanKedaluwarsa(bahan: List<String>)
}

@OptIn(ExperimentalTime::class)
object JadwalPagi {
    const val JAM_PAGI: Int = 7

    /** Selisih milidetik dari [sekarang] sampai pukul 07.00 berikutnya di zona [zona] (tepat 07.00 -> hari berikutnya). */
    fun tundaHinggaPagiBerikutnyaMs(sekarang: Instant, zona: TimeZone): Long {
        val lokal: LocalDateTime = sekarang.toLocalDateTime(zona)
        val hariIni = lokal.date.atTime(LocalTime(JAM_PAGI, 0)).toInstant(zona)
        val target = if (hariIni > sekarang) hariIni else lokal.date.plus(1, DateTimeUnit.DAY).atTime(LocalTime(JAM_PAGI, 0)).toInstant(zona)
        return (target - sekarang).inWholeMilliseconds
    }
}

sealed interface HasilTugasPagi {
    /** [bahanLayu]: G2-S dengan kedaluwarsa <= 2 hari (bonus skor); [diberitahukan]: kedaluwarsa <= 1 hari. */
    data class Selesai(val bahanLayu: List<String>, val diberitahukan: List<String>) : HasilTugasPagi
    /** Sinkron gagal: penjadwal platform diminta mencoba lagi (WorkManager `Result.retry()`). */
    data object Ulangi : HasilTugasPagi
}

/**
 * Tugas harian 07.00 (FSD 10-alur butir 4): sinkron stok Pedaree, hitung bahan layu G2-S (bonus +5 dipakai
 * mesin), dan beri notifikasi bila ada stok kedaluwarsa maksimal 1 hari.
 */
class TugasPagi(
    private val sinkron: SinkronService,
    private val cache: CacheLokal,
    private val notifikator: Notifikator,
) {
    suspend fun jalankan(): HasilTugasPagi {
        when (sinkron.sinkronStok()) {
            is HasilSinkron.Gagal -> return HasilTugasPagi.Ulangi
            is HasilSinkron.Berhasil -> Unit
        }
        sinkron.sinkronResep()
        val stok = cache.bacaStokSegar()
        val layu = stok.filter { it.kodeGrade == KodeGrade.G2_S && (it.kedaluwarsaHari ?: Int.MAX_VALUE) <= HARI_BONUS }
            .map { it.nama }
        val segera = stok.filter { (it.kedaluwarsaHari ?: Int.MAX_VALUE) <= HARI_NOTIFIKASI }.map { it.nama }
        if (segera.isNotEmpty()) notifikator.tampilkanKedaluwarsa(segera)
        return HasilTugasPagi.Selesai(layu, segera)
    }

    private companion object {
        const val HARI_BONUS = 2
        const val HARI_NOTIFIKASI = 1
    }
}
