package org.yac.llamarangers.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.yac.llamarangers.data.local.dao.InfestationZoneDao
import org.yac.llamarangers.data.local.dao.InfestationZoneSnapshotDao
import org.yac.llamarangers.data.local.dao.PatrolRecordDao
import org.yac.llamarangers.data.local.dao.PesticideStockDao
import org.yac.llamarangers.data.local.dao.PesticideUsageRecordDao
import org.yac.llamarangers.data.local.dao.RangerProfileDao
import org.yac.llamarangers.data.local.dao.RangerTaskDao
import org.yac.llamarangers.data.local.dao.SightingLogDao
import org.yac.llamarangers.data.local.dao.SyncQueueDao
import org.yac.llamarangers.data.local.dao.TreatmentRecordDao
import org.yac.llamarangers.data.local.dao.EquipmentDao
import org.yac.llamarangers.data.local.dao.HazardLogDao
import org.yac.llamarangers.data.local.dao.MaintenanceRecordDao
import org.yac.llamarangers.data.local.dao.SafetyCheckInDao
import org.yac.llamarangers.data.local.dao.TreatmentFollowUpDao
import org.yac.llamarangers.data.local.entity.InfestationZoneEntity
import org.yac.llamarangers.data.local.entity.InfestationZoneSnapshotEntity
import org.yac.llamarangers.data.local.entity.PatrolRecordEntity
import org.yac.llamarangers.data.local.entity.PesticideStockEntity
import org.yac.llamarangers.data.local.entity.PesticideUsageRecordEntity
import org.yac.llamarangers.data.local.entity.RangerProfileEntity
import org.yac.llamarangers.data.local.entity.RangerTaskEntity
import org.yac.llamarangers.data.local.entity.SightingLogEntity
import org.yac.llamarangers.data.local.entity.SyncQueueEntity
import org.yac.llamarangers.data.local.entity.TreatmentRecordEntity
import org.yac.llamarangers.data.local.entity.EquipmentEntity
import org.yac.llamarangers.data.local.entity.HazardLogEntity
import org.yac.llamarangers.data.local.entity.MaintenanceRecordEntity
import org.yac.llamarangers.data.local.entity.SafetyCheckInEntity
import org.yac.llamarangers.data.local.entity.TreatmentFollowUpEntity

@Database(
    entities = [
        RangerProfileEntity::class,
        SightingLogEntity::class,
        TreatmentRecordEntity::class,
        RangerTaskEntity::class,
        InfestationZoneEntity::class,
        InfestationZoneSnapshotEntity::class,
        PatrolRecordEntity::class,
        PesticideStockEntity::class,
        PesticideUsageRecordEntity::class,
        SyncQueueEntity::class,
        EquipmentEntity::class,
        HazardLogEntity::class,
        MaintenanceRecordEntity::class,
        SafetyCheckInEntity::class,
        TreatmentFollowUpEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rangerProfileDao(): RangerProfileDao
    abstract fun sightingLogDao(): SightingLogDao
    abstract fun treatmentRecordDao(): TreatmentRecordDao
    abstract fun rangerTaskDao(): RangerTaskDao
    abstract fun infestationZoneDao(): InfestationZoneDao
    abstract fun infestationZoneSnapshotDao(): InfestationZoneSnapshotDao
    abstract fun patrolRecordDao(): PatrolRecordDao
    abstract fun pesticideStockDao(): PesticideStockDao
    abstract fun pesticideUsageRecordDao(): PesticideUsageRecordDao
    abstract fun syncQueueDao(): SyncQueueDao
    abstract fun equipmentDao(): EquipmentDao
    abstract fun hazardLogDao(): HazardLogDao
    abstract fun maintenanceRecordDao(): MaintenanceRecordDao
    abstract fun safetyCheckInDao(): SafetyCheckInDao
    abstract fun treatmentFollowUpDao(): TreatmentFollowUpDao
}
