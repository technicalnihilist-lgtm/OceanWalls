package com.oceanwalls.app.data.remote

/**
 * Rejects files that are not genuine sea photography: maps, diagrams, paintings,
 * book plates, engravings, taxonomy charts and other non-photographic Commons content.
 */
object ImageQualityGate {

    private val JUNK_TITLE = Regex(
        "(map|diagram|chart|graph|logo|sign|poster|stamp|banknote|illustration|drawing|painting|" +
            "engraving|lithograph|cartoon|dictionary|encyclop|scan|book|page|cover|sheet|postcard|" +
            "screenshot|icon|symbol|plan|sketch|clipart|infographic|banner|flag|atlas|plate|" +
            "phylogen|wallet|equipment|serial|skull|skeleton|model|distribution|range map|ranger|" +
            "study|expedition|narrative|pamphlet|brochure|earth at night|compass|infographic|psf|" +
            "comparison|comparisson|size|plot|scientific|wire rope|rope|industry|machine|factory|engine|" +
            "\\b(statue|sculpture|monument|fossil|specimen|holotype|museum|usnm|instructions|notice|" +
            "warning|billboard|placard|chumming|chuming|market|sale|seafood|restaurant|cooking|dish|" +
            "hurricane|typhoon|cyclone|satellite|military|vehicle|assault|amphibious|zoology|" +
            "natural history|limestone|devonian|geologic|outcrop|hubble|galaxy|nebula|telescope|nasa|webb|" +
            "jwst|spitzer|chandra|macs j|deep field|astronom|light-year|quasar|supernova|exoplanet|" +
            "interstellar|cosmic|universe|space station|" +
            "\\b(stilt|tern|gull|egret|heron|" +
            "eagle|hawk|owl|sparrow|crane|flamingo|avocet|plover|sandpiper|pelican)\\b))",
        RegexOption.IGNORE_CASE
    )

    private val JUNK_CATEGORY = Regex(
        "(paintings?|drawings?|illustrations?|diagrams?|maps?|books?|engravings?|lithographs?|" +
            "prints|sculptures?|stamps?|banknotes?|manuscripts?|documents?|texts?|charts?|graphics?|" +
            "logos?|signs?|posters?|cartoons?|comics?|icons?|screenshots?|phylogen|anatom|woodcuts?|" +
            "etchings?|watercolou?rs?|sketches?|postcards?|advertis|encycloped|dictionar|newspapers?|" +
            "magazines?|covers|atlas|models|toys|labels|coins|archives|in art|artworks?|\\bart\\b|" +
            "clipart|vector|pdf)",
        RegexOption.IGNORE_CASE
    )

    /**
     * Ocean imagery only: photographs that show people are rejected. Commons file pages
     * describe their own content ("a diver swims...", "tourists on the beach"), and files
     * with people are almost always filed under a people/diver/tourist category, so the
     * title, description and category list together are a reliable signal.
     */
    private val HUMAN_TERM = Regex(
        "\\b(people|person|persons|human|humans|man|men|woman|women|girl|girls|boy|boys|child|" +
            "children|kid|kids|baby|infant|toddler|tourist|tourists|visitor|visitors|crowd|crowds|" +
            "swimmer|swimmers|diver|divers|diving|scuba|snorkel\\w*|surfer|surfers|surfing|" +
            "lifeguard|lifeguards|model|models|portrait|portraits|selfie|couple|couples|family|" +
            "families|wedding|bride|groom|soldier|soldiers|sailor|sailors|navy|troops|worker|" +
            "workers|fisherman|fishermen|fishing|hand|hands|foot|feet|leg|legs|face|faces|" +
            "mermaid|swimsuit|bikini|wetsuit|swimming pool|beachgoer|beachgoers|sunbather|" +
            "sunbathers|freediver|freedivers|freediving|snorkeler|snorkelers|instructor|" +
            "passenger|passengers|crew|guided|tour|class|students|marines|marine corps|children's)\\b",
        RegexOption.IGNORE_CASE
    )

    private val HUMAN_CATEGORY = Regex(
        "(people|persons|humans|\\bmen\\b|\\bwomen\\b|children|boys|girls|divers|diving|" +
            "scuba|snorkel|tourists|visitors|portrait|selfie|swimmers|surfers|lifeguard|sailors|" +
            "soldiers|navy|beachgoers|sunbathers|bathers|crowds|models|swimming pools|" +
            "recreation|tourism|water sports|water polo)",
        RegexOption.IGNORE_CASE
    )

    fun isHumanFree(title: String, description: String, categories: List<String>): Boolean {
        if (HUMAN_TERM.containsMatchIn(title)) return false
        if (description.isNotBlank() && HUMAN_TERM.containsMatchIn(description)) return false
        return categories.none { HUMAN_CATEGORY.containsMatchIn(it) }
    }

    fun isPhotograph(title: String, categories: List<String>): Boolean {
        if (JUNK_TITLE.containsMatchIn(title.removePrefix("File:"))) return false
        return categories.none { JUNK_CATEGORY.containsMatchIn(it) }
    }

    private val OCEAN_TERM = Regex(
        "(ocean|sea|marine|underwater|coral|reef|coast|beach|shore|wave|water|dive|diver|whale|" +
            "shark|dolphin|turtle|fish|island|lagoon|tide|surf|bay|cliff|harbour|harbor|strait|gulf|" +
            "iceberg|arctic|antarctic|polar|deep|abyss|kelp|seagrass|sand|jellyfish|octopus|squid|" +
            "seahorse|starfish|crab|lobster|seal|penguin|manta|stingray|nautilus|anemone|plankton|" +
            "caustic|grotto|cave)",
        RegexOption.IGNORE_CASE
    )

    fun hasOceanTerm(haystack: String): Boolean = OCEAN_TERM.containsMatchIn(haystack)

    fun matchesSubject(
        haystack: String,
        keywords: List<String>,
        mustMatch: List<String> = emptyList()
    ): Boolean {
        val lower = haystack.lowercase()
        if (keywords.isNotEmpty() && keywords.none { lower.contains(it.lowercase()) }) return false
        return mustMatch.all { group ->
            group.split(',').any { term -> lower.contains(term.trim().lowercase()) }
        }
    }
}
