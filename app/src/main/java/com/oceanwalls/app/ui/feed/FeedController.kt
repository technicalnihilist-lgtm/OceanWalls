package com.oceanwalls.app.ui.feed

import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.model.FeedSpec
import com.oceanwalls.app.data.model.Wallpaper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class FeedUiState(
    val items: List<Wallpaper> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val loadedOnce: Boolean = false,
    val endReached: Boolean = false,
    val error: String? = null
) {
    val isEmpty: Boolean get() = loadedOnce && items.isEmpty() && error == null
    val showSkeleton: Boolean get() = isLoading && items.isEmpty()
}

/**
 * Pagination driver for one feed. Refresh deliberately jumps deeper into the live
 * Commons result set, so pulling down always brings new photographs, never repeats.
 */
class FeedController(
    private val repository: WallpaperRepository,
    private val spec: FeedSpec
) {
    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    private var cursor: String? = null
    private var inFlight = false

    suspend fun loadNext() {
        if (inFlight) return
        val current = _state.value
        if (current.endReached && current.loadedOnce) return

        inFlight = true
        _state.value = current.copy(isLoading = true, error = null)
        try {
            var page = repository.loadFeed(spec, cursor)
            var attempts = 0
            while (page.isSuccess && page.getOrNull()?.items?.isEmpty() == true &&
                page.getOrNull()?.hasMore == true && attempts < 3
            ) {
                cursor = page.getOrNull()?.nextCursor
                page = repository.loadFeed(spec, cursor)
                attempts++
            }
            page.onSuccess { result ->
                cursor = result.nextCursor
                val merged = (current.items + result.items).distinctBy { it.pageId }
                _state.value = _state.value.copy(
                    items = merged,
                    isLoading = false,
                    loadedOnce = true,
                    endReached = !result.hasMore,
                    error = null
                )
            }.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    loadedOnce = true,
                    error = throwable.message ?: "Could not reach Wikimedia Commons"
                )
            }
        } finally {
            inFlight = false
        }
    }

    suspend fun refresh() {
        if (inFlight) return
        inFlight = true
        _state.value = _state.value.copy(isRefreshing = true, error = null)
        try {
            val seen = repository.cachedCount(spec.id)
            var nextCursor: String? = if (seen > 0) "s:$seen" else null
            var result = repository.loadFeed(spec, nextCursor)
            if (result.isSuccess && result.getOrNull()?.items.isNullOrEmpty() && nextCursor != null) {
                nextCursor = null
                result = repository.loadFeed(spec, nextCursor)
            }
            result.onSuccess { page ->
                cursor = page.nextCursor
                _state.value = _state.value.copy(
                    items = page.items.ifEmpty { _state.value.items },
                    isRefreshing = false,
                    isLoading = false,
                    loadedOnce = true,
                    endReached = !page.hasMore,
                    error = null
                )
            }.onFailure { throwable ->
                _state.value = _state.value.copy(
                    isRefreshing = false,
                    isLoading = false,
                    loadedOnce = true,
                    error = throwable.message ?: "Could not reach Wikimedia Commons"
                )
            }
        } finally {
            inFlight = false
        }
    }

    suspend fun retry() {
        _state.value = _state.value.copy(error = null, endReached = false)
        loadNext()
    }
}
