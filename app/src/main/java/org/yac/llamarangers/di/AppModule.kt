package org.yac.llamarangers.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Hilt DI module for the service layer.
 *
 * Most services (SecureStorageService, AuthManager, LocationManager, SyncEngine,
 * MeshSyncEngine, SyncQueueManager, OfflineTileManager, PhotoUploadManager) use
 * @Inject constructor + @Singleton directly, so Hilt provides them automatically.
 * Explicit @Provides entries here were removed to avoid duplicate binding errors
 * with the constructor-injected singletons.
 *
 * Room database and DAOs are provided by DatabaseModule.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule
