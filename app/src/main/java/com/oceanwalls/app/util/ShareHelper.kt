package com.oceanwalls.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.oceanwalls.app.BuildConfig
import com.oceanwalls.app.data.model.Wallpaper
import java.io.File

/** Shares a wallpaper with its full attribution attached. */
object ShareHelper {

    fun shareWallpaper(context: Context, file: File, wallpaper: Wallpaper) {
        val uri = FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.fileprovider", file)
        val caption = buildString {
            append(wallpaper.title).append("\n")
            append("Photo: ").append(wallpaper.displayArtist).append("\n")
            append("Licence: ").append(wallpaper.licenseName)
            if (wallpaper.licenseUrl.isNotBlank()) append(" (").append(wallpaper.licenseUrl).append(")")
            append("\nSource: ").append(wallpaper.sourcePage)
            append("\nShared from OceanWalls")
        }
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, caption)
            putExtra(Intent.EXTRA_SUBJECT, wallpaper.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        runCatching {
            context.startActivity(
                Intent.createChooser(intent, "Share wallpaper").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    fun openUrl(context: Context, url: String) {
        if (url.isBlank()) return
        runCatching {
            context.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
