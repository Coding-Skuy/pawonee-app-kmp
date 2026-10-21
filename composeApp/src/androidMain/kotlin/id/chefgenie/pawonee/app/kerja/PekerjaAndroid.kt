package id.chefgenie.pawonee.app.kerja

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import id.chefgenie.pawonee.app.PawoneeApplication
import id.chefgenie.pawonee.app.data.cache.JamSistem
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlinx.datetime.TimeZone

/** WorkManager: menjalankan [TugasPagi] sekali sehari sekitar 07.00 waktu setempat. */
class PekerjaPagi(private val konteks: Context, params: WorkerParameters) : CoroutineWorker(konteks, params) {
    override suspend fun doWork(): Result {
        val container = (konteks.applicationContext as PawoneeApplication).container
        val tugas = TugasPagi(container.sinkron, container.cache, NotifikatorAndroid(konteks))
        return when (tugas.jalankan()) {
            is HasilTugasPagi.Selesai -> Result.success()
            HasilTugasPagi.Ulangi -> Result.retry()
        }
    }
}

object PenjadwalAndroid {
    const val NAMA_KERJA = "pawonee-pekerja-harian-0700"

    @OptIn(ExperimentalTime::class)
    fun jadwalkan(konteks: Context) {
        val tunda = JadwalPagi.tundaHinggaPagiBerikutnyaMs(
            Instant.fromEpochMilliseconds(JamSistem.sekarangMs()),
            TimeZone.currentSystemDefault(),
        )
        val permintaan = PeriodicWorkRequestBuilder<PekerjaPagi>(24, TimeUnit.HOURS)
            .setInitialDelay(tunda, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(konteks)
            .enqueueUniquePeriodicWork(NAMA_KERJA, ExistingPeriodicWorkPolicy.KEEP, permintaan)
    }
}

class NotifikatorAndroid(private val konteks: Context) : Notifikator {
    override fun tampilkanKedaluwarsa(bahan: List<String>) {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(konteks, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return
        val nm = konteks.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(NotificationChannel(SALURAN, "Stok hampir kedaluwarsa", NotificationManager.IMPORTANCE_DEFAULT))
        val notif = NotificationCompat.Builder(konteks, SALURAN)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Stok hampir kedaluwarsa")
            .setContentText("Masak hari ini: " + bahan.joinToString(", "))
            .setAutoCancel(true)
            .build()
        nm.notify(ID_NOTIF, notif)
    }

    private companion object {
        const val SALURAN = "stok_kedaluwarsa"
        const val ID_NOTIF = 7
    }
}
