package com.oceanwalls.app.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.oceanwalls.app.data.local.DownloadDao
import com.oceanwalls.app.data.local.DownloadEntity
import com.oceanwalls.app.data.local.WallpaperJson
import com.oceanwalls.app.data.model.Wallpaper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

/**
 * Real downloads: the full-resolution original is streamed to app storage (temp file
 * first, so a failed transfer never leaves a corrupt wallpaper) and can also be copied
 * into the device gallery.
 */
class DownloadRepository(
    private val context: Context,
    private val client: OkHttpClient,
    private val dao: DownloadDao
) {

    val downloads: Flow<List<DownloadEntity>> = dao.observeAll()

    private val directory: File
        get() = File(context.filesDir, "wallpapers").apply { if (!exists()) mkdirs() }

    fun fileFor(pageId: Long): File = File(directory, "$pageId.jpg")

    suspend fun find(pageId: Long): DownloadEntity? = withContext(Dispatchers.IO) { dao.find(pageId) }

    suspend fun download(wallpaper: Wallpaper, onProgress: (Float) -> Unit = {}): Result<File> =
        withContext(Dispatchers.IO) {
            runCatching {
                val target = fileFor(wallpaper.pageId)
                if (target.exists() && target.length() > 32 * 1024) {
                    onProgress(1f)
                    return@runCatching target
                }
                val temp = File(directory, "${wallpaper.pageId}.part")
                val request = Request.Builder().url(wallpaper.fullUrl).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                    val body = response.body ?: throw IOException("Empty response body")
                    val total = body.contentLength()
                    body.byteStream().use { input ->
                        temp.outputStream().use { output ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var written = 0L
                            while (true) {
                                val read = input.read(buffer)
                                if (read <= 0) break
                                output.write(buffer, 0, read)
                                written += read
                                if (total > 0) onProgress((written.toFloat() / total).coerceIn(0f, 1f))
                            }
                            output.flush()
                        }
                    }
                }
                if (!temp.exists() || temp.length() < 1024) {
                    temp.delete()
                    throw IOException("Downloaded file was empty")
                }
                if (target.exists()) target.delete()
                if (!temp.renameTo(target)) throw IOException("Could not store wallpaper")

                dao.insert(
                    DownloadEntity(
                        pageId = wallpaper.pageId,
                        payload = WallpaperJson.encode(wallpaper),
                        filePath = target.absolutePath,
                        bytes = target.length(),
                        savedAt = System.currentTimeMillis()
                    )
                )
                onProgress(1f)
                target
            }
        }

    suspend fun ensureLocal(wallpaper: Wallpaper): File {
        val existing = fileFor(wallpaper.pageId)
        if (existing.exists() && existing.length() > 32 * 1024) return existing
        return download(wallpaper).getOrThrow()
    }

    suspend fun delete(pageId: Long) = withContext(Dispatchers.IO) {
        fileFor(pageId).delete()
        dao.delete(pageId)
    }

    /** Copies the wallpaper into Pictures/OceanWalls so it also lives in the gallery. */
    suspend fun exportToGallery(file: File, title: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val safeTitle = title.replace(Regex("[^A-Za-z0-9]+"), "_").trim('_').take(48).ifBlank { "wallpaper" }
            val name = "OceanWalls_${safeTitle}_${System.currentTimeMillis()}.jpg"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, name)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/OceanWalls")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                    ?: throw IOException("Could not create gallery entry")
                resolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } }
                    ?: throw IOException("Could not open gallery stream")
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                "Saved to Pictures/OceanWalls"
            } else {
                @Suppress("DEPRECATION")
                val folder = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "OceanWalls"
                ).apply { if (!exists()) mkdirs() }
                val destination = File(folder, name)
                file.copyTo(destination, overwrite = true)
                @Suppress("DEPRECATION")
                MediaStore.Images.Media.insertImage(
                    context.contentResolver, destination.absolutePath, destination.name, "OceanWalls"
                )
                "Saved to Pictures/OceanWalls"
            }
        }
    }
}
