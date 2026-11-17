package com.example.kasku.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasku.R
import com.example.kasku.data.Categories
import com.example.kasku.data.Transaction
import com.example.kasku.databinding.FragmentTransactionsBinding
import com.example.kasku.util.Format

class TransactionsFragment : Fragment() {

    private var _b: FragmentTransactionsBinding? = null
    private val b get() = _b!!
    private val vm: TransactionViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _b = FragmentTransactionsBinding.inflate(inflater, container, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = TransactionAdapter { t ->
            startActivity(
                Intent(requireContext(), DetailActivity::class.java)
                    .putExtra(DetailActivity.EXTRA_ID, t.id)
            )
        }
        b.recycler.layoutManager = LinearLayoutManager(requireContext())
        b.recycler.adapter = adapter

        setupFilters()

        b.fabAdd.setOnClickListener {
            startActivity(Intent(requireContext(), FormActivity::class.java))
        }
        b.btnRetry.setOnClickListener { vm.load() }

        vm.allItems.observe(viewLifecycleOwner) { list ->
            val income = list.filter { it.type == Transaction.TYPE_INCOME }.sumOf { it.amount }
            val expense = list.filter { it.type == Transaction.TYPE_EXPENSE }.sumOf { it.amount }
            b.tvBalance.text = Format.rupiah(income - expense)
            b.tvIncome.text = Format.rupiah(income)
            b.tvExpense.text = Format.rupiah(expense)
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            val list = (state as? UiState.Success)?.data
            b.progress.isVisible = state is UiState.Loading
            b.layoutError.isVisible = state is UiState.Error
            b.recycler.isVisible = !list.isNullOrEmpty()
            b.tvEmpty.isVisible = list != null && list.isEmpty()
            b.tvEmpty.setText(
                if (vm.hasFilter()) R.string.empty_not_found else R.string.empty_no_data
            )
            adapter.submitList(list ?: emptyList())
        }
    }

    private fun setupFilters() {
        // Pencarian
        b.searchView.setQuery(vm.query, false)
        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?) = true
            override fun onQueryTextChange(newText: String?): Boolean {
                vm.setQuery(newText.orEmpty())
                return true
            }
        })

        // Filter tipe (chip)
        b.chipGroup.check(
            when (vm.type) {
                Transaction.TYPE_INCOME -> R.id.chipIncome
                Transaction.TYPE_EXPENSE -> R.id.chipExpense
                else -> R.id.chipAll
            }
        )
        b.chipGroup.setOnCheckedStateChangeListener { _, ids ->
            vm.setType(
                when (ids.firstOrNull()) {
                    R.id.chipIncome -> Transaction.TYPE_INCOME
                    R.id.chipExpense -> Transaction.TYPE_EXPENSE
                    else -> null
                }
            )
        }

        // Filter kategori
        val allCategories = getString(R.string.all_categories)
        val categoryItems = listOf(allCategories) + Categories.ALL
        b.actCategory.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categoryItems)
        )
        b.actCategory.setText(vm.category ?: allCategories, false)
        b.actCategory.setOnItemClickListener { _, _, position, _ ->
            vm.setCategory(if (position == 0) null else Categories.ALL[position - 1])
        }

        // Filter bulan
        val allMonths = getString(R.string.all_months)
        val monthNames = resources.getStringArray(R.array.months).toList()
        b.actMonth.setAdapter(
            ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf(allMonths) + monthNames)
        )
        b.actMonth.setText(vm.month?.let { monthNames[it] } ?: allMonths, false)
        b.actMonth.setOnItemClickListener { _, _, position, _ ->
            vm.setMonth(if (position == 0) null else position - 1)
        }
    }

    override fun onDestroyView() {
        _b = null
        super.onDestroyView()
    }
}