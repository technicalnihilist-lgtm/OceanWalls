package com.oceanwalls.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT pageId FROM favorites")
    fun observeIds(): Flow<List<Long>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE pageId = :pageId)")
    fun observeIsFavorite(pageId: Long): Flow<Boolean>

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun all(): List<FavoriteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE pageId = :pageId")
    suspend fun delete(pageId: Long)
}

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY savedAt DESC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE pageId = :pageId LIMIT 1")
    suspend fun find(pageId: Long): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: DownloadEntity)

    @Query("DELETE FROM downloads WHERE pageId = :pageId")
    suspend fun delete(pageId: Long)
}

@Dao
interface RecentDao {
    @Query("SELECT * FROM recents ORDER BY viewedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<RecentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentEntity)

    @Query("DELETE FROM recents WHERE pageId NOT IN (SELECT pageId FROM recents ORDER BY viewedAt DESC LIMIT :keep)")
    suspend fun trim(keep: Int)
}

@Dao
interface FeedDao {
    @Query("SELECT * FROM feed_items WHERE feedKey = :feedKey ORDER BY position ASC")
    fun observeFeed(feedKey: String): Flow<List<FeedItemEntity>>

    @Query("SELECT * FROM feed_items WHERE pageId = :pageId LIMIT 1")
    suspend fun findByPageId(pageId: Long): FeedItemEntity?

    @Query("SELECT COUNT(*) FROM feed_items WHERE feedKey = :feedKey")
    suspend fun count(feedKey: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<FeedItemEntity>): List<Long>
}

@Dao
interface DailyDao {
    @Query("SELECT * FROM daily_picks WHERE date = :date LIMIT 1")
    suspend fun find(date: String): DailyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: DailyEntity)
}

@Dao
interface SearchDao {
    @Query("SELECT * FROM searches ORDER BY searchedAt DESC LIMIT 10")
    fun observeRecent(): Flow<List<SearchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun put(entity: SearchEntity)

    @Query("DELETE FROM searches WHERE query NOT IN (SELECT query FROM searches ORDER BY searchedAt DESC LIMIT 10)")
    suspend fun trim()
}
