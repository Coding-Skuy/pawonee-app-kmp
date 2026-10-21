package id.chefgenie.pawonee.app.data.cache

import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/** Sumber waktu yang bisa diganti pada uji. */
fun interface Jam {
    fun sekarangMs(): Long
}

@OptIn(ExperimentalTime::class)
object JamSistem : Jam {
    override fun sekarangMs(): Long = Clock.System.now().toEpochMilliseconds()
}
