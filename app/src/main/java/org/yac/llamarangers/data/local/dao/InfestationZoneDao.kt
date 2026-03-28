package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity

@Dao
interface InfestationZoneDao {

    @Query("SELECT * FROM infestation_zone ORDER BY name ASC")
    fun observeAll(): Flow<List<InfestationZoneEntity>>

    @Query("SELECT * FROM infestation_zone ORDER BY name ASC")
    suspend fun fetchAll(): List<InfestationZoneEntity>

    @Query("SELECT * FROM infestation_zone WHERE id = :id")
    suspend fun findById(id: String): InfestationZoneEntity?

    @Upsert
    suspend fun upsert(entity: InfestationZoneEntity)

    @Upsert
    suspend fun upsertAll(entities: List<InfestationZoneEntity>)

    @Delete
    suspend fun delete(entity: InfestationZoneEntity)

    @Query("DELETE FROM infestation_zone WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM infestation_zone WHERE sync_status != 3 ORDER BY created_at ASC")
    suspend fun fetchUnsynced(): List<InfestationZoneEntity>

    @Query("SELECT COUNT(*) FROM infestation_zone")
    suspend fun count(): Int

    @Query("DELETE FROM infestation_zone")
    suspend fun deleteAll()

    @Query("UPDATE infestation_zone SET name = :name, dominant_variant = :dominantVariant, status = :status, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateZone(id: String, name: String?, dominantVariant: String, status: String, updatedAt: Long, syncStatus: Int)
}
