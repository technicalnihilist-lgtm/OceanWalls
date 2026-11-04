package com.oceanwalls.app.data.model

/**
 * One wallpaper, built only from real Wikimedia Commons metadata.
 * Every field comes from the MediaWiki API (imageinfo + extmetadata) for that file.
 */
data class Wallpaper(
    val pageId: Long,
    val title: String,
    val subject: String,
    val fullUrl: String,
    val thumbUrl: String,
    val width: Int,
    val height: Int,
    val mime: String,
    val artist: String,
    val credit: String,
    val licenseName: String,
    val licenseUrl: String,
    val description: String,
    val sourcePage: String,
    val dateOriginal: String,
    val usageTerms: String,
    val attributionRequired: Boolean,
    val modifications: String
) {
    val resolutionLabel: String get() = "$width x $height"
    val megapixels: String get() = String.format("%.1f MP", (width.toLong() * height.toLong()) / 1_000_000.0)
    val orientation: String get() = when {
        width > height * 1.15f -> "Landscape"
        height > width * 1.15f -> "Portrait"
        else -> "Square"
    }
    val displayArtist: String get() = artist.ifBlank { "Photographer not named on source page" }

    companion object {
        const val MODIFICATION_NOTE =
            "OceanWalls does not alter the artwork. Previews are resized and centre-cropped by the app; " +
                "wallpapers are set from the unmodified original file."
    }
}

data class FeedPage(
    val items: List<Wallpaper>,
    val nextCursor: String?,
    val hasMore: Boolean
)

data class FeedSpec(
    val id: String,
    val label: String,
    val search: String,
    val keywords: List<String>,
    val category: String? = null,
    val minWidth: Int = 1200,
    val minHeight: Int = 800,
    /** Each entry is a group: at least one comma separated term must appear. All groups must match. */
    val mustMatch: List<String> = emptyList()
) {
    /**
     * Search results are noisier than curated categories, so feeds without a Commons
     * category additionally require an unambiguous ocean word in the metadata.
     */
    val requiresOceanTerm: Boolean get() = category == null
}
