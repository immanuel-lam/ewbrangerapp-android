package org.yac.llamarangers.data.repository

import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.dao.PesticideStockDao
import org.yac.llamarangers.data.local.dao.PesticideUsageRecordDao
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.domain.model.enums.SyncStatus
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PesticideRepository @Inject constructor(
    private val stockDao: PesticideStockDao,
    private val usageDao: PesticideUsageRecordDao,
    private val rangerDao: RangerProfileDao,
    private val syncQueueDao: SyncQueueDao
) {

    fun observeAllStocks(): Flow<List<PesticideStockEntity>> =
        stockDao.observeAll()

    suspend fun fetchAllStocks(): List<PesticideStockEntity> =
        stockDao.fetchAll()

    suspend fun addStock(
        productName: String,
        unit: String,
        initialQuantity: Double,
        minThreshold: Double
    ): PesticideStockEntity {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val entity = PesticideStockEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            productName = productName,
            unit = unit,
            currentQuantity = initialQuantity,
            minThreshold = minThreshold,
            syncStatus = SyncStatus.PENDING_CREATE.value
        )

        stockDao.upsert(entity)

        syncQueueDao.upsert(
            SyncQueueEntity(
                id = UUID.randomUUID().toString(),
                createdAt = now,
                entityName = "PesticideStock",
                entityId = id,
                operationType = "create",
                payload = null,
                attemptCount = 0,
                lastAttemptAt = null,
                lastErrorMessage = null
            )
        )

        return entity
    }

    suspend fun logUsage(
        stockId: String,
        quantity: Double,
        notes: String?,
        rangerId: String
    ) {
        val now = System.currentTimeMillis()
        val id = UUID.randomUUID().toString()

        val usage = PesticideUsageRecordEntity(
            id = id,
            createdAt = now,
            updatedAt = now,
            usedQuantity = quantity,
            usedAt = now,
            notes = notes,
            syncStatus = SyncStatus.PENDING_CREATE.value,
            stockId = stockId,
            treatmentId = null,
            rangerId = rangerId
        )

        usageDao.upsert(usage)

        // Recompute stock quantity
        val stock = stockDao.findById(stockId) ?: return
        val totalUsed = usageDao.totalUsageForStock(stockId) ?: 0.0
        // currentQuantity = initial quantity minus total usage
        // Since we don't track initial separately, just subtract this usage
        val newQuantity = (stock.currentQuantity - quantity).coerceAtLeast(0.0)
        stockDao.updateQuantity(stockId, newQuantity, now, SyncStatus.PENDING_UPDATE.value)
    }

    suspend fun fetchUsageHistory(stockId: String): List<PesticideUsageRecordEntity> =
        usageDao.fetchByStockId(stockId)

    fun observeUsageHistory(stockId: String): Flow<List<PesticideUsageRecordEntity>> =
        usageDao.observeByStockId(stockId)
}
