package org.yac.llamarangers.service.sync

import java.util.Date

/**
 * Last-Write-Wins (LWW) conflict resolver shared by cloud sync and mesh sync.
 * Ports iOS ConflictResolver.
 */
object ConflictResolver {

    /**
     * Result of a conflict resolution attempt.
     */
    data class Resolution<T>(
        val updated: Boolean,
        val mergedPhotoFilenames: List<String>?
    )

    /**
     * Resolves a conflict between a local record and incoming peer/server data.
     * Incoming wins on all scalar fields if its updatedAt is newer.
     * Photo filenames are merged (union, never lost).
     *
     * @param incomingUpdatedAt timestamp of the incoming record
     * @param localUpdatedAt timestamp of the local record
     * @param localPhotoFilenames photo filenames on the local record (nullable)
     * @param incomingPhotoFilenames photo filenames on the incoming record (nullable)
     * @param applyIncoming lambda to apply incoming scalar fields to the local record
     * @return Resolution indicating whether the local record was updated and merged photos
     */
    fun <T> resolve(
        incomingUpdatedAt: Date,
        localUpdatedAt: Date,
        localPhotoFilenames: List<String>? = null,
        incomingPhotoFilenames: List<String>? = null,
        applyIncoming: () -> Unit
    ): Resolution<T> {
        if (!incomingUpdatedAt.after(localUpdatedAt)) {
            return Resolution(updated = false, mergedPhotoFilenames = null)
        }

        // Incoming wins -- apply incoming data
        applyIncoming()

        // Merge photo filenames: union of both lists
        val mergedPhotos = if (localPhotoFilenames != null || incomingPhotoFilenames != null) {
            val local = localPhotoFilenames.orEmpty().toSet()
            val incoming = incomingPhotoFilenames.orEmpty().toSet()
            (local union incoming).toList()
        } else {
            null
        }

        return Resolution(updated = true, mergedPhotoFilenames = mergedPhotos)
    }
}
