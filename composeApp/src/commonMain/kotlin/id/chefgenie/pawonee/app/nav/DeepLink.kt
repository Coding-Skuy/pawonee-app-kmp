package id.chefgenie.pawonee.app.nav

import id.chefgenie.pawonee.pantryresep.PorsiScaler

/** Hasil penguraian `pawonee://resep/{id}?jiwa=4`. `jiwa` null bila tak ada atau tak sah (1..8). */
data class DeepLinkResep(val id: String, val jiwa: Int?)

object DeepLink {
    const val SKEMA = "pawonee"
    const val HOST_RESEP = "resep"

    fun urai(uri: String): DeepLinkResep? {
        val awalan = "$SKEMA://$HOST_RESEP/"
        if (!uri.startsWith(awalan, ignoreCase = true)) return null
        val sisa = uri.substring(awalan.length).substringBefore('#')
        val id = sisa.substringBefore('?').trim('/')
        if (id.isEmpty() || id.contains('/')) return null
        val jiwa = sisa.substringAfter('?', "")
            .split('&')
            .firstOrNull { it.startsWith("jiwa=") }
            ?.removePrefix("jiwa=")
            ?.toIntOrNull()
            ?.takeIf(PorsiScaler::jiwaSah)
        return DeepLinkResep(id, jiwa)
    }
}
