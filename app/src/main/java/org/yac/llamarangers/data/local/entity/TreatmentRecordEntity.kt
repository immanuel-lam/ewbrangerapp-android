package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatment_record",
    foreignKeys = [
        ForeignKey(
            entity = SightingLogEntity::class,
            parentColumns = ["id"],
            childColumns = ["sighting_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RangerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["ranger_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RangerTaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["follow_up_task_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("sighting_id"),
        Index("ranger_id"),
        Index("follow_up_task_id"),
        Index("sync_status"),
        Index("treatment_date")
    ]
)
data class TreatmentRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "treatment_date")
    val treatmentDate: Long,
    val method: String,
    @ColumnInfo(name = "herbicide_product")
    val herbicideProduct: String?,
    @ColumnInfo(name = "outcome_notes")
    val outcomeNotes: String?,
    @ColumnInfo(name = "follow_up_date")
    val followUpDate: Long?,
    @ColumnInfo(name = "photo_filenames")
    val photoFilenames: List<String>, // JSON array of strings, Room TypeConverter
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "sighting_id")
    val sightingId: String?,
    @ColumnInfo(name = "ranger_id")
    val rangerId: String?,
    @ColumnInfo(name = "follow_up_task_id")
    val followUpTaskId: String?
)
