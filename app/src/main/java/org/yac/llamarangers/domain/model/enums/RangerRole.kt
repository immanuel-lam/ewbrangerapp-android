package org.yac.llamarangers.domain.model.enums

enum class RangerRole(val value: String) {
    RANGER("ranger"),
    SENIOR_RANGER("seniorRanger"),
    COORDINATOR("coordinator");

    val displayName: String
        get() = when (this) {
            RANGER -> "Ranger"
            SENIOR_RANGER -> "Senior Ranger"
            COORDINATOR -> "Coordinator"
        }

    companion object {
        fun fromValue(value: String): RangerRole =
            entries.firstOrNull { it.value == value } ?: RANGER
    }
}
