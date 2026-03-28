package org.yac.llamarangers.domain.model.enums

import androidx.compose.ui.graphics.Color

enum class TaskPriority(val value: String) {
    LOW("low"),
    MEDIUM("medium"),
    HIGH("high");

    val displayName: String
        get() = when (this) {
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
        }

    val color: Color
        get() = when (this) {
            LOW -> Color.Blue
            MEDIUM -> Color(0xFFFFA500)
            HIGH -> Color.Red
        }

    val icon: String
        get() = when (this) {
            LOW -> "arrow_downward"
            MEDIUM -> "remove_circle_outline"
            HIGH -> "error"
        }

    val sortOrder: Int
        get() = when (this) {
            HIGH -> 0
            MEDIUM -> 1
            LOW -> 2
        }

    companion object {
        fun fromValue(value: String): TaskPriority =
            entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}
