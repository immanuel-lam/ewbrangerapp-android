package org.yac.llamarangers.domain.model

import java.util.UUID

data class PatrolChecklistItem(
    val id: String = UUID.randomUUID().toString(),
    val label: String,
    val isComplete: Boolean = false,
    val completedAt: Long? = null, // epoch millis
    val timeEstimateMins: Int = 0
)
