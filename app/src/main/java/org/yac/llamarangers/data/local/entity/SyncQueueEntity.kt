package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_queue",
    indices = [
        Index("entity_name"),
        Index("entity_id"),
        Index("created_at")
    ]
)
data class SyncQueueEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "entity_name")
    val entityName: String,
    @ColumnInfo(name = "entity_id")
    val entityId: String,
    @ColumnInfo(name = "operation_type")
    val operationType: String,
    val payload: String?, // JSON string
    @ColumnInfo(name = "attempt_count")
    val attemptCount: Int,
    @ColumnInfo(name = "last_attempt_at")
    val lastAttemptAt: Long?,
    @ColumnInfo(name = "last_error_message")
    val lastErrorMessage: String?
)
