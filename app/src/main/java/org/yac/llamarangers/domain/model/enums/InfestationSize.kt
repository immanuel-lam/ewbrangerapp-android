package org.yac.llamarangers.domain.model.enums

enum class InfestationSize(val value: String) {
    SMALL("small"),
    MEDIUM("medium"),
    LARGE("large");

    val displayName: String
        get() = when (this) {
            SMALL -> "Small"
            MEDIUM -> "Medium"
            LARGE -> "Large"
        }

    val areaDescription: String
        get() = when (this) {
            SMALL -> "< 5 m\u00B2"
            MEDIUM -> "5 \u2013 50 m\u00B2"
            LARGE -> "> 50 m\u00B2"
        }

    companion object {
        fun fromValue(value: String): InfestationSize =
            entries.firstOrNull { it.value == value } ?: SMALL
    }
}
