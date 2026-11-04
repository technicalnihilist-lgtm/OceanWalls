package com.oceanwalls.app.util

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Sets wallpapers through Android's WallpaperManager for home, lock or both. */
class WallpaperSetter(private val context: Context) {

    enum class Target(val label: String) {
        HOME("Home screen"),
        LOCK("Lock screen"),
        BOTH("Home + lock screen")
    }

    suspend fun set(file: File, target: Target): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (!file.exists()) throw IOException("Wallpaper file is missing")
            val manager = WallpaperManager.getInstance(context)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (!manager.isWallpaperSupported) throw IOException("Wallpapers are not supported on this device")
                if (!manager.isSetWallpaperAllowed) throw IOException("Setting wallpapers is blocked on this device")
            }

            val bitmap = decode(file)
            try {
                when (target) {
                    Target.HOME -> {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        "Home screen updated"
                    }
                    Target.LOCK -> {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        "Lock screen updated"
                    }
                    Target.BOTH -> {
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                        manager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                        "Home and lock screen updated"
                    }
                }
            } finally {
                if (!bitmap.isRecycled) bitmap.recycle()
            }
        }
    }

    /** Samples the decode so a 40 MP photograph never blows the heap. */
    private fun decode(file: File): Bitmap {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) throw IOException("Image could not be decoded")

        val manager = WallpaperManager.getInstance(context)
        val targetWidth = maxOf(manager.desiredMinimumWidth, 1080)
        val targetHeight = maxOf(manager.desiredMinimumHeight, 1920)

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= targetWidth && bounds.outHeight / (sample * 2) >= targetHeight) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return BitmapFactory.decodeFile(file.absolutePath, options)
            ?: throw IOException("Image could not be decoded")
    }
}
