package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "treatment_follow_up",
    foreignKeys = [
        ForeignKey(
            entity = TreatmentRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["treatment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("treatment_id"),
        Index("follow_up_date")
    ]
)
data class TreatmentFollowUpEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "follow_up_date")
    val followUpDate: Long?,
    @ColumnInfo(name = "percent_dead")
    val percentDead: Double,
    @ColumnInfo(name = "regrowth_level")
    val regrowthLevel: String?,
    val notes: String?,
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "treatment_id")
    val treatmentId: String?
)