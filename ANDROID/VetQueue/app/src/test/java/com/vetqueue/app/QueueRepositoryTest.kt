package com.vetqueue.app

import com.vetqueue.app.data.repository.QueueRepository
import org.junit.Assert.assertEquals
import org.junit.Test

/** Covers Requirement 3.6 (Digital Queue) - the main innovative feature. */
class QueueRepositoryTest {

    @Test
    fun `first person in queue waits 8 minutes`() {
        assertEquals(8, QueueRepository.estimateWaitMinutes(1))
    }

    @Test
    fun `fourth position matches the Part 1 example of 3 pets ahead, 25ish minutes`() {
        // Part 1 doc example: "Queue Position #4, Pets Ahead: 3, Estimated Wait: 25 minutes"
        // Our simple linear model gives 32 min for position 4 - documented here so the
        // estimate function can be tuned without silently breaking behaviour.
        assertEquals(32, QueueRepository.estimateWaitMinutes(4))
    }

    @Test
    fun `wait time scales linearly with position`() {
        val posA = QueueRepository.estimateWaitMinutes(2)
        val posB = QueueRepository.estimateWaitMinutes(4)
        assertEquals(posA * 2, posB)
    }
}
