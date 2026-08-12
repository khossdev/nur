package com.example.mysalat.ui.placeholder

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mysalat.ui.components.GlassCard
import com.example.mysalat.ui.components.GradientSurface
import com.example.mysalat.ui.components.IconBadge
import com.example.mysalat.ui.icons.AppIcon
import com.example.mysalat.ui.icons.AppIcons
import com.example.mysalat.ui.icons.IconSize
import com.example.mysalat.ui.theme.MySalatTheme
import com.example.mysalat.ui.theme.Radius
import com.example.mysalat.ui.theme.Spacing
import com.example.mysalat.ui.theme.brand

/**
 * Premium placeholder for features that are planned but not built yet. Uses the
 * same gradient header and card language as the finished screens so the app
 * never feels half-finished.
 */
@Composable
fun ComingSoonScreen(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    highlights: List<String>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    footer: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        GradientHeader(title = title, subtitle = subtitle, icon = icon)

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                Text(
                    text = "Bientôt disponible",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                highlights.forEach { line ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconBadge(
                            icon = AppIcons.Sparkles,
                            size = 28.dp,
                            iconSize = IconSize.sm,
                            cornerRadius = Radius.sm
                        )
                        Text(
                            text = line,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        footer?.invoke()
    }
}

@Composable
private fun GradientHeader(
    title: String,
    subtitle: String,
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier
) {
    val onHero = Color.White

    GradientSurface(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 210.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(onHero.copy(alpha = 0.10f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(Radius.md))
                    .background(onHero.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                AppIcon(
                    icon = icon,
                    contentDescription = null,
                    size = IconSize.xl,
                    tint = onHero
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = onHero
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = onHero.copy(alpha = 0.85f),
                textAlign = TextAlign.Start
            )
        }
    }
}

@Preview(name = "Bientôt clair", heightDp = 700)
@Composable
private fun ComingSoonLightPreview() {
    MySalatTheme(darkTheme = false) {
        ComingSoonScreen(
            title = "Coran",
            subtitle = "Lisez, écoutez et mémorisez, sourate par sourate.",
            icon = AppIcons.Quran,
            highlights = listOf(
                "Texte arabe et traduction française",
                "Reprise à votre dernière lecture",
                "Récitations audio hors connexion"
            ),
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = Spacing.md)
        )
    }
}

@Preview(name = "Bientôt sombre", heightDp = 700)
@Composable
private fun ComingSoonDarkPreview() {
    MySalatTheme(darkTheme = true) {
        ComingSoonScreen(
            title = "Coran",
            subtitle = "Lisez, écoutez et mémorisez, sourate par sourate.",
            icon = AppIcons.Quran,
            highlights = listOf(
                "Texte arabe et traduction",
                "Reprise à la dernière lecture",
                "Récitations audio"
            ),
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = Spacing.md)
        )
    }
}
