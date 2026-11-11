package com.example.kasku.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.kasku.R
import com.example.kasku.data.Categories
import com.example.kasku.data.Transaction
import com.example.kasku.data.TransactionRepository
import com.example.kasku.databinding.ActivityFormBinding
import com.example.kasku.util.DateUtil
import com.example.kasku.util.Format
import com.example.kasku.util.isOnline
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import java.util.Date

class FormActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID = "extra_id"
        private const val STATE_DATE = "state_date"
    }

    private lateinit var b: ActivityFormBinding
    private val repo = TransactionRepository()
    private var editingId: String? = null
    private var dateMillis: Long = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        b = ActivityFormBinding.inflate(layoutInflater)
        setContentView(b.root)

        editingId = intent.getStringExtra(EXTRA_ID)
        savedInstanceState?.getLong(STATE_DATE)?.let { dateMillis = it }

        b.toolbar.setTitle(if (editingId == null) R.string.add_transaction else R.string.edit_transaction)
        b.toolbar.setNavigationOnClickListener { finish() }

        b.actCategory.setSimpleItems(Categories.ALL.toTypedArray())
        updateDateText()
        b.etDate.setOnClickListener { pickDate() }
        b.btnSave.setOnClickListener { save() }

        // Mode edit: isi form dari data yang ada (hanya saat pertama dibuka).
        val id = editingId
        if (id != null && savedInstanceState == null) loadExisting(id)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(STATE_DATE, dateMillis)
    }

    private fun loadExisting(id: String) {
        setLoading(true)
        repo.getOnce(id)
            .addOnSuccessListener { snap ->
                setLoading(false)
                val t = snap.toObject(Transaction::class.java) ?: return@addOnSuccessListener
                b.etTitle.setText(t.title)
                b.etAmount.setText(t.amount.toString())
                b.etNote.setText(t.note)
                b.actCategory.setText(t.category, false)
                b.toggleType.check(
                    if (t.type == Transaction.TYPE_INCOME) R.id.btnIncome else R.id.btnExpense
                )
                dateMillis = t.date.toDate().time
                updateDateText()
            }
            .addOnFailureListener {
                setLoading(false)
                Snackbar.make(b.root, R.string.load_error, Snackbar.LENGTH_LONG).show()
            }
    }

    private fun pickDate() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(DateUtil.localToUtc(dateMillis))
            .build()
        picker.addOnPositiveButtonClickListener {
            dateMillis = DateUtil.utcToLocal(it)
            updateDateText()
        }
        picker.show(supportFragmentManager, "date_picker")
    }

    private fun updateDateText() {
        b.etDate.setText(Format.date(dateMillis))
    }

    private fun save() {
        val title = b.etTitle.text.toString().trim()
        val amount = b.etAmount.text.toString().trim().toLongOrNull()
        val category = b.actCategory.text.toString().trim()
        val note = b.etNote.text.toString().trim()
        val type = if (b.toggleType.checkedButtonId == R.id.btnIncome)
            Transaction.TYPE_INCOME else Transaction.TYPE_EXPENSE

        b.tilTitle.error = null
        b.tilAmount.error = null
        b.tilCategory.error = null

        var valid = true
        if (title.isEmpty()) { b.tilTitle.error = getString(R.string.err_title_empty); valid = false }
        if (amount == null || amount <= 0L) { b.tilAmount.error = getString(R.string.err_amount_invalid); valid = false }
        if (category.isEmpty()) { b.tilCategory.error = getString(R.string.err_category_empty); valid = false }
        if (!valid) return

        if (!isOnline()) {
            Snackbar.make(b.root, R.string.err_no_network, Snackbar.LENGTH_LONG).show()
            return
        }

        val t = Transaction(
            id = editingId ?: "",
            title = title,
            amount = amount!!,
            type = type,
            category = category,
            date = Timestamp(Date(dateMillis)),
            note = note
        )

        setLoading(true)
        val task = if (editingId == null) repo.add(t) else repo.update(t)
        task.addOnSuccessListener {
            setLoading(false)
            finish()
        }.addOnFailureListener {
            setLoading(false)
            Snackbar.make(b.root, R.string.err_generic, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun setLoading(loading: Boolean) {
        b.progress.isVisible = loading
        b.btnSave.isEnabled = !loading
    }
}