package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.local.entity.ClinicEntity
import com.vetqueue.app.data.repository.ClinicRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ClinicViewModel(private val repository: ClinicRepository) : ViewModel() {

    private val query = MutableStateFlow("")

    val clinics: StateFlow<List<ClinicEntity>> = query
        .debounce(200)
        .flatMapLatest { q -> if (q.isBlank()) repository.observeClinics() else repository.search(q) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { repository.refresh() }
    }

    fun onQueryChange(newQuery: String) {
        query.value = newQuery
    }

    fun refresh() {
        viewModelScope.launch { repository.refresh() }
    }
}
