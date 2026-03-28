package org.yac.llamarangers.domain.model.enums

enum class TreatmentMethod(val value: String) {
    CUT_STUMP("cutStump"),
    SPLAT_GUN("splatGun"),
    FOLIAR_SPRAY("foliarSpray"),
    BASAL_BARK("basalBark");

    val displayName: String
        get() = when (this) {
            CUT_STUMP -> "Cut Stump"
            SPLAT_GUN -> "Splat Gun"
            FOLIAR_SPRAY -> "Foliar Spray"
            BASAL_BARK -> "Basal Bark"
        }

    val instructions: String
        get() = when (this) {
            CUT_STUMP -> "Cut stem close to ground. Apply neat Garlon 600 to cut surface immediately. Effective for stems >1cm diameter."
            SPLAT_GUN -> "Apply Garlon 600 in diesel using splat gun applicator to stem. Space injections 2\u20133cm apart around stem circumference."
            FOLIAR_SPRAY -> "Mix Garlon 600 at 5mL/L with water + penetrant. Spray to wet all foliage. Best applied to actively growing plants. Avoid in rain."
            BASAL_BARK -> "Apply Garlon 600 in diesel at 1:3 ratio to lower 30cm of stem bark. Effective year-round. Do not apply to wet bark."
        }

    val iconName: String
        get() = when (this) {
            CUT_STUMP -> "content_cut"
            SPLAT_GUN -> "crop_square"
            FOLIAR_SPRAY -> "water_drop"
            BASAL_BARK -> "park"
        }

    companion object {
        fun fromValue(value: String): TreatmentMethod =
            entries.firstOrNull { it.value == value } ?: FOLIAR_SPRAY
    }
}
