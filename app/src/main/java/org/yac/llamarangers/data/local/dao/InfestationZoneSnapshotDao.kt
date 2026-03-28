package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity

@Dao
interface InfestationZoneSnapshotDao {

    @Query("SELECT * FROM infestation_zone_snapshot WHERE zone_id = :zoneId ORDER BY snapshot_date DESC")
    fun observeByZoneId(zoneId: String): Flow<List<InfestationZoneSnapshotEntity>>

    @Query("SELECT * FROM infestation_zone_snapshot WHERE zone_id = :zoneId ORDER BY snapshot_date DESC")
    suspend fun fetchByZoneId(zoneId: String): List<InfestationZoneSnapshotEntity>

    @Query("SELECT * FROM infestation_zone_snapshot WHERE id = :id")
    suspend fun findById(id: String): InfestationZoneSnapshotEntity?

    @Upsert
    suspend fun upsert(entity: InfestationZoneSnapshotEntity)

    @Upsert
    suspend fun upsertAll(entities: List<InfestationZoneSnapshotEntity>)

    @Delete
    suspend fun delete(entity: InfestationZoneSnapshotEntity)

    @Query("DELETE FROM infestation_zone_snapshot WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM infestation_zone_snapshot")
    suspend fun deleteAll()
}
