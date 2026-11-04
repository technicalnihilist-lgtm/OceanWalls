package com.oceanwalls.app.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.DownloadRepository
import com.oceanwalls.app.data.FavoritesRepository
import com.oceanwalls.app.data.RecentRepository
import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.util.WallpaperSetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DetailUiState(
    val wallpaper: Wallpaper? = null,
    val loading: Boolean = true,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadProgress: Float? = null,
    val applyingTarget: WallpaperSetter.Target? = null,
    val message: String? = null
)

class DetailViewModel(
    private val pageId: Long,
    private val repository: WallpaperRepository,
    private val favorites: FavoritesRepository,
    private val downloads: DownloadRepository,
    private val recents: RecentRepository,
    private val setter: WallpaperSetter
) : ViewModel() {

    private val _state = MutableStateFlow(DetailUiState())
    val state: StateFlow<DetailUiState> = _state.asStateFlow()

    init {
        load()
        viewModelScope.launch {
            favorites.isFavorite(pageId).collect { fav ->
                _state.value = _state.value.copy(isFavorite = fav)
            }
        }
        viewModelScope.launch {
            downloads.downloads.collect { rows ->
                _state.value = _state.value.copy(isDownloaded = rows.any { it.pageId == pageId })
            }
        }
    }

    fun load() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            val wallpaper = repository.byId(pageId)
            if (wallpaper == null) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = "This wallpaper is not cached. Open it again from a collection."
                )
                return@launch
            }
            recents.record(wallpaper)
            _state.value = _state.value.copy(wallpaper = wallpaper, loading = false, error = null)
        }
    }

    fun toggleFavorite() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            val added = favorites.toggle(wallpaper)
            _state.value = _state.value.copy(
                message = if (added) "Added to favourites" else "Removed from favourites"
            )
        }
    }

    fun download() {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.downloadProgress != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(downloadProgress = 0f, message = null)
            downloads.download(wallpaper) { progress ->
                _state.value = _state.value.copy(downloadProgress = progress)
            }.onSuccess {
                _state.value = _state.value.copy(
                    downloadProgress = null,
                    isDownloaded = true,
                    message = "Full-resolution file saved"
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    downloadProgress = null,
                    message = it.message ?: "Download failed"
                )
            }
        }
    }

    fun setWallpaper(target: WallpaperSetter.Target) {
        val wallpaper = _state.value.wallpaper ?: return
        if (_state.value.applyingTarget != null) return
        viewModelScope.launch {
            _state.value = _state.value.copy(applyingTarget = target, message = null)
            val message = runCatching { downloads.ensureLocal(wallpaper) }.fold(
                onSuccess = { file -> setter.set(file, target).getOrElse { it.message ?: "Could not set wallpaper" } },
                onFailure = { it.message ?: "Could not download the wallpaper" }
            )
            _state.value = _state.value.copy(applyingTarget = null, message = message)
        }
    }

    fun saveToGallery() {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(message = "Saving to gallery...")
            val file = runCatching { downloads.ensureLocal(wallpaper) }.getOrNull()
            if (file == null) {
                _state.value = _state.value.copy(message = "Could not download the wallpaper")
                return@launch
            }
            downloads.exportToGallery(file, wallpaper.title)
                .onSuccess { _state.value = _state.value.copy(message = it) }
                .onFailure { _state.value = _state.value.copy(message = it.message ?: "Could not save to gallery") }
        }
    }

    fun prepareShare(onReady: (File, Wallpaper) -> Unit) {
        val wallpaper = _state.value.wallpaper ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(message = "Preparing image...")
            val file = withContext(Dispatchers.IO) { runCatching { downloads.ensureLocal(wallpaper) }.getOrNull() }
            if (file != null) {
                _state.value = _state.value.copy(message = null)
                onReady(file, wallpaper)
            } else {
                _state.value = _state.value.copy(message = "Could not prepare the image")
            }
        }
    }

    fun consumeMessage() { _state.value = _state.value.copy(message = null) }
}
