package com.radiogolha.mobile.ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationWithMiniPlayer
import com.radiogolha.mobile.ui.home.TrackUiModel

@Composable
fun TabRootScreen(
    title: String,
    subtitle: String,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                bottomBar = {
                    BottomNavigationWithMiniPlayer(
                        items = bottomNavItems,
                        onItemSelected = onBottomNavSelected,
                        currentTrack = currentTrack,
                        isPlaying = isPlayerPlaying,
                        isLoading = isPlayerLoading,
                        currentPositionMs = currentPlaybackPositionMs,
                        durationMs = currentPlaybackDurationMs,
                        onTogglePlayback = onTogglePlayerPlayback,
                    )
                },
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = GolhaSpacing.ScreenHorizontal,
                        end = GolhaSpacing.ScreenHorizontal,
                        top = GolhaSpacing.StatusBarTopGap,
                        bottom = innerPadding.calculateBottomPadding() + 22.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    item {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = GolhaColors.PrimaryText,
                        )
                    }
                    item {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = GolhaColors.SecondaryText,
                        )
                    }
                    item {
                        Surface(
                            shape = RoundedCornerShape(GolhaRadius.Card),
                            color = GolhaColors.Surface,
                            tonalElevation = 0.dp,
                            shadowElevation = GolhaElevation.Card,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 18.dp, vertical = 32.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "این بخش در مرحله‌ی بعد کامل می‌شود.",
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                    color = GolhaColors.PrimaryText,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
