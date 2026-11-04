package com.oceanwalls.app.data.remote

/** MediaWiki query parameters used by OceanWalls. */
object CommonsParams {

    const val API_BASE = "https://commons.wikimedia.org/"

    const val USER_AGENT =
        "OceanWalls/1.0 (Android wallpaper app; contact: nihaonihao54059@gmail.com) OkHttp"

    const val EXT_METADATA_FILTER =
        "ObjectName|Artist|Credit|LicenseShortName|LicenseUrl|ImageDescription|DateTimeOriginal|" +
            "DateTime|UsageTerms|AttributionRequired|Copyrighted|Restrictions"

    private const val IMAGE_PROPS = "url|size|mime|extmetadata|user|timestamp"

    /** Files that live directly in a Commons category (human curated, high precision). */
    fun categoryFiles(category: String, limit: Int, cursor: String?): Map<String, String> =
        base() + mapOf(
            "generator" to "categorymembers",
            "gcmtitle" to "Category:$category",
            "gcmtype" to "file",
            "gcmlimit" to limit.toString()
        ) + (cursor?.let { mapOf("gcmcontinue" to it) } ?: emptyMap())

    /** Full-text search fallback with offset pagination. */
    fun searchFiles(query: String, limit: Int, offset: Int): Map<String, String> =
        base() + mapOf(
            "generator" to "search",
            "gsrsearch" to "$query filetype:bitmap",
            "gsrnamespace" to "6",
            "gsrlimit" to limit.toString(),
            "gsroffset" to offset.toString()
        )

    /** Newest uploads in the File namespace - powers the "Recently added" rail. */
    fun recentUploads(limit: Int): Map<String, String> = mapOf(
        "action" to "query",
        "format" to "json",
        "formatversion" to "2",
        "list" to "recentchanges",
        "rcnamespace" to "6",
        "rctype" to "new",
        "rcprop" to "title|timestamp",
        "rclimit" to limit.toString(),
        "rcshow" to "!bot"
    )

    /** Batch metadata lookup by file title (50 titles per request). */
    fun byTitles(titles: List<String>): Map<String, String> =
        base() + mapOf(
            "titles" to titles.joinToString("|"),
            "prop" to "imageinfo",
            "iiprop" to IMAGE_PROPS,
            "iiurlwidth" to "800",
            "iiextmetadatafilter" to EXT_METADATA_FILTER
        )

    private fun base(): Map<String, String> = mapOf(
        "action" to "query",
        "format" to "json",
        "formatversion" to "2",
        "prop" to "imageinfo|categories",
        "iiprop" to IMAGE_PROPS,
        "iiurlwidth" to "800",
        "iiextmetadatafilter" to EXT_METADATA_FILTER,
        "clshow" to "!hidden",
        "cllimit" to "max"
    )
}
