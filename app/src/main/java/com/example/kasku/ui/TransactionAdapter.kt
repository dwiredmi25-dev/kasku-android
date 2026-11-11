package com.example.kasku.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kasku.R
import com.example.kasku.data.Transaction
import com.example.kasku.data.isIncome
import com.example.kasku.databinding.ItemTransactionBinding
import com.example.kasku.util.Format

class TransactionAdapter(
    private val onClick: (Transaction) -> Unit
) : ListAdapter<Transaction, TransactionAdapter.VH>(Diff) {

    class VH(val b: ItemTransactionBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = getItem(position)
        val ctx = holder.b.root.context
        holder.b.tvTitle.text = t.title
        holder.b.tvMeta.text = "${t.category} • ${Format.date(t.date)}"
        val sign = if (t.isIncome) "+ " else "- "
        holder.b.tvAmount.text = sign + Format.rupiah(t.amount)
        holder.b.tvAmount.setTextColor(
            ContextCompat.getColor(ctx, if (t.isIncome) R.color.income else R.color.expense)
        )
        holder.b.root.setOnClickListener { onClick(t) }
    }

    private object Diff : DiffUtil.ItemCallback<Transaction>() {
        override fun areItemsTheSame(a: Transaction, b: Transaction) = a.id == b.id
        override fun areContentsTheSame(a: Transaction, b: Transaction) = a == b
    }
}