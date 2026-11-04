package com.oceanwalls.app.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SaveAlt
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wallpaper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.ui.components.CenterLoader
import com.oceanwalls.app.ui.components.EmptyState
import com.oceanwalls.app.ui.components.LicenseChip
import com.oceanwalls.app.ui.components.OceanImage
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Hairline
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.theme.SurfaceMid
import com.oceanwalls.app.ui.viewModelFactory
import com.oceanwalls.app.util.ShareHelper
import com.oceanwalls.app.util.WallpaperSetter
import kotlinx.coroutines.delay

@Composable
fun DetailScreen(
    container: AppContainer,
    pageId: Long,
    onBack: () -> Unit,
    onOpenCredits: () -> Unit
) {
    val viewModel: DetailViewModel = viewModel(
        key = "detail:$pageId",
        factory = viewModelFactory {
            DetailViewModel(
                pageId = pageId,
                repository = container.wallpapers,
                favorites = container.favorites,
                downloads = container.downloads,
                recents = container.recents,
                setter = container.wallpaperSetter
            )
        }
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(2600)
            viewModel.consumeMessage()
        }
    }

    val wallpaper = state.wallpaper

    if (wallpaper == null) {
        Box(Modifier.fillMaxSize()) {
            if (state.loading) {
                CenterLoader(Modifier.padding(top = 120.dp))
            } else {
                EmptyState(
                    title = "Wallpaper unavailable",
                    message = state.error ?: "Open this wallpaper again from a collection.",
                    actionLabel = "Go back",
                    onAction = onBack
                )
            }
        }
        return
    }

    Box(Modifier.fillMaxSize()) {
        OceanImage(
            url = wallpaper.thumbUrl,
            contentDescription = wallpaper.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to Color.Black.copy(alpha = 0.55f),
                    0.28f to Color.Transparent,
                    0.55f to Color.Black.copy(alpha = 0.35f),
                    1f to Color.Black.copy(alpha = 0.92f)
                )
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundIconButton(icon = Icons.AutoMirrored.Filled.ArrowBack, description = "Back", onClick = onBack)
            Spacer(Modifier.weight(1f))
            RoundIconButton(
                icon = if (state.isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                description = "Favourite",
                onClick = { viewModel.toggleFavorite() },
                tint = if (state.isFavorite) Aqua else Foam
            )
            Spacer(Modifier.width(8.dp))
            RoundIconButton(
                icon = Icons.Outlined.Share,
                description = "Share",
                onClick = {
                    viewModel.prepareShare { file, item ->
                        ShareHelper.shareWallpaper(context, file, item)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(Color.Black.copy(alpha = 0.88f))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.07f),
                    RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                )
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            if (state.message != null) {
                MessageBar(state.message ?: "")
                Spacer(Modifier.height(12.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = wallpaper.subject.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Aqua
                )
                Spacer(Modifier.width(8.dp))
                LicenseChip(wallpaper.licenseName)
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.headlineLarge,
                color = Foam,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Photo: ${wallpaper.displayArtist}",
                style = MaterialTheme.typography.bodyMedium,
                color = Mist,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${wallpaper.resolutionLabel}  •  ${wallpaper.megapixels}  •  ${wallpaper.orientation}",
                style = MaterialTheme.typography.labelSmall,
                color = MistDim
            )

            if (wallpaper.description.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = wallpaper.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Mist,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(18.dp))
            Text("SET AS WALLPAPER", style = MaterialTheme.typography.labelSmall, color = MistDim)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    icon = Icons.Outlined.PhoneAndroid,
                    label = "Home",
                    busy = state.applyingTarget == WallpaperSetter.Target.HOME,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setWallpaper(WallpaperSetter.Target.HOME) }
                )
                ActionButton(
                    icon = Icons.Outlined.Lock,
                    label = "Lock",
                    busy = state.applyingTarget == WallpaperSetter.Target.LOCK,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setWallpaper(WallpaperSetter.Target.LOCK) }
                )
                ActionButton(
                    icon = Icons.Outlined.Wallpaper,
                    label = "Both",
                    busy = state.applyingTarget == WallpaperSetter.Target.BOTH,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.setWallpaper(WallpaperSetter.Target.BOTH) }
                )
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ActionButton(
                    icon = Icons.Outlined.Download,
                    label = if (state.isDownloaded) "Saved" else "Download",
                    busy = state.downloadProgress != null,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.download() }
                )
                ActionButton(
                    icon = Icons.Outlined.SaveAlt,
                    label = "Gallery",
                    busy = false,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.saveToGallery() }
                )
                ActionButton(
                    icon = Icons.Outlined.Share,
                    label = "Share",
                    busy = false,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.prepareShare { file, item ->
                            ShareHelper.shareWallpaper(context, file, item)
                        }
                    }
                )
            }

            if (state.downloadProgress != null) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { state.downloadProgress ?: 0f },
                    modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                    color = Aqua,
                    trackColor = SurfaceMid
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Downloading full resolution image...",
                    style = MaterialTheme.typography.labelSmall,
                    color = MistDim
                )
            }

            Spacer(Modifier.height(22.dp))
            AttributionBlock(wallpaper, onOpenCredits)
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MessageBar(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Aqua.copy(alpha = 0.12f))
            .border(1.dp, Aqua.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(text = message, style = MaterialTheme.typography.bodySmall, color = Aqua)
    }
}

@Composable
private fun RoundIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    tint: Color = Foam
) {
    Box(
        modifier = Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = description, tint = tint, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    busy: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
            .clickable(enabled = !busy, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (busy) {
            CircularProgressIndicator(color = Aqua, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
        } else {
            Icon(icon, contentDescription = null, tint = Aqua, modifier = Modifier.size(19.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium, color = Foam, maxLines = 1)
    }
}

@Composable
private fun AttributionBlock(wallpaper: Wallpaper, onOpenCredits: () -> Unit) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Hairline, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Text("IMAGE CREDITS", style = MaterialTheme.typography.labelSmall, color = Aqua)
        Spacer(Modifier.height(10.dp))
        CreditLine("Photographer", wallpaper.displayArtist)
        if (wallpaper.credit.isNotBlank()) CreditLine("Credit", wallpaper.credit)
        CreditLine("Licence", wallpaper.licenseName)
        if (wallpaper.licenseUrl.isNotBlank()) CreditLine("Licence URL", wallpaper.licenseUrl)
        CreditLine("Source", "Wikimedia Commons")
        if (wallpaper.dateOriginal.isNotBlank()) CreditLine("Date", wallpaper.dateOriginal.take(24))
        CreditLine("Modifications", wallpaper.modifications)

        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Aqua.copy(alpha = 0.1f))
                .clickable { ShareHelper.openUrl(context, wallpaper.sourcePage) }
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Aqua, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text = "Open original on Wikimedia Commons",
                style = MaterialTheme.typography.labelLarge,
                color = Aqua
            )
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Tap for full attribution and licence details",
            style = MaterialTheme.typography.labelSmall,
            color = MistDim,
            modifier = Modifier.clickable(onClick = onOpenCredits)
        )
    }
}

@Composable
private fun CreditLine(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MistDim,
            modifier = Modifier.width(96.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Foam,
            modifier = Modifier.weight(1f)
        )
    }
}
