package com.example.mysalat.ui.qibla

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mysalat.QiblaUiState
import com.example.mysalat.ui.components.GlassCard
import com.example.mysalat.ui.components.GradientSurface
import com.example.mysalat.ui.icons.AppIcon
import com.example.mysalat.ui.icons.AppIcons
import com.example.mysalat.ui.icons.IconSize
import com.example.mysalat.ui.theme.Motion
import com.example.mysalat.ui.theme.MySalatTheme
import com.example.mysalat.ui.theme.Radius
import com.example.mysalat.ui.theme.Spacing
import com.example.mysalat.ui.theme.brand
import kotlin.math.roundToInt

@Composable
fun QiblaScreen(
    state: QiblaUiState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    DisposableEffect(Unit) {
        onStart()
        onDispose { onStop() }
    }

    val haptic = LocalHapticFeedback.current
    var wasAligned by remember { mutableStateOf(false) }
    LaunchedEffect(state.aligned) {
        if (state.aligned && !wasAligned) {
            haptic.performHapticFeedback(HapticFeedbackType.Confirm)
        }
        wasAligned = state.aligned
    }

    val status = statusCopy(state)
    val talkBack = "Qibla, ${state.cityName}, ${state.qiblaDegrees.roundToInt()} degrés. $status"

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = Spacing.md)
            .semantics { contentDescription = talkBack },
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        QiblaHeader(
            subtitle = "Depuis ${state.cityName}"
        )

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                CompassDisc(
                    needleDegrees = state.needleDegrees,
                    aligned = state.aligned,
                    showNeedle = !state.atKaaba
                )

                Text(
                    text = "${state.qiblaDegrees.roundToInt()}°",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = status,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.aligned) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun statusCopy(state: QiblaUiState): String = when {
    state.atKaaba -> "Vous êtes à La Mecque"
    !state.sensorAvailable -> "Boussole indisponible sur cet appareil"
    state.aligned -> "Aligné vers la Kaaba"
    else -> "Tournez le téléphone"
}

@Composable
private fun QiblaHeader(
    subtitle: String,
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
                    icon = AppIcons.Qibla,
                    contentDescription = null,
                    size = IconSize.xl,
                    tint = onHero
                )
            }
            Text(
                text = "Qibla",
                style = MaterialTheme.typography.displaySmall,
                color = onHero
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = onHero.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun CompassDisc(
    needleDegrees: Float,
    aligned: Boolean,
    showNeedle: Boolean,
    modifier: Modifier = Modifier
) {
    val brand = MaterialTheme.brand
    val outline by animateColorAsState(
        targetValue = if (aligned) brand.gold else MaterialTheme.colorScheme.outline,
        animationSpec = Motion.springSnappy(),
        label = "qiblaOutline"
    )
    val wash by animateColorAsState(
        targetValue = if (aligned) brand.goldWash else brand.greenWash,
        animationSpec = Motion.springSnappy(),
        label = "qiblaWash"
    )
    val needleColor by animateColorAsState(
        targetValue = if (aligned) brand.gold else MaterialTheme.colorScheme.primary,
        animationSpec = Motion.springSnappy(),
        label = "qiblaNeedle"
    )
    val tickColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
    val notchColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .size(260.dp)
            .clip(CircleShape)
            .background(wash)
            .border(2.dp, outline, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f)
            val inner = radius - 18.dp.toPx()
            for (i in 0 until 36) {
                val angle = Math.toRadians((i * 10).toDouble() - 90.0)
                val major = i % 9 == 0
                val startR = if (major) inner - 10.dp.toPx() else inner - 5.dp.toPx()
                val stroke = if (major) 2.5.dp.toPx() else 1.dp.toPx()
                drawLine(
                    color = tickColor,
                    start = Offset(
                        center.x + (kotlin.math.cos(angle) * startR).toFloat(),
                        center.y + (kotlin.math.sin(angle) * startR).toFloat()
                    ),
                    end = Offset(
                        center.x + (kotlin.math.cos(angle) * inner).toFloat(),
                        center.y + (kotlin.math.sin(angle) * inner).toFloat()
                    ),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }
            val notchY = 14.dp.toPx()
            drawLine(
                color = notchColor,
                start = Offset(center.x, notchY),
                end = Offset(center.x, notchY + 14.dp.toPx()),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawCircle(
                color = outline.copy(alpha = 0.35f),
                radius = radius - 8.dp.toPx(),
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        if (showNeedle) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(needleDegrees)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val tip = Offset(center.x, center.y - size.minDimension * 0.32f)
                val left = Offset(center.x - 12.dp.toPx(), center.y + 8.dp.toPx())
                val right = Offset(center.x + 12.dp.toPx(), center.y + 8.dp.toPx())
                val tail = Offset(center.x, center.y + size.minDimension * 0.22f)
                val path = Path().apply {
                    moveTo(tip.x, tip.y)
                    lineTo(left.x, left.y)
                    lineTo(center.x, center.y)
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(path, needleColor)
                drawLine(
                    color = needleColor.copy(alpha = 0.45f),
                    start = center,
                    end = tail,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawCircle(color = needleColor, radius = 6.dp.toPx(), center = center)
            }
        }
    }
}

@Preview(name = "Qibla clair", heightDp = 800)
@Composable
private fun QiblaLightPreview() {
    MySalatTheme(darkTheme = false) {
        QiblaScreen(
            state = QiblaUiState(
                cityName = "Dakar",
                qiblaDegrees = 58f,
                headingDegrees = 40f,
                needleDegrees = 18f,
                sensorAvailable = true,
                aligned = false
            ),
            onStart = {},
            onStop = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = Spacing.md)
        )
    }
}

@Preview(name = "Qibla sombre aligné", heightDp = 800)
@Composable
private fun QiblaDarkPreview() {
    MySalatTheme(darkTheme = true) {
        QiblaScreen(
            state = QiblaUiState(
                cityName = "Paris",
                qiblaDegrees = 119f,
                headingDegrees = 119f,
                needleDegrees = 0f,
                sensorAvailable = true,
                aligned = true
            ),
            onStart = {},
            onStop = {},
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(vertical = Spacing.md)
        )
    }
}
