package org.yac.llamarangers.domain.model

data class Herbicide(
    val id: String,
    val name: String,
    val activeIngredient: String,
    val commonProducts: List<String>,
    val targetSpecies: List<String>,
    val notCompatibleWith: List<String>,
    val weatherConstraints: String,
    val ppeRequired: List<String>,
    val dilutionRateMlPer10L: Double,
    val applicationNotes: String
)

enum class CompatibilityResult {
    COMPATIBLE,
    INCOMPATIBLE,
    SAME_PRODUCT
}

object HerbicideDatabase {
    val all: List<Herbicide> = listOf(
        Herbicide(
            id = "glyphosate",
            name = "Glyphosate",
            activeIngredient = "Glyphosate 360 g/L (SL)",
            commonProducts = listOf("Roundup Biactive", "Weedmaster Duo", "Glyphosate 360"),
            targetSpecies = listOf("Lantana", "Rubber Vine", "Sicklepod", "Giant Rat's Tail Grass", "Pond Apple"),
            notCompatibleWith = listOf("Metsulfuron"),
            weatherConstraints = "Do not apply if rain expected within 4 hours. Avoid application in temperatures above 35 °C or during drought stress. Best applied in calm conditions to minimise drift.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Long-sleeved clothing"),
            dilutionRateMlPer10L = 100.0,
            applicationNotes = "Foliar spray: add a non-ionic surfactant (0.25% v/v) to improve uptake. Allow 7–10 days for full effect before follow-up assessment. Do not use near waterways — use Access or Garlon alternatives instead."
        ),
        Herbicide(
            id = "metsulfuron",
            name = "Metsulfuron",
            activeIngredient = "Metsulfuron-methyl 600 g/kg (WG)",
            commonProducts = listOf("Brushoff", "Metsulfuron 600 DF", "Ally"),
            targetSpecies = listOf("Lantana", "Sicklepod", "Prickly Acacia"),
            notCompatibleWith = listOf("Glyphosate", "Picloram"),
            weatherConstraints = "Do not apply if rain expected within 1 hour. Soil activity — do not apply before heavy rain events. Avoid use near susceptible crops or native legumes.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Dust mask when handling concentrate"),
            dilutionRateMlPer10L = 1.5,
            applicationNotes = "Highly effective on broad-leaf weeds at very low rates. Add a non-ionic surfactant. Residual soil activity — observe re-sowing withholding periods. Restricted near waterways due to persistence."
        ),
        Herbicide(
            id = "triclopyr",
            name = "Triclopyr",
            activeIngredient = "Triclopyr 600 g/L (EC)",
            commonProducts = listOf("Garlon 600", "Triclopyr 600EC", "Starane Advanced"),
            targetSpecies = listOf("Lantana", "Rubber Vine", "Pond Apple", "Prickly Acacia"),
            notCompatibleWith = listOf("Aminopyralid"),
            weatherConstraints = "Do not apply if rain expected within 2 hours. Avoid application during strong winds. Do not apply when temperatures exceed 30 °C — product volatilises and can drift to non-target plants.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Respirator (mixing concentrate)", "Protective footwear"),
            dilutionRateMlPer10L = 130.0,
            applicationNotes = "Preferred for basal bark and cut-stump application in diesel or penetrant oil carrier. Excellent on woody shrubs and vines. Safe for use near waterways when used at label rates — good for riparian Rubber Vine and Pond Apple control."
        ),
        Herbicide(
            id = "picloram",
            name = "Picloram",
            activeIngredient = "Picloram 44.7 g/L + Triclopyr 44.7 g/L (SL)",
            commonProducts = listOf("Tordon 75-D", "Access", "Grazon Extra"),
            targetSpecies = listOf("Lantana", "Rubber Vine", "Prickly Acacia", "Pond Apple"),
            notCompatibleWith = listOf("Metsulfuron", "Aminopyralid"),
            weatherConstraints = "Do not apply if rain expected within 1 hour. Significant soil persistence — do not use on sandy soils near waterways. Avoid spray drift. Not to be used in areas where susceptible crops are grown nearby.",
            ppeRequired = listOf("Chemical-resistant gloves", "Full face shield", "Respirator", "Protective clothing", "Protective footwear"),
            dilutionRateMlPer10L = 100.0,
            applicationNotes = "Highly effective stem injection and cut-stump herbicide for large woody weeds. Tordon 75-D uses diesel carrier for undiluted basal bark application. Picloram component provides residual soil activity — prevents regrowth. Withholding period applies for livestock grazing."
        ),
        Herbicide(
            id = "aminopyralid",
            name = "Aminopyralid",
            activeIngredient = "Aminopyralid 300 g/L (SL)",
            commonProducts = listOf("Grazon Extra", "Vigilant II Gel", "Broadstrike"),
            targetSpecies = listOf("Lantana", "Sicklepod", "Prickly Acacia"),
            notCompatibleWith = listOf("Triclopyr", "Picloram"),
            weatherConstraints = "Do not apply if rain expected within 1 hour. Very high soil persistence — do not use in cultivation areas or where soil runoff enters waterways. Composting restriction: do not use treated plant material as compost or mulch.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Long-sleeved clothing"),
            dilutionRateMlPer10L = 50.0,
            applicationNotes = "Vigilant II Gel formulation suitable for small cut-stumps and individual stem treatment — minimises non-target exposure. Aminopyralid is highly persistent; observe all grazing and crop re-sowing withholding periods. Do not allow treated material to enter compost systems."
        ),
        Herbicide(
            id = "fluroxypyr",
            name = "Fluroxypyr",
            activeIngredient = "Fluroxypyr 200 g/L (EC)",
            commonProducts = listOf("Starane 200", "Fluroxypyr 200EC", "Hotshot"),
            targetSpecies = listOf("Giant Rat's Tail Grass", "Sicklepod", "Lantana"),
            notCompatibleWith = emptyList(),
            weatherConstraints = "Do not apply if rain expected within 1 hour. Avoid application in extreme heat (above 35 °C). Effective at temperatures above 15 °C.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection"),
            dilutionRateMlPer10L = 60.0,
            applicationNotes = "Selective for broad-leaf weeds in pasture situations — does not affect most grasses. Useful where Giant Rat's Tail Grass management involves pasture renovation. Compatible with most broadleaf herbicides when mixed fresh. Always mix just before use."
        ),
        Herbicide(
            id = "haloxyfop",
            name = "Haloxyfop",
            activeIngredient = "Haloxyfop-P 520 g/L (EC)",
            commonProducts = listOf("Verdict 520", "Haloxyfop 520EC"),
            targetSpecies = listOf("Giant Rat's Tail Grass"),
            notCompatibleWith = emptyList(),
            weatherConstraints = "Do not apply if rain expected within 1 hour. Most effective when grass is actively growing. Avoid application in drought stress conditions.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Long-sleeved clothing"),
            dilutionRateMlPer10L = 40.0,
            applicationNotes = "Highly selective grass-specific herbicide — will not harm broad-leaf plants or native trees. Best option for Giant Rat's Tail Grass growing within native vegetation. Requires addition of a crop oil concentrate (1% v/v). Allow 3–4 weeks for full effect. Post-emergent only."
        ),
        Herbicide(
            id = "imazapyr",
            name = "Imazapyr",
            activeIngredient = "Imazapyr 250 g/L (SL)",
            commonProducts = listOf("Arsenal Xtra", "Imazapyr 250 SL"),
            targetSpecies = listOf("Rubber Vine", "Pond Apple", "Giant Rat's Tail Grass"),
            notCompatibleWith = listOf("Metsulfuron", "Glyphosate"),
            weatherConstraints = "Do not apply if rain expected within 2 hours. Significant soil residual activity — use only in bushland, not near crops or desirable native pasture. Do not use on or near waterways.",
            ppeRequired = listOf("Chemical-resistant gloves", "Eye protection", "Respirator (concentrate handling)", "Protective clothing"),
            dilutionRateMlPer10L = 30.0,
            applicationNotes = "Soil and foliar activity — very effective for stem injection of large Rubber Vine and Pond Apple. Residual soil activity suppresses seedling emergence for 12–18 months. Ideal for multi-year control programs. Not for use near desirable vegetation due to root uptake."
        )
    )

    fun herbicidesFor(speciesName: String): List<Herbicide> {
        return all.filter { it.targetSpecies.contains(speciesName) }
    }

    fun compatibility(a: Herbicide, b: Herbicide): CompatibilityResult {
        if (a.id == b.id) return CompatibilityResult.SAME_PRODUCT
        val aIncompatibleWithB = a.notCompatibleWith.contains(b.name)
        val bIncompatibleWithA = b.notCompatibleWith.contains(a.name)
        return if (aIncompatibleWithB || bIncompatibleWithA) CompatibilityResult.INCOMPATIBLE else CompatibilityResult.COMPATIBLE
    }
}
