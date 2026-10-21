#!/usr/bin/env python3
"""Membangkitkan BankBawaan.kt (bank 12 resep + tabel harga) dari fixture yang disalin dari pawonee-ai-models.

Jalankan dari akar repo setelah menyalin ulang fixture:  python3 scripts/buat-bank-bawaan.py
Uji `BankBawaanTest` memastikan hasil bangkitan sama dengan bank fixture.
"""
import json
import pathlib

akar = pathlib.Path(__file__).resolve().parent.parent
fx = akar / "shared/pantry-resep/src/commonTest/fixtures"
bank = (fx / "bank_resep_grade2.json").read_text(encoding="utf-8")
kasus = json.loads((fx / "kasus-uji.json").read_text(encoding="utf-8"))
harga = kasus["konfigurasi"]["harga"]
assert '"""' not in bank and "$" not in bank
entri = ",\n".join(
    f'        "{n}" to HargaBahan("{h["satuan"]}", {h["per"]}, {h["harga"]})' for n, h in sorted(harga.items())
)
isi = f'''package id.chefgenie.pawonee.app.data

import id.chefgenie.pawonee.pantryresep.BankLoader
import id.chefgenie.pawonee.pantryresep.BankResep
import id.chefgenie.pawonee.pantryresep.HargaBahan

// BERKAS DIBANGKITKAN oleh scripts/buat-bank-bawaan.py; jangan disunting tangan.
// Sumber: pawonee-ai-models models/bank_resep_grade2.json (bank 12 resep inti) dan harga referensi (ASUMSI).

/** Bank 12 resep bawaan: dipakai saat cache resep kosong dan sebelum sinkron pertama (luring-dahulu). */
object BankBawaan {{
    val bank: BankResep by lazy {{ BankLoader.bankDariJson(JSON) }}

    val harga: Map<String, HargaBahan> = mapOf(
{entri},
    )

    const val JSON: String = """{bank}"""
}}
'''
(akar / "composeApp/src/commonMain/kotlin/id/chefgenie/pawonee/app/data/BankBawaan.kt").write_text(isi, encoding="utf-8")
