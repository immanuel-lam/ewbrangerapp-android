package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.SightingLogEntity

@Dao
interface SightingLogDao {

    @Query("SELECT * FROM sighting_log ORDER BY created_at DESC")
    fun observeAll(): Flow<List<SightingLogEntity>>

    @Query("SELECT * FROM sighting_log ORDER BY created_at DESC")
    suspend fun fetchAll(): List<SightingLogEntity>

    @Query("SELECT * FROM sighting_log WHERE id = :id")
    suspend fun findById(id: String): SightingLogEntity?

    @Upsert
    suspend fun upsert(entity: SightingLogEntity)

    @Upsert
    suspend fun upsertAll(entities: List<SightingLogEntity>)

    @Delete
    suspend fun delete(entity: SightingLogEntity)

    @Query("DELETE FROM sighting_log WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sighting_log WHERE ranger_id = :rangerId ORDER BY created_at DESC")
    suspend fun fetchByRangerId(rangerId: String): List<SightingLogEntity>

    @Query("SELECT * FROM sighting_log WHERE ranger_id = :rangerId ORDER BY created_at DESC")
    fun observeByRangerId(rangerId: String): Flow<List<SightingLogEntity>>

    @Query("SELECT * FROM sighting_log WHERE infestation_zone_id = :zoneId ORDER BY created_at DESC")
    suspend fun fetchByZoneId(zoneId: String): List<SightingLogEntity>

    @Query("SELECT * FROM sighting_log WHERE sync_status != 3 ORDER BY created_at ASC")
    suspend fun fetchUnsynced(): List<SightingLogEntity>

    @Query("SELECT * FROM sighting_log WHERE created_at >= :sinceMillis ORDER BY created_at DESC")
    suspend fun fetchSince(sinceMillis: Long): List<SightingLogEntity>

    @Query("SELECT COUNT(*) FROM sighting_log")
    suspend fun count(): Int

    @Query("UPDATE sighting_log SET infestation_zone_id = :zoneId, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateZoneAssignment(id: String, zoneId: String?, updatedAt: Long)

    @Query("DELETE FROM sighting_log")
    suspend fun deleteAll()
}
