package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.HazardLogEntity

@Dao
interface HazardLogDao {

    @Query("SELECT * FROM hazard_log ORDER BY timestamp DESC")
    fun observeAll(): Flow<List<HazardLogEntity>>

    @Query("SELECT * FROM hazard_log ORDER BY timestamp DESC")
    suspend fun fetchAll(): List<HazardLogEntity>

    @Query("SELECT * FROM hazard_log WHERE id = :id")
    suspend fun findById(id: String): HazardLogEntity?

    @Upsert
    suspend fun upsert(entity: HazardLogEntity)

    @Delete
    suspend fun delete(entity: HazardLogEntity)

    @Query("DELETE FROM hazard_log")
    suspend fun deleteAll()
}