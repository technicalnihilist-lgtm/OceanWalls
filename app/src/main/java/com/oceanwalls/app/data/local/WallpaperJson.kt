package com.oceanwalls.app.data.local

import com.google.gson.Gson
import com.oceanwalls.app.data.model.Wallpaper

/** Wallpapers are stored as JSON so cached rows survive schema changes. */
object WallpaperJson {
    private val gson = Gson()

    fun encode(wallpaper: Wallpaper): String = gson.toJson(wallpaper)

    fun decode(payload: String?): Wallpaper? = try {
        if (payload.isNullOrBlank()) null else gson.fromJson(payload, Wallpaper::class.java)
    } catch (_: Exception) {
        null
    }
}
