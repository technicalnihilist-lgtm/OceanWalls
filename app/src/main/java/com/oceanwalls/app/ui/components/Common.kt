package com.oceanwalls.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.oceanwalls.app.data.model.Wallpaper
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Hairline
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.ui.theme.SurfaceMid

@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Int = 24,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius.dp)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color.White.copy(alpha = 0.055f), Color.White.copy(alpha = 0.018f))
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.07f), shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun rememberShimmer(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "shimmerProgress"
    )
    return Brush.linearGradient(
        colors = listOf(SurfaceMid, Color(0xFF15242A), SurfaceMid),
        start = Offset(progress * 900f - 400f, 0f),
        end = Offset(progress * 900f, 400f)
    )
}

@Composable
fun OceanImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    AsyncImage(
        model = ImageRequest.Builder(context).data(url).crossfade(280).build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier.background(SurfaceMid)
    )
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.headlineMedium, color = Foam)
            if (subtitle != null) {
                Spacer(Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MistDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = Aqua)
            }
        }
    }
}

@Composable
fun LicenseChip(licenseName: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.55f))
            .border(1.dp, Aqua.copy(alpha = 0.35f), RoundedCornerShape(50))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = licenseName.ifBlank { "Licence on source" },
            style = MaterialTheme.typography.labelSmall,
            color = Aqua,
            maxLines = 1
        )
    }
}

@Composable
fun WallpaperCard(
    wallpaper: Wallpaper,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 0.72f,
    showBadge: Boolean = true
) {
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(shape)
            .background(SurfaceMid)
            .clickable(onClick = onClick)
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
                    0.55f to Color.Transparent,
                    1f to Color.Black.copy(alpha = 0.88f)
                )
            )
        )
        if (showBadge) {
            LicenseChip(
                licenseName = wallpaper.licenseName,
                modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Text(
                text = wallpaper.title,
                style = MaterialTheme.typography.titleMedium,
                color = Foam,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "${wallpaper.subject} • ${wallpaper.resolutionLabel}",
                style = MaterialTheme.typography.labelSmall,
                color = Mist,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    offline: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(Aqua.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (offline) Icons.Outlined.CloudOff else Icons.Outlined.SearchOff,
                contentDescription = null,
                tint = Aqua,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(Modifier.height(14.dp))
        Text(text = title, style = MaterialTheme.typography.titleLarge, color = Foam)
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MistDim,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onAction) {
                Text(actionLabel, color = Aqua, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
fun InlineError(message: String, modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MistDim,
            textAlign = TextAlign.Center
        )
        TextButton(onClick = onRetry) {
            Text("Try again", color = Aqua, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun CenterLoader(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(28.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Aqua, strokeWidth = 2.dp, modifier = Modifier.size(26.dp))
    }
}

@Composable
fun ScreenTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Foam)
            }
        } else {
            Spacer(Modifier.width(8.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = Foam,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MistDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        actions()
    }
}

@Composable
fun AttributionFooter(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 24.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Spacer(Modifier.height(14.dp))
        Text(
            text = "Every photograph is served live by the Wikimedia Commons API and keeps its original " +
                "photographer, licence and source link. OceanWalls claims no ownership of any image.",
            style = MaterialTheme.typography.bodySmall,
            color = MistDim
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Licence-clean by design: CC0, Public Domain, CC BY and CC BY-SA only.",
            style = MaterialTheme.typography.labelSmall,
            color = Aqua.copy(alpha = 0.7f),
            fontWeight = FontWeight.Medium
        )
    }
}
