package com.oceanwalls.app.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oceanwalls.app.data.WallpaperRepository
import com.oceanwalls.app.data.model.Catalog
import com.oceanwalls.app.data.model.WallpaperCategory
import com.oceanwalls.app.ui.feed.FeedController
import kotlinx.coroutines.launch

class CategoryViewModel(
    repository: WallpaperRepository,
    categoryId: String
) : ViewModel() {

    val category: WallpaperCategory? = Catalog.find(categoryId)

    private val controller: FeedController? = category?.let { FeedController(repository, it.spec) }

    val state = controller?.state

    init {
        viewModelScope.launch { controller?.loadNext() }
    }

    fun loadMore() { viewModelScope.launch { controller?.loadNext() } }
    fun refresh() { viewModelScope.launch { controller?.refresh() } }
    fun retry() { viewModelScope.launch { controller?.retry() } }
}
