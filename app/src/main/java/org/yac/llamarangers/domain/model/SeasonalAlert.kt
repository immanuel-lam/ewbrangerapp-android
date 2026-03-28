package org.yac.llamarangers.domain.model

import java.util.Calendar

data class SeasonalAlert(
    val title: String,
    val message: String,
    val severity: Severity
) {
    enum class Severity {
        INFO, WARNING, CRITICAL
    }

    companion object {
        /**
         * Returns active seasonal alerts based on the current date and rain-event flag.
         */
        fun activeAlerts(
            date: Long = System.currentTimeMillis(),
            recentRain: Boolean = false
        ): List<SeasonalAlert> {
            val alerts = mutableListOf<SeasonalAlert>()
            val calendar = Calendar.getInstance().apply { timeInMillis = date }
            val month = calendar.get(Calendar.MONTH) + 1 // Calendar.MONTH is 0-based

            // Autumn chemical treatment window (April-May in Cape York)
            if (month == 4 || month == 5) {
                alerts.add(
                    SeasonalAlert(
                        title = "Autumn Treatment Window",
                        message = "April\u2013May is the optimal period for foliar spray and basal bark treatments. Plants are actively translocating nutrients.",
                        severity = Severity.INFO
                    )
                )
            }

            // Post-rain regrowth alert
            if (recentRain) {
                alerts.add(
                    SeasonalAlert(
                        title = "Post-Rain Regrowth Expected",
                        message = "Recent rainfall increases Lantana regrowth rate. Check previously treated sites within 2\u20133 weeks.",
                        severity = Severity.WARNING
                    )
                )
            }

            // Dry season - reduced efficacy warning (June-September)
            if (month in 6..9) {
                alerts.add(
                    SeasonalAlert(
                        title = "Dry Season \u2014 Reduced Foliar Efficacy",
                        message = "Foliar spraying is less effective when plants are drought-stressed. Prefer cut-stump or basal bark methods.",
                        severity = Severity.WARNING
                    )
                )
            }

            // Wet season - biocontrol opportunity (November-March)
            if (month >= 11 || month <= 3) {
                alerts.add(
                    SeasonalAlert(
                        title = "Wet Season: Biocontrol Active",
                        message = "Check for lantana bug (Aconophora compressa) before spraying pink variants. Biocontrol insects may be present.",
                        severity = Severity.INFO
                    )
                )
            }

            return alerts
        }
    }
}
