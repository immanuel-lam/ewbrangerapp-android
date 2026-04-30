package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hazard_log")
data class HazardLogEntity(
    @PrimaryKey
    val id: String,
    val timestamp: Long?,
    val title: String?,
    @ColumnInfo(name = "hazard_type")
    val hazardType: String?,
    val severity: String?,
    val notes: String?,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "photo_path")
    val photoPath: String?,
    @ColumnInfo(name = "synced_to_cloud")
    val syncedToCloud: Boolean
)