package com.oceanwalls.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.RecentRepository
import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.model.Catalog
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.ui.feed.FeedController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DailyState {
    data object Loading : DailyState
    data class Ready(val wallpaper: Wallpaper) : DailyState
    data class Failed(val message: String) : DailyState
}

sealed interface RecentState {
    data object Loading : RecentState
    data class Ready(val items: List<Wallpaper>) : RecentState
    data class Failed(val message: String) : RecentState
}

class HomeViewModel(
    private val repository: WallpaperRepository,
    recents: RecentRepository
) : ViewModel() {

    val featured = FeedController(repository, Catalog.featured)
    val deepSea = FeedController(repository, Catalog.deepSea)
    val coralReef = FeedController(repository, Catalog.coralReef)
    val popular = FeedController(repository, Catalog.popularOcean)
    val landscapes = FeedController(repository, Catalog.oceanLandscapeFeed)

    private val _daily = MutableStateFlow<DailyState>(DailyState.Loading)
    val daily: StateFlow<DailyState> = _daily.asStateFlow()

    private val _recent = MutableStateFlow<RecentState>(RecentState.Loading)
    val recent: StateFlow<RecentState> = _recent.asStateFlow()

    val recentlyViewed: StateFlow<List<Wallpaper>> = recents.recentlyViewed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val trendingAnimals = Catalog.trendingAnimals
    val collections = Catalog.specialCollections

    private var bootstrapped = false

    fun bootstrap() {
        if (bootstrapped) return
        bootstrapped = true
        loadDaily()
        loadRecentUploads()
        viewModelScope.launch { featured.loadNext() }
        viewModelScope.launch { deepSea.loadNext() }
        viewModelScope.launch { coralReef.loadNext() }
        viewModelScope.launch { popular.loadNext() }
        viewModelScope.launch { landscapes.loadNext() }
    }

    fun refreshAll() {
        viewModelScope.launch { featured.refresh() }
        viewModelScope.launch { deepSea.refresh() }
        viewModelScope.launch { coralReef.refresh() }
        viewModelScope.launch { popular.refresh() }
        viewModelScope.launch { landscapes.refresh() }
        loadDaily()
        loadRecentUploads()
    }

    fun retry(controller: FeedController) {
        viewModelScope.launch { controller.retry() }
    }

    fun loadDaily() {
        _daily.value = DailyState.Loading
        viewModelScope.launch {
            repository.dailyWallpaper()
                .onSuccess { _daily.value = DailyState.Ready(it) }
                .onFailure { _daily.value = DailyState.Failed(it.message ?: "Daily pick unavailable") }
        }
    }

    fun loadRecentUploads() {
        _recent.value = RecentState.Loading
        viewModelScope.launch {
            repository.recentUploads(24)
                .onSuccess { _recent.value = RecentState.Ready(it) }
                .onFailure { _recent.value = RecentState.Failed(it.message ?: "Feed unavailable") }
        }
    }
}
