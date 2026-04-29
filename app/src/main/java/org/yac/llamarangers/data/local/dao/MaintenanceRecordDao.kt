package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.MaintenanceRecordEntity

@Dao
interface MaintenanceRecordDao {

    @Query("SELECT * FROM maintenance_record WHERE equipment_id = :equipmentId ORDER BY date DESC")
    fun observeByEquipmentId(equipmentId: String): Flow<List<MaintenanceRecordEntity>>

    @Query("SELECT * FROM maintenance_record WHERE equipment_id = :equipmentId ORDER BY date DESC")
    suspend fun fetchByEquipmentId(equipmentId: String): List<MaintenanceRecordEntity>

    @Query("SELECT * FROM maintenance_record WHERE id = :id")
    suspend fun findById(id: String): MaintenanceRecordEntity?

    @Upsert
    suspend fun upsert(entity: MaintenanceRecordEntity)

    @Delete
    suspend fun delete(entity: MaintenanceRecordEntity)

    @Query("DELETE FROM maintenance_record")
    suspend fun deleteAll()
}