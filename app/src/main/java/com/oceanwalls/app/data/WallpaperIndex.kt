package com.oceanwalls.app.data

import com.oceanwalls.app.data.model.Wallpaper

/** Small in-memory LRU so the detail screen never re-fetches metadata. */
class WallpaperIndex {
    private val map = object : LinkedHashMap<Long, Wallpaper>(256, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Long, Wallpaper>?): Boolean = size > 600
    }

    @Synchronized fun put(wallpaper: Wallpaper) { map[wallpaper.pageId] = wallpaper }
    @Synchronized fun putAll(wallpapers: List<Wallpaper>) { wallpapers.forEach { map[it.pageId] = it } }
    @Synchronized fun get(pageId: Long): Wallpaper? = map[pageId]
}
