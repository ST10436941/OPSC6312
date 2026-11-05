package com.vetqueue.app.ui.controller

import com.vetqueue.app.data.local.entity.ClinicEntity
import com.vetqueue.app.data.repository.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ClinicController(clinicRepository: ClinicRepository) {

    private val _clinics =
        MutableStateFlow<List<ClinicEntity>>(emptyList())

    val clinics: StateFlow<List<ClinicEntity>> =
        _clinics.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> =
        _searchQuery.asStateFlow()

    fun setClinics(list: List<ClinicEntity>) {
        _clinics.value = list
    }

    fun search(query: String) {
        _searchQuery.value = query
    }

    fun getFilteredClinics(): List<ClinicEntity> {
        val query = _searchQuery.value.trim()

        if (query.isEmpty()) return _clinics.value

        return _clinics.value.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.address.contains(query, ignoreCase = true)
        }
    }
}