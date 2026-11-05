package com.vetqueue.app.data.repository

import com.vetqueue.app.data.local.dao.QueueDao
import com.vetqueue.app.data.local.entity.QueueEntryEntity
import com.vetqueue.app.data.remote.ApiService
import com.vetqueue.app.data.remote.dto.QueueJoinRequest
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class QueueRepository(
    private val api: ApiService,
    private val queueDao: QueueDao,
    private val clinicRepository: ClinicRepository
) {
    fun observeActiveQueue(userId: String): Flow<QueueEntryEntity?> = queueDao.observeActiveForUser(userId)

    suspend fun join(clinicId: String, userId: String, petId: String): QueueEntryEntity {
        val clinicBaseline = clinicRepository.getById(clinicId)?.currentQueueCount ?: 0
        val appTrackedWaiting = queueDao.countWaiting(clinicId)
        val position = clinicBaseline + appTrackedWaiting + 1

        val entry = QueueEntryEntity(
            queueId = UUID.randomUUID().toString(),
            clinicId = clinicId,
            userId = userId,
            petId = petId,
            queueNumber = position,
            position = position,
            estWaitMinutes = estimateWaitMinutes(position),
            status = "waiting"
        )
        queueDao.insert(entry)
        runCatching { api.joinQueue(QueueJoinRequest(clinicId, userId, petId)) }
        return entry
    }

    suspend fun leave(entry: QueueEntryEntity) {
        queueDao.update(entry.copy(status = "left"))
        runCatching { api.leaveQueue(entry.queueId) }
    }

    companion object {
        fun estimateWaitMinutes(position: Int): Int = position * 8
    }
}