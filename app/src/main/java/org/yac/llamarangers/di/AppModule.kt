package org.yac.llamarangers.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.yac.llamarangers.service.auth.AuthManager
import org.yac.llamarangers.service.auth.SecureStorageService
import org.yac.llamarangers.service.location.LocationManager
import org.yac.llamarangers.service.map.OfflineTileManager
import org.yac.llamarangers.service.sync.ConflictResolver
import org.yac.llamarangers.service.sync.MeshSyncEngine
import org.yac.llamarangers.service.sync.PhotoUploadManager
import org.yac.llamarangers.service.sync.SyncEngine
import org.yac.llamarangers.service.sync.SyncQueueManager
import javax.inject.Singleton

/**
 * Hilt DI module providing all service-layer singletons.
 *
 * Room database and DAOs will be added here once the data layer entities
 * and database class are created.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // --- Room Database ---
    // TODO: Uncomment when LlamaRangersDatabase is created
    //
    // @Provides
    // @Singleton
    // fun provideDatabase(@ApplicationContext context: Context): LlamaRangersDatabase {
    //     return Room.databaseBuilder(
    //         context,
    //         LlamaRangersDatabase::class.java,
    //         "llama_rangers.db"
    //     ).build()
    // }
    //
    // @Provides fun provideSightingLogDao(db: LlamaRangersDatabase) = db.sightingLogDao()
    // @Provides fun providePatrolRecordDao(db: LlamaRangersDatabase) = db.patrolRecordDao()
    // @Provides fun provideRangerProfileDao(db: LlamaRangersDatabase) = db.rangerProfileDao()
    // @Provides fun provideTreatmentRecordDao(db: LlamaRangersDatabase) = db.treatmentRecordDao()
    // @Provides fun provideRangerTaskDao(db: LlamaRangersDatabase) = db.rangerTaskDao()
    // @Provides fun provideInfestationZoneDao(db: LlamaRangersDatabase) = db.infestationZoneDao()
    // @Provides fun providePesticideStockDao(db: LlamaRangersDatabase) = db.pesticideStockDao()
    // @Provides fun provideSyncQueueDao(db: LlamaRangersDatabase) = db.syncQueueDao()

    // --- Services ---
    // Most services use @Inject constructor and @Singleton directly,
    // so Hilt provides them automatically. Explicit @Provides entries
    // are only needed for types without @Inject constructors.

    @Provides
    @Singleton
    fun provideSecureStorageService(
        @ApplicationContext context: Context
    ): SecureStorageService {
        return SecureStorageService(context)
    }

    @Provides
    @Singleton
    fun provideAuthManager(
        secureStorage: SecureStorageService
    ): AuthManager {
        return AuthManager(secureStorage)
    }

    @Provides
    @Singleton
    fun provideLocationManager(
        @ApplicationContext context: Context
    ): LocationManager {
        return LocationManager(context)
    }

    @Provides
    @Singleton
    fun provideSyncEngine(
        @ApplicationContext context: Context
    ): SyncEngine {
        return SyncEngine(context)
    }

    @Provides
    @Singleton
    fun provideMeshSyncEngine(
        @ApplicationContext context: Context
    ): MeshSyncEngine {
        return MeshSyncEngine(context)
    }

    @Provides
    @Singleton
    fun provideSyncQueueManager(): SyncQueueManager {
        return SyncQueueManager()
    }

    @Provides
    @Singleton
    fun provideOfflineTileManager(
        @ApplicationContext context: Context
    ): OfflineTileManager {
        return OfflineTileManager(context)
    }

    @Provides
    @Singleton
    fun providePhotoUploadManager(
        @ApplicationContext context: Context
    ): PhotoUploadManager {
        return PhotoUploadManager(context)
    }
}
