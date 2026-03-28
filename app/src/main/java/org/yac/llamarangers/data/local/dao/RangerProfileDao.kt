package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.RangerProfileEntity

@Dao
interface RangerProfileDao {

    @Query("SELECT * FROM ranger_profile ORDER BY display_name ASC")
    fun observeAll(): Flow<List<RangerProfileEntity>>

    @Query("SELECT * FROM ranger_profile ORDER BY display_name ASC")
    suspend fun fetchAll(): List<RangerProfileEntity>

    @Query("SELECT * FROM ranger_profile WHERE id = :id")
    suspend fun findById(id: String): RangerProfileEntity?

    @Query("SELECT * FROM ranger_profile WHERE is_current_device = 1 LIMIT 1")
    suspend fun fetchCurrentDevice(): RangerProfileEntity?

    @Upsert
    suspend fun upsert(entity: RangerProfileEntity)

    @Upsert
    suspend fun upsertAll(entities: List<RangerProfileEntity>)

    @Delete
    suspend fun delete(entity: RangerProfileEntity)

    @Query("DELETE FROM ranger_profile WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE ranger_profile SET is_current_device = 0")
    suspend fun clearAllCurrentDevice()

    @Query("UPDATE ranger_profile SET is_current_device = :isCurrent WHERE id = :id")
    suspend fun setCurrentDevice(id: String, isCurrent: Boolean)

    @Query("UPDATE ranger_profile SET display_name = :name, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateDisplayName(id: String, name: String, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM ranger_profile")
    suspend fun count(): Int
}
