package com.oceanwalls.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.ui.components.AttributionFooter
import com.oceanwalls.app.ui.components.CenterLoader
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.InlineError
import com.oceanwalls.app.ui.components.ScreenTopBar
import com.oceanwalls.app.ui.components.WallpaperCard
import com.oceanwalls.app.ui.components.rememberShimmer
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    container: AppContainer,
    categoryId: String,
    onBack: () -> Unit,
    onOpenWallpaper: (Long) -> Unit
) {
    val viewModel: CategoryViewModel = viewModel(
        key = categoryId,
        factory = viewModelFactory { CategoryViewModel(container.wallpapers, categoryId) }
    )
    val stateFlow = viewModel.state
    val gridState = rememberLazyGridState()

    if (stateFlow == null) {
        Box(Modifier.fillMaxSize()) {
            EmptyState(
                title = "Unknown collection",
                message = "This collection does not exist.",
                actionLabel = "Go back",
                onAction = onBack
            )
        }
        return
    }

    val state by stateFlow.collectAsStateWithLifecycle()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.items.size - 4 && state.items.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.endReached && !state.isLoading) viewModel.loadMore()
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            ScreenTopBar(
                title = viewModel.category?.label ?: "Collection",
                subtitle = viewModel.category?.group?.title,
                onBack = onBack
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                state = gridState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (state.showSkeleton) {
                    items(6) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(rememberShimmer())
                        )
                    }
                }

                items(state.items, key = { it.pageId }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.pageId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (state.isLoading && state.items.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) { CenterLoader() }
                }

                if (state.error != null && state.items.isEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        InlineError(state.error ?: "", onRetry = { viewModel.retry() })
                    }
                }

                if (state.isEmpty) {
                    item(span = { GridItemSpan(2) }) {
                        EmptyState(
                            title = "Nothing to show yet",
                            message = "No licence-clean photograph matched this collection. Pull down to refresh.",
                            actionLabel = "Retry",
                            onAction = { viewModel.retry() }
                        )
                    }
                }

                if (state.endReached && state.items.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Text(
                            text = "That is every licensed photograph in this collection.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MistDim,
                            modifier = Modifier.padding(vertical = 18.dp)
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) { AttributionFooter() }
            }
        }
    }
}
