package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sighting_log",
    foreignKeys = [
        ForeignKey(
            entity = RangerProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["ranger_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = InfestationZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["infestation_zone_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("ranger_id"),
        Index("infestation_zone_id"),
        Index("sync_status"),
        Index("created_at")
    ]
)
data class SightingLogEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val latitude: Double,
    val longitude: Double,
    @ColumnInfo(name = "horizontal_accuracy")
    val horizontalAccuracy: Double,
    val variant: String,
    @ColumnInfo(name = "infestation_size")
    val infestationSize: String,
    val notes: String?,
    @ColumnInfo(name = "photo_filenames")
    val photoFilenames: List<String>, // JSON array of strings, Room TypeConverter
    @ColumnInfo(name = "device_id")
    val deviceId: String?,
    @ColumnInfo(name = "server_id")
    val serverId: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int,
    @ColumnInfo(name = "ranger_id")
    val rangerId: String?,
    @ColumnInfo(name = "infestation_zone_id")
    val infestationZoneId: String?
)
