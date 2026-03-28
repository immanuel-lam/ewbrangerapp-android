package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "infestation_zone_snapshot",
    foreignKeys = [
        ForeignKey(
            entity = InfestationZoneEntity::class,
            parentColumns = ["id"],
            childColumns = ["zone_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("zone_id"),
        Index("snapshot_date")
    ]
)
data class InfestationZoneSnapshotEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "snapshot_date")
    val snapshotDate: Long,
    @ColumnInfo(name = "polygon_coordinates")
    val polygonCoordinates: List<List<Double>>, // JSON: [[lat, lon], ...], Room TypeConverter
    val area: Double,
    @ColumnInfo(name = "created_by_ranger_id")
    val createdByRangerId: String?,
    @ColumnInfo(name = "zone_id")
    val zoneId: String?
)
