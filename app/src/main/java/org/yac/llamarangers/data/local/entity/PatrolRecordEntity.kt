package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "patrol_record",
    foreignKeys = [
        ForeignKey(
            entity = RangerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["ranger_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("ranger_id"),
        Index("sync_status"),
        Index("patrol_date")
    ]
)
data class PatrolRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "patrol_date")
    val patrolDate: Long,
    @ColumnInfo(name = "start_time")
    val startTime: Long,
    @ColumnInfo(name = "end_time")
    val endTime: Long?,
    @ColumnInfo(name = "area_name")
    val areaName: String?,
    @ColumnInfo(name = "checklist_items")
    val checklistItems: String, // JSON: [PatrolChecklistItem]
    val notes: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "ranger_id")
    val rangerId: String?
)
