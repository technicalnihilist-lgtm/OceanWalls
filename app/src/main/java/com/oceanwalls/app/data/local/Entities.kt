package com.oceanwalls.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val pageId: Long,
    val payload: String,
    val addedAt: Long
)

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val pageId: Long,
    val payload: String,
    val filePath: String,
    val bytes: Long,
    val savedAt: Long
)

@Entity(tableName = "recents")
data class RecentEntity(
    @PrimaryKey val pageId: Long,
    val payload: String,
    val viewedAt: Long
)

/** Cached feed rows: offline fallback plus built-in duplicate prevention. */
@Entity(
    tableName = "feed_items",
    primaryKeys = ["feedKey", "pageId"],
    indices = [Index(value = ["feedKey", "position"]), Index(value = ["pageId"])]
)
data class FeedItemEntity(
    val feedKey: String,
    val pageId: Long,
    val payload: String,
    val position: Int,
    val fetchedAt: Long
)

@Entity(tableName = "daily_picks")
data class DailyEntity(
    @PrimaryKey val date: String,
    val payload: String,
    val pickedAt: Long
)

@Entity(tableName = "searches")
data class SearchEntity(
    @PrimaryKey val query: String,
    val searchedAt: Long
)
