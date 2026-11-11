package com.example.kasku.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.kasku.R
import com.example.kasku.data.Transaction
import com.example.kasku.data.TransactionRepository
import com.example.kasku.data.isIncome
import com.example.kasku.databinding.ActivityDetailBinding
import com.example.kasku.util.Format
import com.example.kasku.util.isOnline
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ListenerRegistration

class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    private lateinit var b: ActivityDetailBinding
    private val repo = TransactionRepository()
    private var registration: ListenerRegistration? = null
    private lateinit var transactionId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(b.root)

        transactionId = intent.getStringExtra(EXTRA_ID) ?: run { finish(); return }

        b.toolbar.setTitle(R.string.detail_transaction)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.btnEdit.setOnClickListener {
            startActivity(
                Intent(this, FormActivity::class.java).putExtra(FormActivity.EXTRA_ID, transactionId)
            )
        }
        b.btnDelete.setOnClickListener { confirmDelete() }

        // Listener real-time: setelah diedit, tampilan ini langsung ikut berubah.
        registration = repo.listenOne(
            transactionId,
            onData = { t -> if (t == null) finish() else bind(t) },
            onError = { Snackbar.make(b.root, R.string.load_error, Snackbar.LENGTH_LONG).show() }
        )
    }

    private fun bind(t: Transaction) {
        b.progress.isVisible = false
        b.content.isVisible = true
        b.tvTitle.text = t.title
        val sign = if (t.isIncome) "+ " else "- "
        b.tvAmount.text = sign + Format.rupiah(t.amount)
        b.tvAmount.setTextColor(
            ContextCompat.getColor(this, if (t.isIncome) R.color.income else R.color.expense)
        )
        b.tvType.text = getString(if (t.isIncome) R.string.income else R.string.expense)
        b.tvCategory.text = t.category
        b.tvDate.text = Format.date(t.date)
        b.tvNote.text = t.note.ifBlank { "-" }
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.delete_title)
            .setMessage(R.string.delete_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ -> delete() }
            .show()
    }

    private fun delete() {
        if (!isOnline()) {
            Snackbar.make(b.root, R.string.err_no_network, Snackbar.LENGTH_LONG).show()
            return
        }
        repo.delete(transactionId)
            .addOnSuccessListener { finish() }
            .addOnFailureListener {
                Snackbar.make(b.root, R.string.err_generic, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onDestroy() {
        registration?.remove()
        super.onDestroy()
    }
}