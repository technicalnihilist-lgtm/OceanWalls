package com.oceanwalls.app.data.remote

/**
 * Strict allow-list licence gate. Only images whose licence permits reuse are shown.
 * Unknown, non-commercial, no-derivatives, fair-use and all-rights-reserved files are rejected.
 */
object LicensePolicy {

    private val ALLOWED = listOf(
        Regex("^cc0", RegexOption.IGNORE_CASE),
        Regex("^cc[ -]?zero", RegexOption.IGNORE_CASE),
        Regex("^public domain", RegexOption.IGNORE_CASE),
        Regex("^pd($|[ -])", RegexOption.IGNORE_CASE),
        Regex("^no restrictions", RegexOption.IGNORE_CASE),
        Regex("^cc[ -]by(-sa)?([ -]?\\d(\\.\\d)?)?$", RegexOption.IGNORE_CASE),
        Regex("^cc[ -]by(-sa)?[ -]\\d\\.\\d", RegexOption.IGNORE_CASE)
    )

    private val BLOCKED = Regex(
        "(non[- ]?commercial|\\bnc\\b|no[- ]?deriv|\\bnd\\b|fair use|all rights|copyright|gfdl|permission|\\u00a9)",
        RegexOption.IGNORE_CASE
    )

    fun isReusable(licenseName: String?, licenseUrl: String?): Boolean {
        val name = licenseName?.trim().orEmpty()
        if (name.isEmpty()) return false
        if (BLOCKED.containsMatchIn(name)) return false
        if (ALLOWED.none { it.containsMatchIn(name) }) return false
        val needsUrl = !(name.startsWith("CC0", true) ||
            name.startsWith("Public domain", true) ||
            name.startsWith("PD", true) ||
            name.startsWith("No restrictions", true))
        return !needsUrl || !licenseUrl.isNullOrBlank()
    }

    fun obligations(licenseName: String): String = when {
        licenseName.startsWith("CC0", true) || licenseName.contains("Public domain", true) ->
            "No rights reserved - free for personal and commercial use. Credit shown as a courtesy."
        licenseName.contains("BY-SA", true) ->
            "Attribution + ShareAlike: credit the photographer, link the licence, share adaptations under the same licence."
        licenseName.contains("BY", true) ->
            "Attribution: credit the photographer and link the licence."
        else -> "Credit the photographer and link the licence."
    }
}
