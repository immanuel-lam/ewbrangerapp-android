package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity

@Dao
interface TreatmentRecordDao {

    @Query("SELECT * FROM treatment_record ORDER BY treatment_date DESC")
    fun observeAll(): Flow<List<TreatmentRecordEntity>>

    @Query("SELECT * FROM treatment_record ORDER BY treatment_date DESC")
    suspend fun fetchAll(): List<TreatmentRecordEntity>

    @Query("SELECT * FROM treatment_record WHERE id = :id")
    suspend fun findById(id: String): TreatmentRecordEntity?

    @Upsert
    suspend fun upsert(entity: TreatmentRecordEntity)

    @Upsert
    suspend fun upsertAll(entities: List<TreatmentRecordEntity>)

    @Delete
    suspend fun delete(entity: TreatmentRecordEntity)

    @Query("DELETE FROM treatment_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM treatment_record WHERE ranger_id = :rangerId ORDER BY treatment_date DESC")
    suspend fun fetchByRangerId(rangerId: String): List<TreatmentRecordEntity>

    @Query("SELECT * FROM treatment_record WHERE sighting_id = :sightingId ORDER BY treatment_date DESC")
    suspend fun fetchBySightingId(sightingId: String): List<TreatmentRecordEntity>

    @Query("SELECT * FROM treatment_record WHERE sighting_id = :sightingId ORDER BY treatment_date DESC")
    fun observeBySightingId(sightingId: String): Flow<List<TreatmentRecordEntity>>

    @Query("SELECT * FROM treatment_record WHERE follow_up_date IS NOT NULL AND follow_up_date <= :beforeMillis ORDER BY follow_up_date ASC")
    suspend fun fetchWithFollowUpBefore(beforeMillis: Long): List<TreatmentRecordEntity>

    @Query("SELECT * FROM treatment_record WHERE sync_status != 3 ORDER BY created_at ASC")
    suspend fun fetchUnsynced(): List<TreatmentRecordEntity>

    @Query("SELECT * FROM treatment_record WHERE created_at >= :sinceMillis ORDER BY created_at DESC")
    suspend fun fetchSince(sinceMillis: Long): List<TreatmentRecordEntity>

    @Query("SELECT COUNT(*) FROM treatment_record")
    suspend fun count(): Int

    @Query("DELETE FROM treatment_record")
    suspend fun deleteAll()

    @Query("SELECT * FROM treatment_record WHERE created_at >= :sinceMillis AND created_at < :untilMillis ORDER BY created_at DESC")
    suspend fun fetchBetween(sinceMillis: Long, untilMillis: Long): List<TreatmentRecordEntity>
}
