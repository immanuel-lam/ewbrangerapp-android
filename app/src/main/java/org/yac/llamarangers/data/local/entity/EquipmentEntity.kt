package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "equipment")
data class EquipmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long?,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long?,
    val name: String?,
    @ColumnInfo(name = "equipment_type")
    val equipmentType: String?,
    @ColumnInfo(name = "serial_number")
    val serialNumber: String?,
    val notes: String?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    @ColumnInfo(name = "last_maintenance_date")
    val lastMaintenanceDate: Long?,
    @ColumnInfo(name = "next_maintenance_due")
    val nextMaintenanceDue: Long?
)