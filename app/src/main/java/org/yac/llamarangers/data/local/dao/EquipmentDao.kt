package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.EquipmentEntity

@Dao
interface EquipmentDao {

    @Query("SELECT * FROM equipment ORDER BY name ASC")
    fun observeAll(): Flow<List<EquipmentEntity>>

    @Query("SELECT * FROM equipment ORDER BY name ASC")
    suspend fun fetchAll(): List<EquipmentEntity>

    @Query("SELECT * FROM equipment WHERE id = :id")
    suspend fun findById(id: String): EquipmentEntity?

    @Upsert
    suspend fun upsert(entity: EquipmentEntity)

    @Delete
    suspend fun delete(entity: EquipmentEntity)

    @Query("DELETE FROM equipment")
    suspend fun deleteAll()
}