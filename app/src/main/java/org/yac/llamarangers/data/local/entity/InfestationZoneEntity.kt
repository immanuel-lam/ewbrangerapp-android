package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "infestation_zone",
    indices = [
        Index("sync_status"),
        Index("name")
    ]
)
data class InfestationZoneEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    val name: String?,
    val status: String?,
    @ColumnInfo(name = "dominant_variant")
    val dominantVariant: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int
)
