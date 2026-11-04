package com.oceanwalls.app.ui.screens.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.ScreenTopBar
import com.oceanwalls.app.ui.components.WallpaperCard
import com.oceanwalls.app.ui.viewModelFactory

@Composable
fun FavoritesScreen(
    container: AppContainer,
    onOpenWallpaper: (Long) -> Unit,
    onBrowse: () -> Unit
) {
    val viewModel: FavoritesViewModel = viewModel(
        factory = viewModelFactory { FavoritesViewModel(container.favorites) }
    )
    val items by viewModel.items.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "Favourites",
            subtitle = if (items.isEmpty()) "Nothing saved yet" else "${items.size} saved wallpapers"
        )
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                EmptyState(
                    title = "No favourites yet",
                    message = "Tap the heart on any wallpaper to keep it here.",
                    actionLabel = "Browse the ocean",
                    onAction = onBrowse
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(items, key = { it.pageId }) { wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.pageId) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item(span = { GridItemSpan(2) }) {
                    EmptyState(
                        title = "",
                        message = "Favourites are stored on this device and keep their photographer credits."
                    )
                }
            }
        }
    }
}
