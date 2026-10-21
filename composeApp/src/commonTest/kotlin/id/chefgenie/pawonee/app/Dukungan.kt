package id.chefgenie.pawonee.app

import id.chefgenie.pawonee.app.data.cache.Jam

class JamPalsu(var sekarang: Long = 1_700_000_000_000L) : Jam {
    override fun sekarangMs(): Long = sekarang

    fun majuJam(n: Long) {
        sekarang += n * 3_600_000L
    }
}
