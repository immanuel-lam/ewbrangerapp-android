package org.yac.llamarangers.service.sync

import java.util.Collections
import java.util.Date
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks pending sync operations in a queue.
 * Ports iOS SyncQueueManager. Operations are stored alongside entity saves
 * and processed when cloud sync becomes available (V3).
 */
@Singleton
class SyncQueueManager @Inject constructor() {

    companion object {
        const val FAILURE_THRESHOLD = 10
    }

    /**
     * Represents a pending sync queue entry.
     * In a full implementation this maps to a Room entity; kept as a data class
     * for the V2 PoC since cloud sync is a no-op.
     */
    data class SyncQueueEntry(
        val id: UUID = UUID.randomUUID(),
        val createdAt: Date = Date(),
        val entityName: String,
        val entityId: UUID,
        val operationType: String,
        val payload: ByteArray,
        var attemptCount: Int = 0,
        var lastAttemptAt: Date? = null,
        var lastErrorMessage: String? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is SyncQueueEntry) return false
            return id == other.id
        }

        override fun hashCode(): Int = id.hashCode()
    }

    private val queue: MutableList<SyncQueueEntry> =
        Collections.synchronizedList(mutableListOf())

    /**
     * Enqueue a new sync operation.
     */
    fun enqueue(
        entityName: String,
        entityId: UUID,
        operationType: String,
        payload: ByteArray
    ) {
        val entry = SyncQueueEntry(
            entityName = entityName,
            entityId = entityId,
            operationType = operationType,
            payload = payload
        )
        queue.add(entry)
    }

    /**
     * Returns all pending entries sorted by creation date (oldest first).
     */
    fun pendingEntries(): List<SyncQueueEntry> {
        synchronized(queue) {
            return queue.sortedBy { it.createdAt }
        }
    }

    /**
     * Mark an entry as attempted, optionally recording an error.
     */
    fun markAttempt(entry: SyncQueueEntry, error: String? = null) {
        entry.attemptCount += 1
        entry.lastAttemptAt = Date()
        entry.lastErrorMessage = error
    }

    /**
     * Remove a completed entry from the queue.
     */
    fun remove(entry: SyncQueueEntry) {
        queue.remove(entry)
    }

    /**
     * Returns true if any entries have exceeded the failure threshold.
     */
    val hasPersistentFailures: Boolean
        get() = synchronized(queue) { queue.any { it.attemptCount >= FAILURE_THRESHOLD } }

    /**
     * Number of pending sync operations.
     */
    val pendingSyncCount: Int
        get() = queue.size
}
