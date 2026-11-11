package com.example.kasku.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.kasku.R
import com.example.kasku.data.Transaction
import com.example.kasku.databinding.FragmentSummaryBinding
import com.example.kasku.util.Format
import java.util.Calendar

class SummaryFragment : Fragment() {

    private var _b: FragmentSummaryBinding? = null
    private val b get() = _b!!
    private val vm: TransactionViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentSummaryBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.allItems.observe(viewLifecycleOwner) { render(it) }
    }

    private fun sumOf(items: List<Transaction>, type: String): Long =
        items.filter { it.type == type }.sumOf { it.amount }

    private fun render(list: List<Transaction>) {
        val income = sumOf(list, Transaction.TYPE_INCOME)
        val expense = sumOf(list, Transaction.TYPE_EXPENSE)
        b.tvIncome.text = Format.rupiah(income)
        b.tvExpense.text = Format.rupiah(expense)
        b.tvNet.text = Format.rupiah(income - expense)

        val hasData = list.isNotEmpty()
        b.tvEmpty.isVisible = !hasData
        b.layoutChart.isVisible = hasData
        b.layoutMonthly.isVisible = hasData

        val cal = Calendar.getInstance()
        val monthNames = resources.getStringArray(R.array.months)
        val groups = list.groupBy { t ->
            cal.time = t.date.toDate()
            cal.get(Calendar.YEAR) * 12 + cal.get(Calendar.MONTH)
        }

        // Grafik: 6 bulan kalender terakhir (bulan tanpa data tetap tampil bernilai 0).
        val entries = (5 downTo 0).map { back ->
            val c = Calendar.getInstance().apply { add(Calendar.MONTH, -back) }
            val items = groups[c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)].orEmpty()
            MonthlyBarChart.Entry(
                monthNames[c.get(Calendar.MONTH)].take(3),
                sumOf(items, Transaction.TYPE_INCOME),
                sumOf(items, Transaction.TYPE_EXPENSE)
            )
        }
        b.chart.setData(entries)

        // Rekap per bulan, dari yang terbaru.
        b.layoutMonthly.removeAllViews()
        val pad = (12 * resources.displayMetrics.density).toInt()
        groups.toSortedMap(reverseOrder()).forEach { (key, items) ->
            val tv = TextView(requireContext()).apply {
                text = getString(
                    R.string.summary_line,
                    "${monthNames[key % 12]} ${key / 12}",
                    Format.rupiah(sumOf(items, Transaction.TYPE_INCOME)),
                    Format.rupiah(sumOf(items, Transaction.TYPE_EXPENSE))
                )
                setPadding(0, pad, 0, pad)
            }
            b.layoutMonthly.addView(tv)
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}