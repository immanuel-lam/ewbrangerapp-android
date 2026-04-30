package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "safety_check_in")
data class SafetyCheckInEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "start_time")
    val startTime: Long?,
    @ColumnInfo(name = "interval_minutes")
    val intervalMinutes: Int,
    @ColumnInfo(name = "last_check_in_time")
    val lastCheckInTime: Long?,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean,
    val notes: String?
)