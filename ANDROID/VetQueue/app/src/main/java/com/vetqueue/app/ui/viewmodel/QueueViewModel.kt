package com.vetqueue.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vetqueue.app.data.local.entity.QueueEntryEntity
import com.vetqueue.app.data.repository.NotificationRepository
import com.vetqueue.app.data.repository.QueueRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QueueViewModel(
    private val repository: QueueRepository,
    private val notificationRepository: NotificationRepository,
    private val userId: String
) : ViewModel() {

    val activeEntry: StateFlow<QueueEntryEntity?> = repository.observeActiveQueue(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun join(clinicId: String, petId: String) {
        viewModelScope.launch {
            val entry = repository.join(clinicId, userId, petId)
            notificationRepository.add(
                userId,
                "You are now #${entry.position} — est. wait ${entry.estWaitMinutes} min",
                "queue_update"
            )
        }
    }

    fun leave(entry: QueueEntryEntity) {
        viewModelScope.launch { repository.leave(entry) }
    }
}
