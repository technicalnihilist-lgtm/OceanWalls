package com.oceanwalls.app.data.remote

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName

/** Response shapes of https://commons.wikimedia.org/w/api.php (formatversion=2). */
data class CommonsResponse(
    @SerializedName("batchcomplete") val batchComplete: Boolean? = null,
    @SerializedName("continue") val continueToken: CommonsContinue? = null,
    @SerializedName("query") val query: CommonsQuery? = null,
    @SerializedName("error") val error: CommonsError? = null
)

data class CommonsError(
    @SerializedName("code") val code: String? = null,
    @SerializedName("info") val info: String? = null
)

data class CommonsContinue(
    @SerializedName("gcmcontinue") val gcmContinue: String? = null,
    @SerializedName("gsroffset") val gsrOffset: Int? = null,
    @SerializedName("rccontinue") val rcContinue: String? = null
)

data class CommonsQuery(
    @SerializedName("pages") val pages: List<CommonsPage>? = null,
    @SerializedName("recentchanges") val recentChanges: List<RecentChange>? = null
)

data class CommonsPage(
    @SerializedName("pageid") val pageId: Long = 0L,
    @SerializedName("title") val title: String = "",
    @SerializedName("imageinfo") val imageInfo: List<CommonsImageInfo>? = null,
    @SerializedName("categories") val categories: List<CommonsCategoryRef>? = null
)

data class CommonsCategoryRef(@SerializedName("title") val title: String = "")

data class CommonsImageInfo(
    @SerializedName("url") val url: String? = null,
    @SerializedName("descriptionurl") val descriptionUrl: String? = null,
    @SerializedName("thumburl") val thumbUrl: String? = null,
    @SerializedName("thumbwidth") val thumbWidth: Int = 0,
    @SerializedName("thumbheight") val thumbHeight: Int = 0,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0,
    @SerializedName("size") val size: Long = 0L,
    @SerializedName("mime") val mime: String? = null,
    @SerializedName("user") val user: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("extmetadata") val extMetadata: Map<String, CommonsMetaValue>? = null
)

data class CommonsMetaValue(
    @SerializedName("value") val value: JsonElement? = null,
    @SerializedName("source") val source: String? = null,
    @SerializedName("hidden") val hidden: Boolean? = null
)

data class RecentChange(
    @SerializedName("title") val title: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("type") val type: String? = null
)
