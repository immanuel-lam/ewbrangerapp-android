package org.yac.llamarangers.config

object SyncConfig {
    // Retry delays in milliseconds
    const val RETRY_DELAY_1_MS: Long = 0                  // Immediate
    const val RETRY_DELAY_2_MS: Long = 300_000             // 5 minutes
    const val MAX_RETRY_DELAY_MS: Long = 3_600_000         // 1 hour cap
    const val FAILURE_THRESHOLD: Int = 10                  // Show badge after this many failures

    // Chunk sizes for mesh sync
    const val MANIFEST_CHUNK_SIZE = 500                    // Records per manifest chunk
    const val RECORD_CHUNK_SIZE = 50                       // Records per transfer batch

    // Background sync
    const val BACKGROUND_TASK_IDENTIFIER = "org.yac.llamarangers.sync"
}
