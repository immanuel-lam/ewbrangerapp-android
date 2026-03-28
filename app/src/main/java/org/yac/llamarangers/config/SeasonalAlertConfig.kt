package org.yac.llamarangers.config

object SeasonalAlertConfig {
    // Months (1-based) when autumn treatment window is active
    val AUTUMN_TREATMENT_MONTHS: Set<Int> = setOf(4, 5)

    // Months when wet season biocontrol may be active
    val WET_SEASON_MONTHS: Set<Int> = setOf(11, 12, 1, 2, 3)

    // Months when dry season warning applies
    val DRY_SEASON_MONTHS: Set<Int> = setOf(6, 7, 8, 9)

    // SharedPreferences / DataStore key for rain event flag
    const val RECENT_RAIN_KEY = "recentRainEvent"

    // How long the rain flag stays active (milliseconds)
    const val RAIN_FLAG_DURATION_MS: Long = 14L * 24 * 3600 * 1000
}
