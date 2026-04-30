package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "maintenance_record",
    foreignKeys = [
        ForeignKey(
            entity = EquipmentEntity::class,
            parentColumns = ["id"],
            childColumns = ["equipment_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("equipment_id"),
        Index("date")
    ]
)
data class MaintenanceRecordEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "equipment_id")
    val equipmentId: String?,
    @ColumnInfo(name = "maintenance_type")
    val maintenanceType: String,
    @ColumnInfo(name = "description_text")
    val descriptionText: String,
    @ColumnInfo(name = "performed_by")
    val performedBy: String,
    @ColumnInfo(name = "cost_amount")
    val costAmount: Double,
    val date: Long
)