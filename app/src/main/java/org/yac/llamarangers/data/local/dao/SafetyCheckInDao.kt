package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.SafetyCheckInEntity

@Dao
interface SafetyCheckInDao {

    @Query("SELECT * FROM safety_check_in ORDER BY start_time DESC")
    fun observeAll(): Flow<List<SafetyCheckInEntity>>

    @Query("SELECT * FROM safety_check_in ORDER BY start_time DESC")
    suspend fun fetchAll(): List<SafetyCheckInEntity>

    @Query("SELECT * FROM safety_check_in WHERE id = :id")
    suspend fun findById(id: String): SafetyCheckInEntity?

    @Upsert
    suspend fun upsert(entity: SafetyCheckInEntity)

    @Delete
    suspend fun delete(entity: SafetyCheckInEntity)

    @Query("DELETE FROM safety_check_in")
    suspend fun deleteAll()
}