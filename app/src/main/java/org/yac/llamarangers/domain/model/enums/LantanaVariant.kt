package org.yac.llamarangers.domain.model.enums

import androidx.compose.ui.graphics.Color

enum class LantanaVariant(val value: String) {
    PINK("pink"),
    RED("red"),
    PINK_EDGED_RED("pinkEdgedRed"),
    ORANGE("orange"),
    WHITE("white"),
    UNKNOWN("unknown");

    val displayName: String
        get() = when (this) {
            PINK -> "Pink"
            RED -> "Red"
            PINK_EDGED_RED -> "Pink-Edged Red"
            ORANGE -> "Orange"
            WHITE -> "White"
            UNKNOWN -> "Unknown"
        }

    val color: Color
        get() = when (this) {
            PINK -> Color(1.0f, 0.41f, 0.71f)
            RED -> Color(0.86f, 0.08f, 0.24f)
            PINK_EDGED_RED -> Color(0.9f, 0.25f, 0.45f)
            ORANGE -> Color(1.0f, 0.55f, 0.0f)
            WHITE -> Color(0.95f, 0.95f, 0.95f)
            UNKNOWN -> Color.Gray
        }

    val controlMethods: List<TreatmentMethod>
        get() = when (this) {
            PINK -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.SPLAT_GUN)
            RED -> listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.BASAL_BARK)
            PINK_EDGED_RED -> listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY)
            ORANGE -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.BASAL_BARK)
            WHITE -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.SPLAT_GUN)
            UNKNOWN -> listOf(TreatmentMethod.FOLIAR_SPRAY)
        }

    val distinguishingFeatures: String
        get() = when (this) {
            PINK -> "Soft pink flowers, often fading to yellow centres. Common in disturbed areas."
            RED -> "Deep red-orange flowers. Most aggressive spreader. Toxic berries."
            PINK_EDGED_RED -> "Pink outer petals with red centre. Hybrid characteristics."
            ORANGE -> "Bright orange-yellow flowers. Common along watercourses."
            WHITE -> "White to cream flowers. Less vigorous but still invasive."
            UNKNOWN -> "Variant not clearly identifiable. Treat as per nearest match."
        }

    val hasBiocontrolConcern: Boolean
        get() = this == PINK

    companion object {
        fun fromValue(value: String): LantanaVariant =
            entries.firstOrNull { it.value == value } ?: UNKNOWN
    }
}
