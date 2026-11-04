package com.oceanwalls.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        DownloadEntity::class,
        RecentEntity::class,
        FeedItemEntity::class,
        DailyEntity::class,
        SearchEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class OceanDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun downloadDao(): DownloadDao
    abstract fun recentDao(): RecentDao
    abstract fun feedDao(): FeedDao
    abstract fun dailyDao(): DailyDao
    abstract fun searchDao(): SearchDao

    companion object { const val NAME = "oceanwalls.db" }
}
