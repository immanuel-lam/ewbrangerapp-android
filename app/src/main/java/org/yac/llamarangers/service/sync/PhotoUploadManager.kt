package org.yac.llamarangers.service.sync

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Photo upload manager (V2 stub -- no-op).
 * Cloud photo upload requires Supabase storage (V3).
 * Ports iOS PhotoUploadManager actor.
 */
@Singleton
class PhotoUploadManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "PhotoUploadManager"
        private const val PHOTOS_DIRECTORY_NAME = "Photos"
    }

    /**
     * Upload pending photos to cloud storage.
     * PoC: no-op -- logs stub message.
     */
    suspend fun uploadPendingPhotos(rangerId: UUID, jwt: String) {
        Log.d(TAG, "Photo upload stubbed for PoC -- no cloud storage configured.")
        // V3 implementation would:
        // 1. List files in photosDirectory
        // 2. Upload each to Supabase storage at {rangerId}/{sightingId}/{filename}
        // 3. Delete local file on success
    }

    /**
     * Returns the local photos directory.
     */
    fun photosDirectory(): File {
        val dir = File(context.filesDir, PHOTOS_DIRECTORY_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }
}
