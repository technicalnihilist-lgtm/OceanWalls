package com.oceanwalls.app.util

/** Wikimedia extmetadata values are HTML fragments; strip tags and decode entities. */
object Html {

    private val TAG = Regex("<[^>]*>")
    private val SPACE = Regex("\\s+")

    fun clean(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var text = TAG.replace(raw, " ")
        text = text.replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")
        return SPACE.replace(text, " ").trim()
    }
}
