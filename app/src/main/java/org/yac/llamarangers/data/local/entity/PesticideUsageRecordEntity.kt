package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pesticide_usage_record",
    foreignKeys = [
        ForeignKey(
            entity = PesticideStockEntity::class,
            parentColumns = ["id"],
            childColumns = ["stock_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = TreatmentRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["treatment_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = RangerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["ranger_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("stock_id"),
        Index("treatment_id"),
        Index("ranger_id"),
        Index("sync_status"),
        Index("used_at")
    ]
)
data class PesticideUsageRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "used_quantity")
    val usedQuantity: Double,
    @ColumnInfo(name = "used_at")
    val usedAt: Long?,
    val notes: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "stock_id")
    val stockId: String?,
    @ColumnInfo(name = "treatment_id")
    val treatmentId: String?,
    @ColumnInfo(name = "ranger_id")
    val rangerId: String?
)
