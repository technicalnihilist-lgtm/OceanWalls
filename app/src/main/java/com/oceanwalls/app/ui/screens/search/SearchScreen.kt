package com.oceanwalls.app.ui.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.ui.components.CenterLoader
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.InlineError
import com.oceanwalls.app.ui.components.ScreenTopBar
import com.oceanwalls.app.ui.components.WallpaperCard
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.theme.SurfaceMid
import com.oceanwalls.app.ui.viewModelFactory

private val suggestions = listOf(
    "whale shark", "coral reef", "jellyfish", "sea turtle", "orca",
    "underwater cave", "ocean sunset", "kelp forest", "manta ray", "iceberg"
)

@Composable
fun SearchScreen(
    container: AppContainer,
    onBack: () -> Unit,
    onOpenWallpaper: (Long) -> Unit
) {
    val viewModel: SearchViewModel = viewModel(
        factory = viewModelFactory { SearchViewModel(container.wallpapers, container.searchHistory) }
    )
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val last = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= state.items.size - 4 && state.items.isNotEmpty()
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !state.endReached && !state.isLoading) viewModel.loadMore()
    }

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(title = "Search", subtitle = "Marine life, reefs, coastlines", onBack = onBack)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MistDim, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            TextField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                placeholder = {
                    Text(
                        "Try 'humpback whale' or 'coral reef'",
                        color = MistDim,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Foam),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { viewModel.submit() }),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Aqua
                ),
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) {
                IconButton(onClick = viewModel::clear) {
                    Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Mist)
                }
            }
        }

        if (query.trim().length < 2) {
            Column(Modifier.padding(top = 8.dp)) {
                if (recentSearches.isNotEmpty()) {
                    Text(
                        text = "RECENT SEARCHES",
                        style = MaterialTheme.typography.labelSmall,
                        color = MistDim,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(recentSearches) { term ->
                            SuggestionPill(term) { viewModel.onQueryChange(term) }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                }
                Text(
                    text = "SUGGESTIONS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MistDim,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(suggestions) { term ->
                        SuggestionPill(term) { viewModel.onQueryChange(term) }
                    }
                }
                EmptyState(
                    title = "Search the ocean",
                    message = "Every result is a real photograph from Wikimedia Commons with its photographer and licence attached."
                )
            }
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            state = gridState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.items, key = { it.pageId }) { wallpaper ->
                WallpaperCard(
                    wallpaper = wallpaper,
                    onClick = { onOpenWallpaper(wallpaper.pageId) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (state.isLoading && state.items.isEmpty()) {
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
                        title = "No licensed photos found",
                        message = "Nothing licence-clean matched \"${query.trim()}\". Try a broader term like 'reef' or 'whale'."
                    )
                }
            }
            if (state.isLoading && state.items.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) { CenterLoader() }
            }
        }
    }
}

@Composable
private fun SuggestionPill(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(SurfaceMid)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = Foam)
    }
}
