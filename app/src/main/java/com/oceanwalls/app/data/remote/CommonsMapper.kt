package com.oceanwalls.app.data.remote

import com.google.gson.JsonElement
import com.oceanwalls.app.data.model.FeedSpec
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.util.Html

/**
 * Turns a raw Commons page into a [Wallpaper], or null when the file is not a
 * licensed, photographic, on-subject ocean image.
 */
object CommonsMapper {

    fun toWallpaper(page: CommonsPage, subject: String, spec: FeedSpec?): Wallpaper? {
        val info = page.imageInfo?.firstOrNull() ?: return null
        val meta = info.extMetadata

        val licenseName = meta.text("LicenseShortName")
        val licenseUrl = meta.text("LicenseUrl")
        if (!LicensePolicy.isReusable(licenseName, licenseUrl)) return null

        // Photographs only: Commons diagrams, plots and book plates are almost always PNG/SVG/TIFF.
        val mime = info.mime.orEmpty()
        if (mime != "image/jpeg") return null
        if (info.width <= 0 || info.height <= 0) return null
        if (spec != null && (info.width < spec.minWidth || info.height < spec.minHeight)) return null

        val fullUrl = info.url ?: return null
        val thumbUrl = info.thumbUrl ?: fullUrl
        val sourcePage = info.descriptionUrl ?: return null

        val categories = page.categories?.map { it.title.removePrefix("Category:") }.orEmpty()
        val rawTitle = page.title.removePrefix("File:")
        val title = rawTitle.substringBeforeLast('.', rawTitle)
        val description = meta.text("ImageDescription")

        if (!ImageQualityGate.isPhotograph(page.title, categories)) return null
        // Ocean imagery only - no photographs of people.
        if (!ImageQualityGate.isHumanFree(title, description, categories)) return null

        val haystack = "$title $description ${categories.joinToString(" ")}"
        if (spec != null) {
            if (!ImageQualityGate.matchesSubject(haystack, spec.keywords, spec.mustMatch)) return null
            if (spec.requiresOceanTerm && !ImageQualityGate.hasOceanTerm(haystack)) return null
        }

        val artist = meta.text("Artist").ifBlank { info.user.orEmpty() }
        val objectName = meta.text("ObjectName")

        return Wallpaper(
            pageId = page.pageId,
            title = objectName.ifBlank { title },
            subject = subject,
            fullUrl = fullUrl,
            thumbUrl = thumbUrl,
            width = info.width,
            height = info.height,
            mime = mime,
            artist = artist,
            credit = meta.text("Credit"),
            licenseName = licenseName,
            licenseUrl = licenseUrl,
            description = description,
            sourcePage = sourcePage,
            dateOriginal = meta.text("DateTimeOriginal").ifBlank { info.timestamp.orEmpty() },
            usageTerms = meta.text("UsageTerms"),
            attributionRequired = meta.text("AttributionRequired").equals("true", true),
            modifications = Wallpaper.MODIFICATION_NOTE
        )
    }

    private fun Map<String, CommonsMetaValue>?.text(key: String): String =
        Html.clean(this?.get(key)?.value?.plainText())

    private fun JsonElement?.plainText(): String = when {
        this == null || isJsonNull -> ""
        isJsonPrimitive -> asString
        isJsonObject -> {
            val obj = asJsonObject
            obj.get("value")?.plainText().orEmpty().ifBlank { obj.get("text")?.plainText().orEmpty() }
        }
        else -> ""
    }
}
