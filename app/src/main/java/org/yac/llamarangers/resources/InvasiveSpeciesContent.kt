package org.yac.llamarangers.resources

import org.yac.llamarangers.domain.model.enums.InvasiveSpecies
import org.yac.llamarangers.domain.model.enums.TreatmentMethod

/**
 * Static guide content for the identification guide.
 * Ports iOS InvasiveSpeciesContent.
 */
object InvasiveSpeciesContent {

    data class SpeciesInfo(
        val species: InvasiveSpecies,
        val commonName: String,
        val scientificName: String,
        val imageName: String?,
        val distinguishingFeatures: String,
        val controlMethods: List<TreatmentMethod>,
        val seasonalNotes: String?,
        val priorityLevel: String
    )

    val all: List<SpeciesInfo> = listOf(
        SpeciesInfo(
            species = InvasiveSpecies.LANTANA,
            commonName = "Lantana",
            scientificName = "Lantana camara",
            imageName = "demo_lantana_1",
            distinguishingFeatures = "Dense, branching shrub 0.5–4m tall. Stems are square in cross-section with small downward-curved prickles.\n\nLeaves: Opposite, oval, 2–10cm, with toothed margins and a rough sandpaper-like surface. Strong unpleasant smell when crushed.\n\nFlowers: Small tubular flowers in flat-topped clusters (2–3cm across). Flowers change colour with age — often mixed pink, orange, and yellow on the same head. Multiple colour forms occur.\n\nFruit: Small, fleshy berries, green turning shiny black when ripe. Berries are toxic to livestock and humans.\n\nHabitat: Roadsides, creek lines, disturbed land, cleared areas, coastal scrub.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.CUT_STUMP, TreatmentMethod.BASAL_BARK, TreatmentMethod.SPLAT_GUN),
            seasonalNotes = "Autumn (Apr–May) optimal for foliar spray and basal bark. During wet season, check for lantana bug (Aconophora compressa) before applying herbicide to pink-flowered plants — biocontrol insects may be present. Avoid spraying drought-stressed plants.",
            priorityLevel = "Critical"
        ),
        SpeciesInfo(
            species = InvasiveSpecies.RUBBER_VINE,
            commonName = "Rubber Vine",
            scientificName = "Cryptostegia grandiflora",
            imageName = null,
            distinguishingFeatures = "Vigorous woody vine or scrambling shrub reaching high into tree canopy. Produces milky latex sap when cut.\n\nLeaves: Opposite, broadly oval, 5–10cm, dark glossy green with prominent central vein. Thick and leathery.\n\nFlowers: Large (5–7cm) pink to pale purple trumpet-shaped flowers. Flowers Aug–Oct.\n\nSeed pods: Distinctive V-shaped pairs, 8–12cm long, grey-green turning brown. When dry they split to release seeds attached to white silky fibres that float on wind and water.\n\nHabitat: River banks, floodplains, creek lines, open woodland. Spreads via floodwater.",
            controlMethods = listOf(TreatmentMethod.STEM_INJECTION, TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.BASAL_BARK),
            seasonalNotes = "Peak flowering Aug–Oct — best identification time. Treat before seed set (Oct–Nov) to prevent floodwater dispersal. Stem injection is effective year-round and minimises herbicide exposure near waterways.",
            priorityLevel = "Critical"
        ),
        SpeciesInfo(
            species = InvasiveSpecies.PRICKLY_ACACIA,
            commonName = "Prickly Acacia",
            scientificName = "Vachellia nilotica",
            imageName = null,
            distinguishingFeatures = "Small thorny tree 3–7m tall with a spreading, often flat-topped canopy.\n\nThorns: Pairs of straight white spines 3–8cm long at each leaf node — distinctive and sharp.\n\nLeaves: Bipinnate (feathery), dark green, 3–8cm.\n\nFlowers: Bright yellow, ball-shaped (1cm diameter), clustered at nodes. Highly fragrant. Flowers mainly dry season.\n\nPods: Flat, constricted between seeds, forming a knobbly chain 8–20cm long in clusters. Grey-green turning brown.\n\nBark: Dark grey, deeply furrowed on mature trees.\n\nHabitat: Floodplains, creek margins, black soil plains, disturbed pastoral land.",
            controlMethods = listOf(TreatmentMethod.CUT_STUMP, TreatmentMethod.STEM_INJECTION, TreatmentMethod.FOLIAR_SPRAY),
            seasonalNotes = "Pods form and fall dry season (May–Sep), spreading seed widely. Prioritise removal of seed-bearing trees before pod fall. Biological control agents (seed weevils) may be present — check before spraying.",
            priorityLevel = "High"
        ),
        SpeciesInfo(
            species = InvasiveSpecies.SICKLEPOD,
            commonName = "Sicklepod",
            scientificName = "Senna obtusifolia",
            imageName = null,
            distinguishingFeatures = "Erect annual herb or short-lived shrub, 0.5–1.5m tall.\n\nLeaves: Compound with 3 pairs of oval leaflets (2–5cm each). A small gland is visible at the base of the lowest pair of leaflets — a key ID feature.\n\nFlowers: Yellow, 5 petals, 1–1.5cm. Flowers in leaf axils.\n\nPods: Long (10–20cm), narrow, slightly curved like a sickle. Seeds are square-ish, grey-brown.\n\nSmell: Plant has a characteristic unpleasant smell.\n\nHabitat: Disturbed roadsides, paddock edges, creek banks, cleared land. Common in higher-rainfall areas.",
            controlMethods = listOf(TreatmentMethod.MECHANICAL, TreatmentMethod.FOLIAR_SPRAY),
            seasonalNotes = "Flowers and seeds in wet season (Nov–Apr). Mechanical removal is most effective when plants are young (< 30cm). Prioritise removal before seed set to prevent population growth.",
            priorityLevel = "Moderate"
        ),
        SpeciesInfo(
            species = InvasiveSpecies.GIANT_RATS_TAIL_GRASS,
            commonName = "Giant Rat's Tail Grass",
            scientificName = "Sporobolus pyramidalis",
            imageName = null,
            distinguishingFeatures = "Robust perennial tussock grass forming large dense clumps 0.5–1.5m tall.\n\nLeaves: Long, narrow, flat to folded, with a rolled/compressed sheath. Leaf blades are rough to touch.\n\nSeed heads: Distinctive tall 'rattails' 15–30cm long, rough and bristly. Multiple spikes form a large open panicle. Seeds very small and abundant.\n\nRoots: Tough, deep root system making mechanical removal difficult once established.\n\nHabitat: Roadsides, disturbed paddocks, cleared areas, open woodland. Spreads rapidly on disturbed soils.",
            controlMethods = listOf(TreatmentMethod.FOLIAR_SPRAY, TreatmentMethod.FIRE_MANAGEMENT, TreatmentMethod.MECHANICAL),
            seasonalNotes = "Seed heads ripen late dry season (Aug–Oct). Slashing or burning before seed set significantly reduces spread. Hot late dry-season burns can reduce established clumps. Wet season burning can also be effective in suitable fire management plans.",
            priorityLevel = "High"
        ),
        SpeciesInfo(
            species = InvasiveSpecies.POND_APPLE,
            commonName = "Pond Apple",
            scientificName = "Annona glabra",
            imageName = null,
            distinguishingFeatures = "Small to medium riparian tree, 3–12m tall.\n\nLeaves: Large (10–20cm), oval, glossy dark green with prominent veins. Alternate arrangement.\n\nFlowers: Cream to pale yellow, 3–4cm, with 3 fleshy petals. Slightly unpleasant smell.\n\nFruit: Large (7–12cm), irregular spherical, yellow-green, warty surface — resembling a rough apple. The fruit floats and is dispersed by water.\n\nHabitat: Freshwater wetlands, stream and river margins, swamps, estuarine fringes. Forms dense thickets that displace native riparian vegetation.",
            controlMethods = listOf(TreatmentMethod.STEM_INJECTION, TreatmentMethod.CUT_STUMP, TreatmentMethod.FOLIAR_SPRAY),
            seasonalNotes = "Fruit floats and spreads downstream. Prioritise removal of upstream plants to prevent downstream seed dispersal. Treat when fruit is immature (before it drops). Stem injection minimises herbicide runoff near waterways.",
            priorityLevel = "High"
        )
    )

    fun infoFor(species: InvasiveSpecies): SpeciesInfo? {
        return all.firstOrNull { it.species == species }
    }
}
