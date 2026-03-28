package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ranger_profile",
    indices = [
        Index("sync_status"),
        Index("supabase_uid"),
        Index("is_current_device")
    ]
)
data class RangerProfileEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "supabase_uid")
    val supabaseUid: String?,
    @ColumnInfo(name = "display_name")
    val displayName: String?,
    val role: String?,
    @ColumnInfo(name = "is_current_device")
    val isCurrentDevice: Boolean,
    @ColumnInfo(name = "avatar_filename")
    val avatarFilename: String?,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int
)
