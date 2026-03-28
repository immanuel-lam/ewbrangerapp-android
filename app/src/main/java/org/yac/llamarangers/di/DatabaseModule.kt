package org.yac.llamarangers.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
import org.yac.llamarangers.data.local.db.AppDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "llama_rangers.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideRangerProfileDao(db: AppDatabase): RangerProfileDao = db.rangerProfileDao()
    @Provides fun provideSightingLogDao(db: AppDatabase): SightingLogDao = db.sightingLogDao()
    @Provides fun provideTreatmentRecordDao(db: AppDatabase): TreatmentRecordDao = db.treatmentRecordDao()
    @Provides fun provideRangerTaskDao(db: AppDatabase): RangerTaskDao = db.rangerTaskDao()
    @Provides fun provideInfestationZoneDao(db: AppDatabase): InfestationZoneDao = db.infestationZoneDao()
    @Provides fun provideInfestationZoneSnapshotDao(db: AppDatabase): InfestationZoneSnapshotDao = db.infestationZoneSnapshotDao()
    @Provides fun providePatrolRecordDao(db: AppDatabase): PatrolRecordDao = db.patrolRecordDao()
    @Provides fun providePesticideStockDao(db: AppDatabase): PesticideStockDao = db.pesticideStockDao()
    @Provides fun providePesticideUsageRecordDao(db: AppDatabase): PesticideUsageRecordDao = db.pesticideUsageRecordDao()
    @Provides fun provideSyncQueueDao(db: AppDatabase): SyncQueueDao = db.syncQueueDao()
}
