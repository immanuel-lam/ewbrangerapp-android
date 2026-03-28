package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ranger_task",
    foreignKeys = [
        ForeignKey(
            entity = RangerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["assigned_ranger_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TreatmentRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["source_treatment_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("assigned_ranger_id"),
        Index("source_treatment_id"),
        Index("sync_status"),
        Index("due_date"),
        Index("is_complete")
    ]
)
data class RangerTaskEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val title: String,
    val notes: String?,
    val priority: String,
    @ColumnInfo(name = "due_date")
    val dueDate: Long?,
    @ColumnInfo(name = "is_complete")
    val isComplete: Boolean,
    @ColumnInfo(name = "completed_at")
    val completedAt: Long?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "assigned_ranger_id")
    val assignedRangerId: String?,
    @ColumnInfo(name = "source_treatment_id")
    val sourceTreatmentId: String?
)
