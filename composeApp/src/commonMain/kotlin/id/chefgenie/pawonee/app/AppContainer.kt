package id.chefgenie.pawonee.app

import app.cash.sqldelight.db.SqlDriver
import id.chefgenie.pawonee.app.data.cache.CacheLokal
import id.chefgenie.pawonee.app.data.cache.Jam
import id.chefgenie.pawonee.app.data.cache.JamSistem
import id.chefgenie.pawonee.app.data.net.ApiPawonee
import id.chefgenie.pawonee.app.data.net.SesiKosong
import id.chefgenie.pawonee.app.data.net.SesiProvider
import id.chefgenie.pawonee.app.data.profil.ProfilRepository
import id.chefgenie.pawonee.app.data.profil.buatDataStoreProfil
import id.chefgenie.pawonee.app.data.sinkron.SinkronService
import id.chefgenie.pawonee.app.data.sinkron.StokRepository
import id.chefgenie.pawonee.app.db.PawoneeDb
import id.chefgenie.pawonee.app.domain.ProfilStore
import io.ktor.client.HttpClient

/**
 * Perakitan manual dependensi luring-dahulu. Driver SQLDelight dan direktori data disediakan platform
 * (Android: `AndroidSqliteDriver` + `filesDir`; iOS: `NativeSqliteDriver` + direktori dokumen).
 */
class AppContainer(
    driver: SqlDriver,
    direktoriData: String,
    baseUrl: String,
    sesi: SesiProvider = SesiKosong,
    val jam: Jam = JamSistem,
    httpClient: HttpClient = ApiPawonee.buatHttpClient(),
) {
    val db: PawoneeDb = PawoneeDb(driver)
    val cache: CacheLokal = CacheLokal(db, jam)
    val stokRepo: StokRepository = StokRepository(cache)
    val api: ApiPawonee = ApiPawonee(httpClient, baseUrl, sesi)
    val sinkron: SinkronService = SinkronService(api, cache, stokRepo, jam)
    val profilStore: ProfilStore = ProfilStore()
    val profilRepo: ProfilRepository = ProfilRepository(buatDataStoreProfil(direktoriData), profilStore)
}
