package com.example.mysalat.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mysalat.PrayerViewModel
import com.example.mysalat.QiblaViewModel
import com.example.mysalat.ui.history.HistoryScreen
import com.example.mysalat.ui.home.HomeScreen
import com.example.mysalat.ui.icons.AppIcons
import com.example.mysalat.ui.placeholder.ComingSoonScreen
import com.example.mysalat.ui.profile.ProfileScreen
import com.example.mysalat.ui.qibla.QiblaScreen
import com.example.mysalat.ui.theme.Motion
import com.example.mysalat.ui.theme.Spacing

/** Height reserved for the floating bar (icons + labels) so content clears it. */
private val BottomBarHeight = 96.dp

/**
 * App shell: holds [PrayerViewModel] (and [QiblaViewModel] on the Qibla tab),
 * swaps destinations with a directional slide, and floats the tab bar.
 */
@Composable
fun AppScaffold(
    modifier: Modifier = Modifier,
    onExit: () -> Unit = {}
) {
    val viewModel: PrayerViewModel = viewModel()

    val destination by viewModel.destination.collectAsStateWithLifecycle()
    val homeState by viewModel.homeState.collectAsStateWithLifecycle()
    val summary by viewModel.statsSummary.collectAsStateWithLifecycle()
    val history by viewModel.displayHistory.collectAsStateWithLifecycle()
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val city by viewModel.city.collectAsStateWithLifecycle()

    BackHandler(enabled = true) {
        if (!viewModel.navigateBack()) onExit()
    }

    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navigationBarPadding = WindowInsets.navigationBars.asPaddingValues()

    // Bottom only: top status-bar inset stays on the shell so it never scrolls away.
    val contentPadding = PaddingValues(
        bottom = navigationBarPadding.calculateBottomPadding() + BottomBarHeight + Spacing.xl
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        AnimatedContent(
            targetState = destination,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding.calculateTopPadding() + Spacing.sm),
            transitionSpec = {
                // Slide in the direction of travel along the tab strip.
                val forward = targetState.ordinal > initialState.ordinal
                val offset = if (forward) 1 else -1

                (slideInHorizontally(Motion.springSoft()) { width -> offset * width / 6 } +
                    fadeIn(Motion.fadeMedium()))
                    .togetherWith(
                        slideOutHorizontally(Motion.springSoft()) { width -> -offset * width / 6 } +
                            fadeOut(Motion.fadeFast())
                    )
            },
            label = "destination"
        ) { current ->
            when (current) {
                AppDestination.Home -> HomeScreen(
                    state = homeState,
                    onPrayerToggled = viewModel::onPrayerToggled,
                    onOpenProfile = { viewModel.navigateTo(AppDestination.Profile) },
                    onQuickAction = { action ->
                        // Only shortcuts that map to a real tab navigate for now.
                        when (action.label) {
                            "Qibla" -> viewModel.navigateTo(AppDestination.Qibla)
                            "Coran" -> viewModel.navigateTo(AppDestination.Quran)
                            else -> Unit
                        }
                    },
                    contentPadding = contentPadding
                )

                AppDestination.Quran -> ComingSoonScreen(
                    title = "Coran",
                    subtitle = "Lisez, écoutez et mémorisez, sourate par sourate.",
                    icon = AppIcons.Quran,
                    highlights = listOf(
                        "Texte arabe et traduction française",
                        "Reprise à votre dernière lecture",
                        "Récitations audio hors connexion"
                    ),
                    contentPadding = contentPadding
                )

                AppDestination.Qibla -> {
                    val qiblaViewModel: QiblaViewModel = viewModel()
                    val qiblaState by qiblaViewModel.uiState.collectAsStateWithLifecycle()
                    QiblaScreen(
                        state = qiblaState,
                        onStart = qiblaViewModel::start,
                        onStop = qiblaViewModel::stop,
                        contentPadding = contentPadding
                    )
                }

                AppDestination.History -> HistoryScreen(
                    summary = summary,
                    history = history,
                    contentPadding = contentPadding
                )

                AppDestination.Profile -> ProfileScreen(
                    userName = userName,
                    city = city,
                    summary = summary,
                    onUserNameChanged = viewModel::onUserNameChanged,
                    onCityChanged = viewModel::onCityChanged,
                    contentPadding = contentPadding
                )
            }
        }

        BottomBarScrim(
            height = navigationBarPadding.calculateBottomPadding() + BottomBarHeight + Spacing.xl,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        FloatingBottomBar(
            current = destination,
            onSelect = viewModel::navigateTo,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(
                    start = Spacing.md,
                    end = Spacing.md,
                    bottom = navigationBarPadding.calculateBottomPadding() + Spacing.sm
                )
        )
    }
}

/**
 * Soft opaque fade so scrolling content does not read through the floating bar.
 */
@Composable
private fun BottomBarScrim(
    height: Dp,
    modifier: Modifier = Modifier
) {
    val background = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.verticalGradient(
                    listOf(
                        background.copy(alpha = 0.4f),
                        background.copy(alpha = 0.95f),
                        background
                    )
                )
            )
    )
}
