package com.oceanwalls.app.ui.screens.credits

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.oceanwalls.app.ui.components.ScreenTopBar
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.Foam
import com.oceanwalls.app.ui.theme.Hairline
import com.oceanwalls.app.ui.theme.Mist
import com.oceanwalls.app.ui.theme.MistDim
import com.oceanwalls.app.util.ShareHelper

private data class LicenseInfo(val name: String, val note: String, val url: String)

private val licenses = listOf(
    LicenseInfo(
        "CC0 1.0 / Public Domain",
        "No rights reserved. Free for personal and commercial use; no attribution legally required, but the photographer is always credited.",
        "https://creativecommons.org/publicdomain/zero/1.0/"
    ),
    LicenseInfo(
        "CC BY 2.0 / 3.0 / 4.0",
        "Attribution required. Credit the photographer, keep the licence name and link, and indicate any changes.",
        "https://creativecommons.org/licenses/by/4.0/"
    ),
    LicenseInfo(
        "CC BY-SA 3.0 / 4.0",
        "Attribution + ShareAlike. Credit the photographer, link the licence, and share any adaptation under the same licence.",
        "https://creativecommons.org/licenses/by-sa/4.0/"
    )
)

@Composable
fun CreditsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        ScreenTopBar(
            title = "Credits & licences",
            subtitle = "Where every photograph comes from",
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "OceanWalls is a viewing gallery, not an image owner.",
                style = MaterialTheme.typography.headlineMedium,
                color = Foam
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Every wallpaper is a real photograph served live by the official Wikimedia " +
                    "Commons API (commons.wikimedia.org/w/api.php). Nothing is generated, recoloured " +
                    "or re-drawn by the app. Each image keeps its own photographer, licence and source " +
                    "page, and the app refuses to display any file whose licence is unknown, " +
                    "non-commercial, no-derivatives or fair use.",
                style = MaterialTheme.typography.bodyMedium,
                color = Mist
            )

            Spacer(Modifier.height(22.dp))
            Text("HOW ATTRIBUTION WORKS", style = MaterialTheme.typography.labelSmall, color = Aqua)
            Spacer(Modifier.height(8.dp))
            Bullet("The detail screen of every wallpaper lists the photographer, licence name, licence URL, original file page and modification note.")
            Bullet("Shared images carry the same credit text, so attribution travels with the file.")
            Bullet("Downloaded files are the unmodified originals - the app never strips metadata or watermarks.")
            Bullet("If a licence requires ShareAlike, the obligation is shown on the wallpaper itself.")

            Spacer(Modifier.height(22.dp))
            Text("LICENCES USED", style = MaterialTheme.typography.labelSmall, color = Aqua)
            Spacer(Modifier.height(8.dp))
            licenses.forEach { license ->
                LicenseCard(license) { ShareHelper.openUrl(context, license.url) }
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(12.dp))
            Text("DATA & TOOLS", style = MaterialTheme.typography.labelSmall, color = Aqua)
            Spacer(Modifier.height(8.dp))
            LinkRow("Wikimedia Commons API", "https://commons.wikimedia.org/w/api.php") {
                ShareHelper.openUrl(context, "https://commons.wikimedia.org/w/api.php")
            }
            LinkRow("Wikimedia Commons licence policy", "https://commons.wikimedia.org/wiki/Commons:Licensing") {
                ShareHelper.openUrl(context, "https://commons.wikimedia.org/wiki/Commons:Licensing")
            }
            LinkRow("Manrope typeface (SIL Open Font License 1.1)", "https://openfontlicense.org") {
                ShareHelper.openUrl(context, "https://openfontlicense.org")
            }

            Spacer(Modifier.height(22.dp))
            Text(
                text = "OceanWalls never claims ownership of third-party photographs. If you are a " +
                    "photographer and want an image removed, contact the app developer and it will be " +
                    "filtered out immediately.",
                style = MaterialTheme.typography.bodySmall,
                color = MistDim
            )
            Spacer(Modifier.height(28.dp))
        }
    }
}

@Composable
private fun Bullet(text: String) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text("•", color = Aqua, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = Mist)
    }
}

@Composable
private fun LicenseCard(license: LicenseInfo, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Hairline, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Text(
            text = license.name,
            style = MaterialTheme.typography.titleMedium,
            color = Foam,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(license.note, style = MaterialTheme.typography.bodySmall, color = MistDim)
    }
}

@Composable
private fun LinkRow(title: String, url: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Aqua.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = Foam)
            Text(url, style = MaterialTheme.typography.labelSmall, color = MistDim, maxLines = 1)
        }
        Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = Aqua, modifier = Modifier.size(16.dp))
    }
    Spacer(Modifier.height(8.dp))
}
