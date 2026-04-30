package org.yac.llamarangers.domain.model.enums

enum class TreatmentMethod(val value: String) {
    CUT_STUMP("cutStump"),
    SPLAT_GUN("splatGun"),
    FOLIAR_SPRAY("foliarSpray"),
    BASAL_BARK("basalBark"),
    MECHANICAL("mechanical"),
    STEM_INJECTION("stemInjection"),
    FIRE_MANAGEMENT("fireManagement");

    val displayName: String
        get() = when (this) {
            CUT_STUMP -> "Cut Stump"
            SPLAT_GUN -> "Splat Gun"
            FOLIAR_SPRAY -> "Foliar Spray"
            BASAL_BARK -> "Basal Bark"
            MECHANICAL -> "Mechanical Removal"
            STEM_INJECTION -> "Stem Injection"
            FIRE_MANAGEMENT -> "Fire Management"
        }

    val instructions: String
        get() = when (this) {
            CUT_STUMP -> "Cut stem close to ground. Apply neat herbicide (e.g. Garlon 600) to cut surface immediately. Effective for stems >1cm diameter."
            SPLAT_GUN -> "Apply herbicide in diesel using splat gun applicator to stem. Space injections 2–3cm apart around stem circumference."
            FOLIAR_SPRAY -> "Mix herbicide at label rate with water + penetrant. Spray to wet all foliage. Best applied to actively growing plants. Avoid in rain or extreme heat."
            BASAL_BARK -> "Apply herbicide in diesel (1:3 ratio) to lower 30cm of stem bark. Effective year-round. Do not apply to wet or corky bark."
            MECHANICAL -> "Hand-pull, grub, or slash plants at ground level. Ensure roots are removed to prevent regrowth. Bag and dispose of seed heads. Follow up in 4–6 weeks."
            STEM_INJECTION -> "Drill or cut evenly spaced holes into the stem (1 per 3cm of diameter). Inject neat or diluted herbicide immediately. Effective for large woody vines and trees."
            FIRE_MANAGEMENT -> "Planned burning to reduce grass fuel load and stimulate native recovery. Coordinate with land managers. Follow fire permit requirements. Timing is critical — dry season preferred for grass species."
        }

    val iconName: String
        get() = when (this) {
            CUT_STUMP -> "content_cut"
            SPLAT_GUN -> "crop_square"
            FOLIAR_SPRAY -> "water_drop"
            BASAL_BARK -> "park"
            MECHANICAL -> "back_hand"
            STEM_INJECTION -> "vaccines"
            FIRE_MANAGEMENT -> "local_fire_department"
        }

    companion object {
        fun fromValue(value: String): TreatmentMethod =
            entries.firstOrNull { it.value == value } ?: FOLIAR_SPRAY
    }
}
