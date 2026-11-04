package com.oceanwalls.app.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.SearchHistoryRepository
import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.model.FeedSpec
import com.oceanwalls.app.ui.feed.FeedController
import com.oceanwalls.app.ui.feed.FeedUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: WallpaperRepository,
    private val history: SearchHistoryRepository
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow(FeedUiState())
    val state: StateFlow<FeedUiState> = _state.asStateFlow()

    val recentSearches: StateFlow<List<String>> = history.recentSearches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var controller: FeedController? = null
    private var debounceJob: Job? = null
    private var watcher: Job? = null

    fun onQueryChange(value: String) {
        _query.value = value
        debounceJob?.cancel()
        if (value.trim().length < 2) {
            controller = null
            _state.value = FeedUiState()
            return
        }
        debounceJob = viewModelScope.launch {
            delay(450)
            submit(value)
        }
    }

    fun submit(value: String = _query.value) {
        val trimmed = value.trim()
        if (trimmed.length < 2) return
        val newController = FeedController(
            repository,
            FeedSpec(
                id = "search:${trimmed.lowercase()}",
                label = trimmed,
                search = trimmed,
                keywords = emptyList(),
                minWidth = 1000,
                minHeight = 700
            )
        )
        controller = newController
        _state.value = FeedUiState()
        watcher?.cancel()
        watcher = viewModelScope.launch {
            newController.state.collect { _state.value = it }
        }
        viewModelScope.launch {
            history.record(trimmed)
            newController.loadNext()
        }
    }

    fun loadMore() { viewModelScope.launch { controller?.loadNext() } }
    fun retry() { viewModelScope.launch { controller?.retry() } }

    fun clear() {
        _query.value = ""
        controller = null
        _state.value = FeedUiState()
    }
}
