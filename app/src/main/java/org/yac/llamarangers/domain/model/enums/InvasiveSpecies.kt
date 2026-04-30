package org.yac.llamarangers.domain.model.enums

import androidx.compose.ui.graphics.Color
import org.yac.llamarangers.ui.theme.*

enum class SpeciesCategory(val value: String) {
    SHRUB("shrub"),
    VINE("vine"),
    TREE("tree"),
    GRASS("grass");

    val displayName: String
        get() = when (this) {
            SHRUB -> "Shrub"
            VINE -> "Vine"
            TREE -> "Tree"
            GRASS -> "Grass"
        }

    val iconName: String
        get() = when (this) {
            SHRUB -> "spa" // closest to leaf.fill
            VINE -> "cyclone" // closest to tornado
            TREE -> "park" // closest to tree.fill
            GRASS -> "air" // closest to wind
        }
}

enum class InvasiveSpecies(val value: String) {
    LANTANA("lantana"),
    RUBBER_VINE("rubberVine"),
    PRICKLY_ACACIA("pricklyAcacia"),
    SICKLEPOD("sicklepod"),
    GIANT_RATS_TAIL_GRASS("giantRatsTailGrass"),
    POND_APPLE("pondApple"),
    UNKNOWN("unknown");

    val displayName: String
        get() = when (this) {
            LANTANA -> "Lantana"
            RUBBER_VINE -> "Rubber Vine"
            PRICKLY_ACACIA -> "Prickly Acacia"
            SICKLEPOD -> "Sicklepod"
            GIANT_RATS_TAIL_GRASS -> "Giant Rat's Tail Grass"
            POND_APPLE -> "Pond Apple"
            UNKNOWN -> "Unknown"
        }

    val scientificName: String
        get() = when (this) {
            LANTANA -> "Lantana camara"
            RUBBER_VINE -> "Cryptostegia grandiflora"
            PRICKLY_ACACIA -> "Vachellia nilotica"
            SICKLEPOD -> "Senna obtusifolia"
            GIANT_RATS_TAIL_GRASS -> "Sporobolus pyramidalis"
            POND_APPLE -> "Annona glabra"
            UNKNOWN -> "Species unidentified"
        }

    val category: SpeciesCategory
        get() = when (this) {
            LANTANA -> SpeciesCategory.SHRUB
            RUBBER_VINE -> SpeciesCategory.VINE
            PRICKLY_ACACIA -> SpeciesCategory.TREE
            SICKLEPOD -> SpeciesCategory.SHRUB
            GIANT_RATS_TAIL_GRASS -> SpeciesCategory.GRASS
            POND_APPLE -> SpeciesCategory.TREE
            UNKNOWN -> SpeciesCategory.SHRUB
        }

    val color: Color
        get() = when (this) {
            LANTANA -> VariantPink
            RUBBER_VINE -> VariantPinkEdgedRed
            PRICKLY_ACACIA -> VariantOrange
            SICKLEPOD -> VariantWhite
            GIANT_RATS_TAIL_GRASS -> VariantRed
            POND_APPLE -> Color.Cyan
            UNKNOWN -> VariantUnknown
        }

    val iconName: String
        get() = when (this) {
            LANTANA -> "spa"
            RUBBER_VINE -> "restart_alt"
            PRICKLY_ACACIA -> "park"
            SICKLEPOD -> "dark_mode"
            GIANT_RATS_TAIL_GRASS -> "air"
            POND_APPLE -> "water_drop"
            UNKNOWN -> "help"
        }

    val distinguishingFeatures: String
        get() = when (this) {
            LANTANA -> "Dense shrub with rough, wrinkled leaves. Flower heads contain multiple small flowers that change colour as they age — often pink/orange/yellow on the same head. Distinctive unpleasant smell when foliage is crushed."
            RUBBER_VINE -> "Vigorous woody vine climbing high into tree canopy. Produces milky latex sap when cut. Large, leathery glossy leaves in opposite pairs. Pink or purple trumpet-shaped flowers. Seed pods in V-shaped pairs releasing white silky fibre."
            PRICKLY_ACACIA -> "Small thorny tree 3–7m tall with a spreading, often flat-topped canopy. Pairs of straight white spines 3–8cm long at each leaf node. Yellow ball-shaped flowers. Long flat seed pods in clusters."
            SICKLEPOD -> "Erect annual herb or short-lived shrub, 0.5–1.5m tall. Leaves have 3 pairs of leaflets with distinctive gland at base of lowest pair. Yellow flowers followed by long curved sickle-shaped seed pods. Characteristic unpleasant smell."
            GIANT_RATS_TAIL_GRASS -> "Robust perennial tussock grass forming large dense clumps 0.5–1.5m tall. Distinctive tall 'rattails' 15–30cm long, rough and bristly. Seeds very small and abundant."
            POND_APPLE -> "Small to medium riparian tree, 3–12m tall. Large, oval, glossy dark green leaves. Cream flowers followed by large warty yellow-green fruit (resembling a rough apple) floating in water."
            UNKNOWN -> "Species not clearly identifiable in the field. Record location and photos. Treat as the most likely species based on habitat and growth form."
        }

    val controlMethods: List<TreatmentMethod>
        get() = when (this) {
            LANTANA -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.CUT_STUMP, TreatmentMethod.BASAL_BARK, TreatmentMethod.SPLAT_GUN)
            RUBBER_VINE -> listOf(TreatmentMethod.STEM_INJECTION, TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.BASAL_BARK)
            PRICKLY_ACACIA -> listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.STEM_INJECTION, TreatmentMethod.FOLIAR_SPRAY)
            SICKLEPOD -> listOf(TreatmentMethod.MECHANICAL, TreatmentMethod.FOLIAR_SPRAY)
            GIANT_RATS_TAIL_GRASS -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.FIRE_MANAGEMENT, TreatmentMethod.MECHANICAL)
            POND_APPLE -> listOf(TreatmentMethod.STEM_INJECTION, TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY)
            UNKNOWN -> listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.CUT_STUMP)
        }

    val seasonalNotes: String?
        get() = when (this) {
            LANTANA -> "Autumn (Apr–May) is optimal for foliar spray. Wet season: check for biocontrol insects before spraying. Avoid treatment when plants are drought-stressed."
            RUBBER_VINE -> "Flowers Aug–Oct. This is the best time to identify new plants. Treat before seed set to prevent spread via floodwaters. Stem injection effective year-round."
            PRICKLY_ACACIA -> "Pods fall dry season (May–Sep), spreading seed. Focus removal efforts before pod fall. Biological control agents (seed-feeding weevils) may be active — check before spraying."
            SICKLEPOD -> "Flowers and seeds prolifically in wet season. Mechanical removal is most effective when young (< 30cm). Remove before seeding."
            GIANT_RATS_TAIL_GRASS -> "Seed heads ripen in late dry season (Aug–Oct). Slashing or burning before seed set significantly reduces spread. Hot late dry-season burns can reduce established clumps. Wet season burning can also be effective in suitable fire management plans."
            POND_APPLE -> "Fruit floats and spreads via water. Focus removal on upstream plants first to prevent downstream seed dispersal. Treat when fruit is immature (before it drops). Stem injection minimises herbicide runoff near waterways."
            UNKNOWN -> null
        }

    val hasBiocontrolConcern: Boolean
        get() = this == LANTANA

    companion object {
        fun fromValue(value: String): InvasiveSpecies {
            val directMatch = entries.firstOrNull { it.value == value }
            if (directMatch != null) return directMatch
            
            val lantanaLegacyValues = setOf("pink", "red", "pinkEdgedRed", "orange", "white")
            if (lantanaLegacyValues.contains(value)) return LANTANA

            return UNKNOWN
        }
    }
}
