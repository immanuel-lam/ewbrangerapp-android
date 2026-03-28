package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity

@Dao
interface PesticideUsageRecordDao {

    @Query("SELECT * FROM pesticide_usage_record WHERE stock_id = :stockId ORDER BY used_at DESC")
    fun observeByStockId(stockId: String): Flow<List<PesticideUsageRecordEntity>>

    @Query("SELECT * FROM pesticide_usage_record WHERE stock_id = :stockId ORDER BY used_at DESC")
    suspend fun fetchByStockId(stockId: String): List<PesticideUsageRecordEntity>

    @Query("SELECT * FROM pesticide_usage_record WHERE id = :id")
    suspend fun findById(id: String): PesticideUsageRecordEntity?

    @Upsert
    suspend fun upsert(entity: PesticideUsageRecordEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PesticideUsageRecordEntity>)

    @Delete
    suspend fun delete(entity: PesticideUsageRecordEntity)

    @Query("DELETE FROM pesticide_usage_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM pesticide_usage_record")
    suspend fun deleteAll()

    @Query("SELECT SUM(used_quantity) FROM pesticide_usage_record WHERE stock_id = :stockId")
    suspend fun totalUsageForStock(stockId: String): Double?
}
