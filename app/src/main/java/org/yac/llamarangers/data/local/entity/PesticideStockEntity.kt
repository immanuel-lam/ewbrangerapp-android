package org.yac.llamarangers.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pesticide_stock",
    indices = [
        Index("sync_status"),
        Index("product_name")
    ]
)
data class PesticideStockEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,
    @ColumnInfo(name = "product_name")
    val productName: String?,
    val unit: String?,
    @ColumnInfo(name = "current_quantity")
    val currentQuantity: Double,
    @ColumnInfo(name = "min_threshold")
    val minThreshold: Double,
    @ColumnInfo(name = "sync_status")
    val syncStatus: Int
)
