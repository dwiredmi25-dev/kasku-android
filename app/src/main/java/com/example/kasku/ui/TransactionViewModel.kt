package com.example.kasku.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kasku.data.Transaction
import com.example.kasku.data.TransactionRepository
import com.google.firebase.firestore.ListenerRegistration
import java.util.Calendar

class TransactionViewModel : ViewModel() {

    private val repo = TransactionRepository()
    private var registration: ListenerRegistration? = null

    private val _all = MutableLiveData<List<Transaction>>(emptyList())
    val allItems: LiveData<List<Transaction>> = _all

    private val _state = MutableLiveData<UiState<List<Transaction>>>(UiState.Loading)
    val state: LiveData<UiState<List<Transaction>>> = _state

    var query: String = ""; private set
    var type: String? = null; private set
    var category: String? = null; private set
    var month: Int? = null; private set

    init { load() }

    fun load() {
        _state.value = UiState.Loading
        registration?.remove()
        registration = repo.listenAll(
            onData = { _all.value = it; applyFilter() },
            onError = { _state.value = UiState.Error(it.message ?: "") }
        )
    }

    fun setQuery(q: String) { query = q; applyFilter() }
    fun setType(t: String?) { type = t; applyFilter() }
    fun setCategory(c: String?) { category = c; applyFilter() }
    fun setMonth(m: Int?) { month = m; applyFilter() }

    fun hasFilter(): Boolean =
        query.isNotBlank() || type != null || category != null || month != null

    private fun applyFilter() {
        if (_state.value is UiState.Error) return
        val q = query.trim().lowercase()
        val cal = Calendar.getInstance()
        val result = _all.value.orEmpty().filter { t ->
            val matchQuery = q.isEmpty() ||
                    t.title.lowercase().contains(q) ||
                    t.note.lowercase().contains(q)
            val matchType = type == null || t.type == type
            val matchCategory = category == null || t.category == category
            val matchMonth = month == null || run {
                cal.time = t.date.toDate()
                cal.get(Calendar.MONTH) == month
            }
            matchQuery && matchType && matchCategory && matchMonth
        }
        _state.value = UiState.Success(result)
    }

    override fun onCleared() {
        registration?.remove()
        super.onCleared()
    }
}
