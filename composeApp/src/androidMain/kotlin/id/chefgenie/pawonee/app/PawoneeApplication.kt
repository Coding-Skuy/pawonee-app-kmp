package id.chefgenie.pawonee.app

import android.app.Application
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import id.chefgenie.pawonee.app.db.PawoneeDb
import id.chefgenie.pawonee.app.kerja.PenjadwalAndroid

class PawoneeApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(
            driver = AndroidSqliteDriver(PawoneeDb.Schema, this, "pawonee.db"),
            direktoriData = filesDir.absolutePath,
            baseUrl = BASE_URL_PENGEMBANGAN,
        )
        // Pekerja harian 07.00 (idempoten: jadwal lama dipertahankan).
        PenjadwalAndroid.jadwalkan(this)
    }

    private companion object {
        /** Emulator Android -> backend lokal (openapi.yaml `servers`). Konfigurasi produksi menyusul. */
        const val BASE_URL_PENGEMBANGAN = "http://10.0.2.2:8080"
    }
}
