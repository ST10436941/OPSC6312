package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.local.entity.AppointmentEntity
import com.vetqueue.app.data.repository.AppointmentRepository
import com.vetqueue.app.data.repository.BookingResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BookingUiState(
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val success: Boolean = false
)

class AppointmentViewModel(
    private val repository: AppointmentRepository,
    private val userId: String
) : ViewModel() {

    val appointments: StateFlow<List<AppointmentEntity>> = repository.observeAppointments(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _bookingState = MutableStateFlow(BookingUiState())
    val bookingState: StateFlow<BookingUiState> = _bookingState.asStateFlow()

    fun book(petId: String, clinicId: String, service: String, date: String, time: String) {
        if (petId.isBlank() || clinicId.isBlank() || service.isBlank() || date.isBlank() || time.isBlank()) {
            _bookingState.value = BookingUiState(errorMessage = "Please complete every field before confirming.")
            return
        }
        _bookingState.value = BookingUiState(isSubmitting = true)
        viewModelScope.launch {
            when (val result = repository.book(userId, petId, clinicId, service, date, time)) {
                is BookingResult.Success -> _bookingState.value = BookingUiState(success = true)
                is BookingResult.Error -> _bookingState.value = BookingUiState(errorMessage = result.message)
            }
        }
    }

    fun cancel(appointment: AppointmentEntity) {
        viewModelScope.launch { repository.cancel(appointment) }
    }

    fun resetBookingState() {
        _bookingState.value = BookingUiState()
    }
}
