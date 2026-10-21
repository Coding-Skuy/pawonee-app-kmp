package id.chefgenie.pawonee.pantryresep

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/** Pemuat JSON bank resep dan tabel harga referensi (format `pawonee-ai-models`). */
public object BankLoader {
    private val json = Json { ignoreUnknownKeys = true }

    public fun bankDariJson(teks: String): BankResep = json.decodeFromString(BankResep.serializer(), teks)

    public fun hargaDariJson(teks: String): Map<String, HargaBahan> =
        json.decodeFromString(MapSerializer(String.serializer(), HargaBahan.serializer()), teks)
}
