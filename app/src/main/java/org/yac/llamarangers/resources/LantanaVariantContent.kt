package org.yac.llamarangers.resources

import org.yac.llamarangers.domain.model.enums.LantanaVariant
import org.yac.llamarangers.domain.model.enums.TreatmentMethod

/**
 * Static variant guide content for the identification guide.
 * Ports iOS LantanaVariantContent.
 */
object LantanaVariantContent {

    data class VariantInfo(
        val variant: LantanaVariant,
        val commonName: String,
        val scientificNote: String,
        val imageName: String,
        val distinguishingFeatures: String,
        val controlMethods: List<TreatmentMethod>,
        val seasonalNotes: String?
    )

    val all: List<VariantInfo> = listOf(
        VariantInfo(
            variant = LantanaVariant.PINK,
            commonName = "Pink Lantana",
            scientificNote = "Lantana camara (pink form)",
            imageName = "lantana_pink",
            distinguishingFeatures = "Soft pink flowers, often fading to yellow centres. Compact shrub to 2m. Common in disturbed roadsides and cleared areas.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.SPLAT_GUN),
            seasonalNotes = "Check for lantana bug (Aconophora compressa) before spraying during wet season (Nov\u2013Mar)."
        ),
        VariantInfo(
            variant = LantanaVariant.RED,
            commonName = "Red Lantana",
            scientificNote = "Lantana camara (red form)",
            imageName = "lantana_red",
            distinguishingFeatures = "Deep red-orange flowers aging to darker red. Most vigorous spreader. Toxic black berries attractive to birds.",
            controlMethods = listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.BASAL_BARK),
            seasonalNotes = null
        ),
        VariantInfo(
            variant = LantanaVariant.PINK_EDGED_RED,
            commonName = "Pink-Edged Red Lantana",
            scientificNote = "Lantana camara (hybrid form)",
            imageName = "lantana_pink_edged_red",
            distinguishingFeatures = "Pink outer petals with red or orange centre. Hybrid characteristics, often larger than pure variants.",
            controlMethods = listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY),
            seasonalNotes = null
        ),
        VariantInfo(
            variant = LantanaVariant.ORANGE,
            commonName = "Orange Lantana",
            scientificNote = "Lantana camara (orange form)",
            imageName = "lantana_orange",
            distinguishingFeatures = "Bright orange to yellow-orange flowers. Common along watercourses and creek edges. Good spreader via waterways.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.BASAL_BARK),
            seasonalNotes = null
        ),
        VariantInfo(
            variant = LantanaVariant.WHITE,
            commonName = "White Lantana",
            scientificNote = "Lantana camara (white form)",
            imageName = "lantana_white",
            distinguishingFeatures = "White to cream flowers, sometimes with faint yellow centres. Less vigorous but still invasive. Often found in shaded areas.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.SPLAT_GUN),
            seasonalNotes = null
        ),
        VariantInfo(
            variant = LantanaVariant.UNKNOWN,
            commonName = "Unknown Variant",
            scientificNote = "Lantana camara (unidentified)",
            imageName = "lantana_unknown",
            distinguishingFeatures = "Variant not clearly identifiable from field observation. Log and treat as per nearest visual match.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY),
            seasonalNotes = null
        )
    )
}
