package org.yac.llamarangers.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import org.yac.llamarangers.data.local.entity.TreatmentFollowUpEntity

@Dao
interface TreatmentFollowUpDao {

    @Query("SELECT * FROM treatment_follow_up WHERE treatment_id = :treatmentId ORDER BY follow_up_date DESC")
    fun observeByTreatmentId(treatmentId: String): Flow<List<TreatmentFollowUpEntity>>

    @Query("SELECT * FROM treatment_follow_up WHERE treatment_id = :treatmentId ORDER BY follow_up_date DESC")
    suspend fun fetchByTreatmentId(treatmentId: String): List<TreatmentFollowUpEntity>

    @Query("SELECT * FROM treatment_follow_up WHERE id = :id")
    suspend fun findById(id: String): TreatmentFollowUpEntity?

    @Upsert
    suspend fun upsert(entity: TreatmentFollowUpEntity)

    @Delete
    suspend fun delete(entity: TreatmentFollowUpEntity)

    @Query("DELETE FROM treatment_follow_up")
    suspend fun deleteAll()
}