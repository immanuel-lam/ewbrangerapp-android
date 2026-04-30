package org.yac.llamarangers.domain.model

import org.yac.llamarangers.domain.model.enums.InvasiveSpecies

enum class UrgencyLevel(val value: String) {
    URGENT("URGENT"),
    PRIORITY("PRIORITY"),
    ROUTINE("ROUTINE")
}

data class PhenologyAlert(
    val phase: String,
    val actionRecommended: String,
    val urgencyLevel: UrgencyLevel
)

object PhenologyAlertStore {
    fun alert(forSpecies: InvasiveSpecies, month: Int): PhenologyAlert? {
        return when (forSpecies) {
            InvasiveSpecies.LANTANA -> {
                if (month in 10..12 || month in 1..3) {
                    PhenologyAlert(
                        phase = "Peak Flowering",
                        actionRecommended = "Check for biocontrol insects (Lantana bug) before foliar spray.",
                        urgencyLevel = UrgencyLevel.PRIORITY
                    )
                } else null
            }
            InvasiveSpecies.RUBBER_VINE -> {
                if (month in 8..10) {
                    PhenologyAlert(
                        phase = "Active Flowering",
                        actionRecommended = "Treat now before seed pods mature and disperse via floodwaters.",
                        urgencyLevel = UrgencyLevel.URGENT
                    )
                } else if (month in 11..12 || month == 1) {
                    PhenologyAlert(
                        phase = "Seed Dispersal",
                        actionRecommended = "High risk of spread. Focus on upstream infestations first.",
                        urgencyLevel = UrgencyLevel.URGENT
                    )
                } else null
            }
            InvasiveSpecies.PRICKLY_ACACIA -> {
                if (month in 4..7) {
                    PhenologyAlert(
                        phase = "Pod Fall",
                        actionRecommended = "Seeds are dropping. Prioritise removal of seed-bearing trees.",
                        urgencyLevel = UrgencyLevel.URGENT
                    )
                } else null
            }
            InvasiveSpecies.SICKLEPOD -> {
                if (month in 11..12 || month in 1..4) {
                    PhenologyAlert(
                        phase = "Wet Season Growth",
                        actionRecommended = "Fast growth. Manual removal is most effective for young plants (<30cm).",
                        urgencyLevel = UrgencyLevel.PRIORITY
                    )
                } else null
            }
            InvasiveSpecies.GIANT_RATS_TAIL_GRASS -> {
                if (month in 3..6) {
                    PhenologyAlert(
                        phase = "Seed Ripening",
                        actionRecommended = "High risk of spread. Avoid slashing once seeds are ripe.",
                        urgencyLevel = UrgencyLevel.URGENT
                    )
                } else null
            }
            InvasiveSpecies.POND_APPLE -> {
                if (month in 4..7) {
                    PhenologyAlert(
                        phase = "Fruit Development",
                        actionRecommended = "Fruits float. Focus on preventing seed entry into waterways.",
                        urgencyLevel = UrgencyLevel.PRIORITY
                    )
                } else null
            }
            else -> null
        }
    }
}
