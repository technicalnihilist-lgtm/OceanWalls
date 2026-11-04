package com.oceanwalls.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.data.model.WallpaperCategory
import com.oceanwalls.app.ui.components.AttributionFooter
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.InlineError
import com.oceanwalls.app.ui.components.LicenseChip
import com.oceanwalls.app.ui.components.OceanImage
import com.oceanwalls.app.ui.components.SectionHeader
import com.oceanwalls.app.ui.components.WallpaperCard
import com.oceanwalls.app.ui.components.rememberShimmer
import com.oceanwalls.app.ui.feed.FeedUiState
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Hairline
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.theme.SurfaceMid
import com.oceanwalls.app.ui.viewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    container: AppContainer,
    onOpenWallpaper: (Long) -> Unit,
    onOpenCategory: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenCredits: () -> Unit,
    onOpenFavorites: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel(
        factory = viewModelFactory { HomeViewModel(container.wallpapers, container.recents) }
    )

    LaunchedEffect(Unit) { viewModel.bootstrap() }

    val featured by viewModel.featured.state.collectAsStateWithLifecycle()
    val daily by viewModel.daily.collectAsStateWithLifecycle()
    val deepSea by viewModel.deepSea.state.collectAsStateWithLifecycle()
    val coral by viewModel.coralReef.state.collectAsStateWithLifecycle()
    val popular by viewModel.popular.state.collectAsStateWithLifecycle()
    val landscapes by viewModel.landscapes.state.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val recentlyViewed by viewModel.recentlyViewed.collectAsStateWithLifecycle()

    val refreshing = featured.isRefreshing || popular.isRefreshing

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel.refreshAll() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item { HomeHeader(onOpenCredits = onOpenCredits) }
            item { SearchEntry(onClick = onOpenSearch) }

            item {
                DailyHero(
                    state = daily,
                    onOpen = onOpenWallpaper,
                    onRetry = { viewModel.loadDaily() }
                )
            }

            item {
                RailSection(
                    title = "Featured underwater",
                    subtitle = "Hand-picked from live Wikimedia Commons results",
                    state = featured,
                    onOpenWallpaper = onOpenWallpaper,
                    onRetry = { viewModel.retry(viewModel.featured) },
                    onSeeAll = { onOpenCategory("underwater-world") }
                )
            }

            item {
                SectionHeader(
                    title = "Trending marine animals",
                    subtitle = "Explore 22 marine species"
                )
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.trendingAnimals, key = { it.id }) { category ->
                        AnimalPill(category) { onOpenCategory(category.id) }
                    }
                }
            }

            item {
                RailSection(
                    title = "Deep Sea collection",
                    subtitle = "Cold, dark and enormous",
                    state = deepSea,
                    onOpenWallpaper = onOpenWallpaper,
                    onRetry = { viewModel.retry(viewModel.deepSea) },
                    onSeeAll = { onOpenCategory("deep-sea") }
                )
            }

            item {
                RailSection(
                    title = "Popular ocean wallpapers",
                    subtitle = "What the ocean community is looking at",
                    state = popular,
                    onOpenWallpaper = onOpenWallpaper,
                    onRetry = { viewModel.retry(viewModel.popular) },
                    onSeeAll = { onOpenCategory("open-ocean") }
                )
            }

            item {
                RailSection(
                    title = "Coral reef collection",
                    subtitle = "The rainforests of the sea",
                    state = coral,
                    onOpenWallpaper = onOpenWallpaper,
                    onRetry = { viewModel.retry(viewModel.coralReef) },
                    onSeeAll = { onOpenCategory("coral-reefs") }
                )
            }

            item {
                RailSection(
                    title = "Ocean landscapes",
                    subtitle = "Coastlines, cliffs and open water",
                    state = landscapes,
                    onOpenWallpaper = onOpenWallpaper,
                    onRetry = { viewModel.retry(viewModel.landscapes) },
                    onSeeAll = { onOpenCategory("beaches") }
                )
            }

            item {
                SectionHeader(
                    title = "Recently added",
                    subtitle = "Fresh ocean uploads, straight from Commons"
                )
            }
            item {
                when (val state = recent) {
                    is RecentState.Loading -> RailSkeleton()
                    is RecentState.Failed -> InlineError(state.message, onRetry = { viewModel.loadRecentUploads() })
                    is RecentState.Ready -> {
                        if (state.items.isEmpty()) {
                            EmptyState(
                                title = "No new uploads",
                                message = "Nothing new matched the ocean filters in the last upload window. Pull down to refresh."
                            )
                        } else {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.items, key = { it.pageId }) { wallpaper ->
                                    WallpaperCard(
                                        wallpaper = wallpaper,
                                        onClick = { onOpenWallpaper(wallpaper.pageId) },
                                        modifier = Modifier.width(150.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (recentlyViewed.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Recently viewed",
                        subtitle = "Pick up where you left off",
                        actionLabel = "Favourites",
                        onAction = onOpenFavorites
                    )
                }
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recentlyViewed, key = { it.pageId }) { wallpaper ->
                            WallpaperCard(
                                wallpaper = wallpaper,
                                onClick = { onOpenWallpaper(wallpaper.pageId) },
                                modifier = Modifier.width(150.dp)
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader(title = "Special collections", subtitle = "Curated moods from the deep")
            }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.collections, key = { it.id }) { category ->
                        CollectionPill(category) { onOpenCategory(category.id) }
                    }
                }
            }

            item { AttributionFooter() }
        }
    }
}

@Composable
private fun HomeHeader(onOpenCredits: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 8.dp, top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "OCEANWALLS",
                style = MaterialTheme.typography.titleLarge,
                color = Foam,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "PREMIUM OCEAN PHOTOGRAPHY",
                style = MaterialTheme.typography.labelSmall,
                color = Aqua.copy(alpha = 0.8f)
            )
        }
        IconButton(onClick = onOpenCredits) {
            Icon(Icons.Outlined.Info, contentDescription = "Credits and licences", tint = Mist)
        }
    }
}

@Composable
private fun SearchEntry(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Color.White.copy(alpha = 0.07f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Search, contentDescription = null, tint = MistDim, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Text(
            text = "Search marine life, reefs, coastlines...",
            style = MaterialTheme.typography.bodyMedium,
            color = MistDim
        )
    }
}

@Composable
private fun DailyHero(state: DailyState, onOpen: (Long) -> Unit, onRetry: () -> Unit) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        SectionHeader(title = "Daily ocean wallpaper", subtitle = "A new photograph every day")
        when (state) {
            is DailyState.Loading -> Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .aspectRatio(1.35f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(rememberShimmer())
            )
            is DailyState.Failed -> InlineError(state.message, onRetry = onRetry)
            is DailyState.Ready -> {
                val wallpaper = state.wallpaper
                Box(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .fillMaxWidth()
                        .aspectRatio(1.35f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceMid)
                        .clickable { onOpen(wallpaper.pageId) }
                ) {
                    OceanImage(
                        url = wallpaper.thumbUrl,
                        contentDescription = wallpaper.title,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                0f to Color.Transparent,
                                0.45f to Color.Transparent,
                                1f to Color.Black.copy(alpha = 0.92f)
                            )
                        )
                    )
                    LicenseChip(
                        licenseName = wallpaper.licenseName,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(18.dp)) {
                        Text("TODAY'S PICK", style = MaterialTheme.typography.labelSmall, color = Aqua)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = wallpaper.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = Foam,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Photo: ${wallpaper.displayArtist}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Mist,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RailSection(
    title: String,
    subtitle: String,
    state: FeedUiState,
    onOpenWallpaper: (Long) -> Unit,
    onRetry: () -> Unit,
    onSeeAll: () -> Unit
) {
    Column(modifier = Modifier.padding(top = 14.dp)) {
        SectionHeader(title = title, subtitle = subtitle, actionLabel = "See all", onAction = onSeeAll)
        when {
            state.showSkeleton -> RailSkeleton()
            state.error != null && state.items.isEmpty() -> InlineError(state.error, onRetry = onRetry)
            state.isEmpty -> EmptyState(
                title = "No licensed photos here",
                message = "No reusable photograph matched this collection right now. Pull down to refresh.",
                actionLabel = "Try again",
                onAction = onRetry
            )
            else -> LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.pageId }) { wallpaper: Wallpaper ->
                    WallpaperCard(
                        wallpaper = wallpaper,
                        onClick = { onOpenWallpaper(wallpaper.pageId) },
                        modifier = Modifier.width(158.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RailSkeleton() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(158.dp)
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(rememberShimmer())
            )
        }
    }
}

@Composable
private fun AnimalPill(category: WallpaperCategory, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.05f))
            .border(1.dp, Hairline, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = category.label, style = MaterialTheme.typography.labelLarge, color = Foam)
    }
}

@Composable
private fun CollectionPill(category: WallpaperCategory, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(Aqua.copy(alpha = 0.14f), Color.Transparent)))
            .border(1.dp, Aqua.copy(alpha = 0.22f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = category.label, style = MaterialTheme.typography.labelLarge, color = Aqua)
    }
}
