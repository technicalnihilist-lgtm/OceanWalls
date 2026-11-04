package com.oceanwalls.app.data

import com.oceanwalls.app.data.local.FavoriteDao
import com.oceanwalls.app.data.local.FavoriteEntity
import com.oceanwalls.app.data.local.RecentDao
import com.oceanwalls.app.data.local.RecentEntity
import com.oceanwalls.app.data.local.SearchDao
import com.oceanwalls.app.data.local.SearchEntity
import com.oceanwalls.app.data.local.WallpaperJson
import com.oceanwalls.app.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepository(private val dao: FavoriteDao) {

    val favorites: Flow<List<Wallpaper>> =
        dao.observeAll().map { rows -> rows.mapNotNull { WallpaperJson.decode(it.payload) } }

    val favoriteIds: Flow<Set<Long>> = dao.observeIds().map { it.toSet() }

    fun isFavorite(pageId: Long): Flow<Boolean> = dao.observeIsFavorite(pageId)

    suspend fun toggle(wallpaper: Wallpaper): Boolean = withContext(Dispatchers.IO) {
        val already = dao.all().any { it.pageId == wallpaper.pageId }
        if (already) {
            dao.delete(wallpaper.pageId)
            false
        } else {
            dao.insert(FavoriteEntity(wallpaper.pageId, WallpaperJson.encode(wallpaper), System.currentTimeMillis()))
            true
        }
    }
}

class RecentRepository(private val dao: RecentDao) {

    val recentlyViewed: Flow<List<Wallpaper>> =
        dao.observeRecent(24).map { rows -> rows.mapNotNull { WallpaperJson.decode(it.payload) } }

    suspend fun record(wallpaper: Wallpaper) = withContext(Dispatchers.IO) {
        dao.upsert(RecentEntity(wallpaper.pageId, WallpaperJson.encode(wallpaper), System.currentTimeMillis()))
        dao.trim(60)
    }
}

class SearchHistoryRepository(private val dao: SearchDao) {

    val recentSearches: Flow<List<String>> = dao.observeRecent().map { rows -> rows.map { it.query } }

    suspend fun record(query: String) = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length >= 2) {
            dao.put(SearchEntity(trimmed, System.currentTimeMillis()))
            dao.trim()
        }
    }
}
