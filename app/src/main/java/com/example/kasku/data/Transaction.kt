package com.example.kasku.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/** Entitas utama: satu transaksi keuangan milik satu pengguna. */
data class Transaction(
    @DocumentId var id: String = "",
    var title: String = "",
    var amount: Long = 0L,
    var type: String = TYPE_EXPENSE,
    var category: String = "",
    var date: Timestamp = Timestamp.now(),
    var note: String = ""
) {
    companion object {
        const val TYPE_INCOME = "income"
        const val TYPE_EXPENSE = "expense"
    }
}

// Extension (bukan properti di dalam class) agar tidak ikut tersimpan ke Firestore.
val Transaction.isIncome: Boolean get() = type == Transaction.TYPE_INCOME

object Categories {
    val ALL = listOf("Makan", "Transport", "Belanja", "Tagihan", "Hiburan", "Kesehatan", "Gaji", "Lainnya")
}
