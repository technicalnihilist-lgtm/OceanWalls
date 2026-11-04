package com.oceanwalls.app.ui.screens.downloads

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.data.local.WallpaperJson
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.LicenseChip
import com.oceanwalls.app.ui.components.OceanImage
import com.oceanwalls.app.ui.components.ScreenTopBar
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.theme.SurfaceMid
import com.oceanwalls.app.ui.viewModelFactory

@Composable
fun DownloadsScreen(
    container: AppContainer,
    onOpenWallpaper: (Long) -> Unit,
    onBrowse: () -> Unit
) {
    val viewModel: DownloadsViewModel = viewModel(
        factory = viewModelFactory { DownloadsViewModel(container.downloads) }
    )
    val items by viewModel.items.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "Downloaded",
            subtitle = if (items.isEmpty()) "Nothing downloaded yet" else "${items.size} full-resolution files"
        )
        if (items.isEmpty()) {
            Box(Modifier.fillMaxSize()) {
                EmptyState(
                    title = "No downloads yet",
                    message = "Download a wallpaper to keep the full-resolution file on this device and set it instantly.",
                    actionLabel = "Find a wallpaper",
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
                items(items, key = { it.pageId }) { entity ->
                    val wallpaper = WallpaperJson.decode(entity.payload)
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.72f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(SurfaceMid)
                                .clickable { onOpenWallpaper(entity.pageId) }
                        ) {
                            OceanImage(
                                url = "file://${entity.filePath}",
                                contentDescription = wallpaper?.title,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(
                                    Brush.verticalGradient(
                                        0.55f to Color.Transparent,
                                        1f to Color.Black.copy(alpha = 0.9f)
                                    )
                                )
                            )
                            LicenseChip(
                                licenseName = wallpaper?.licenseName.orEmpty(),
                                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                            )
                            Column(modifier = Modifier.align(Alignment.BottomStart).padding(10.dp)) {
                                Text(
                                    text = wallpaper?.title ?: "Downloaded wallpaper",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Foam,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${entity.bytes / 1024 / 1024} MB",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Mist
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.04f))
                                .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
                                .clickable { viewModel.delete(entity.pageId) }
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = MistDim,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.size(6.dp))
                            Text("Remove", style = MaterialTheme.typography.labelSmall, color = MistDim)
                        }
                    }
                }
                item(span = { GridItemSpan(2) }) {
                    EmptyState(
                        title = "",
                        message = "Downloaded files live in this app's private storage. The original photograph is never modified."
                    )
                }
            }
        }
    }
}
