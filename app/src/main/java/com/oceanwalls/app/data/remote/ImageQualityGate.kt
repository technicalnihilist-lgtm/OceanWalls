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
            "comparison|comparisson|size|plot|scientific|wire rope|rope|industry|machine|factory|engine)",
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

    fun matchesSubject(haystack: String, keywords: List<String>): Boolean {
        if (keywords.isEmpty()) return true
        val lower = haystack.lowercase()
        return keywords.any { lower.contains(it.lowercase()) }
    }
}
