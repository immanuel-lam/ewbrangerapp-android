package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity

@Dao
interface PatrolRecordDao {

    @Query("SELECT * FROM patrol_record ORDER BY patrol_date DESC")
    fun observeAll(): Flow<List<PatrolRecordEntity>>

    @Query("SELECT * FROM patrol_record ORDER BY patrol_date DESC")
    suspend fun fetchAll(): List<PatrolRecordEntity>

    @Query("SELECT * FROM patrol_record WHERE id = :id")
    suspend fun findById(id: String): PatrolRecordEntity?

    @Query("SELECT * FROM patrol_record WHERE ranger_id = :rangerId AND end_time IS NULL LIMIT 1")
    suspend fun fetchActivePatrol(rangerId: String): PatrolRecordEntity?

    @Upsert
    suspend fun upsert(entity: PatrolRecordEntity)

    @Upsert
    suspend fun upsertAll(entities: List<PatrolRecordEntity>)

    @Delete
    suspend fun delete(entity: PatrolRecordEntity)

    @Query("DELETE FROM patrol_record WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM patrol_record")
    suspend fun deleteAll()

    @Query("UPDATE patrol_record SET checklist_items = :checklistJson, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun updateChecklist(id: String, checklistJson: String, updatedAt: Long, syncStatus: Int)

    @Query("UPDATE patrol_record SET end_time = :endTime, updated_at = :updatedAt, sync_status = :syncStatus WHERE id = :id")
    suspend fun finishPatrol(id: String, endTime: Long, updatedAt: Long, syncStatus: Int)

    @Query("SELECT COUNT(*) FROM patrol_record")
    suspend fun count(): Int
}
