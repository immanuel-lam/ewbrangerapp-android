package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.PesticideStockEntity

@Dao
interface PesticideStockDao {

    @Query("SELECT * FROM pesticide_stock ORDER BY product_name ASC")
    fun observeAll(): Flow<List<PesticideStockEntity>>

    @Query("SELECT * FROM pesticide_stock ORDER BY product_name ASC")
    suspend fun fetchAll(): List<PesticideStockEntity>

    @Query("SELECT * FROM pesticide_stock WHERE id = :id")
    suspend fun findById(id: String): PesticideStockEntity?

    @Upsert
    suspend fun upsert(entity: PesticideStockEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PesticideStockEntity>)

    @Delete
    suspend fun delete(entity: PesticideStockEntity)

    @Query("DELETE FROM pesticide_stock WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pesticide_stock")
    suspend fun deleteAll()

    @Query("UPDATE pesticide_stock SET current_quantity = :quantity, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateQuantity(id: String, quantity: Double, updatedAt: Long, syncStatus: Int)

    @Query("SELECT COUNT(*) FROM pesticide_stock")
    suspend fun count(): Int
}
