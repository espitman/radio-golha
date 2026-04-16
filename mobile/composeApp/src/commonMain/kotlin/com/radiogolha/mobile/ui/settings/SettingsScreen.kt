package com.radiogolha.mobile.ui.settings

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationWithMiniPlayer
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.home.SmallPrimaryButton
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.home.ArtistAvatar
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    favoriteSingers: List<SingerListItemUiModel> = emptyList(),
    onArtistClick: (Long) -> Unit = {},
    onShowAllFavorites: () -> Unit = {},
    onOpenDebug: () -> Unit = {},
    isDebugDatabaseToolsEnabled: Boolean,
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    var aboutTapCount by rememberSaveable { mutableIntStateOf(0) }
    var isDebugToolsVisible by rememberSaveable { mutableStateOf(false) }

    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = GolhaColors.ScreenBackground.copy(alpha = 0f),
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
                    onExpand = onExpandPlayer,
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
                        text = "حساب من",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GolhaColors.PrimaryText,
                    )
                }
                // Favorite Artists
                if (favoriteSingers.isNotEmpty()) {
                    item {
                        Text(
                            text = "هنرمندان مورد علاقه",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = GolhaColors.PrimaryText,
                        )
                    }
                    val displayLimit = 5
                    val displayList = favoriteSingers.take(displayLimit)
                    items(displayList) { singer ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().clickable { onArtistClick(singer.artistId) },
                            shape = RoundedCornerShape(GolhaRadius.Card),
                            color = GolhaColors.Surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                ArtistAvatar(name = singer.name, imageUrl = singer.imageUrl, tint = GolhaColors.SoftBlue, modifier = Modifier.size(48.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(singer.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    Text("${singer.programCount} برنامه", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
                                }
                                GolhaLineIcon(icon = GolhaIcon.FavoritesFilled, modifier = Modifier.size(18.dp), tint = GolhaColors.PrimaryAccent)
                            }
                        }
                    }
                    if (favoriteSingers.size > displayLimit) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable { onShowAllFavorites() },
                                shape = RoundedCornerShape(GolhaRadius.Card),
                                color = GolhaColors.BadgeBackground,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                    Text("مشاهده همه (${favoriteSingers.size})", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                }
                            }
                        }
                    }
                }

                // About app
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            aboutTapCount += 1
                            if (aboutTapCount >= 3) {
                                aboutTapCount = 0
                                isDebugToolsVisible = true
                                onOpenDebug()
                            }
                        },
                        shape = RoundedCornerShape(GolhaRadius.Card),
                        color = GolhaColors.Surface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
                        shadowElevation = GolhaElevation.Card,
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(GolhaColors.BadgeBackground),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    GolhaLineIcon(
                                        icon = GolhaIcon.Account,
                                        modifier = Modifier.size(24.dp),
                                        tint = GolhaColors.PrimaryText,
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Text(
                                        "درباره ما",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GolhaColors.PrimaryText,
                                    )
                                    Text(
                                        "آرشیو شنیداری برنامه‌های ماندگار گل‌ها",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GolhaColors.SecondaryText,
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = GolhaColors.SoftSand.copy(alpha = 0.42f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.35f)),
                            ) {
                                Text(
                                    text = "رادیو گل‌ها مجموعه‌ای از برنامه‌های گل‌های رنگارنگ رادیو ایران را با تجربه‌ای ساده، مرتب و امروزی کنار هم آورده است.",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = GolhaColors.PrimaryText.copy(alpha = 0.88f),
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                AboutMetaChip(label = "نسخه ۰.۱.۰")
                                AboutMetaChip(label = "آرشیو گل‌ها")
                            }
                        }
                    }
                }

                if (isDebugToolsVisible && isDebugDatabaseToolsEnabled) {
                    item {
                        DebugDatabaseCard(
                            isImportingDatabase = isImportingDatabase,
                            onImportDebugDatabase = onImportDebugDatabase,
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun DebugDatabaseCard(
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "ابزار توسعه",
                style = MaterialTheme.typography.titleLarge,
                color = GolhaColors.PrimaryText,
            )

            SmallPrimaryButton(
                label = "دریافت دیتابیس جدید",
                enabled = !isImportingDatabase,
                loading = isImportingDatabase,
                onClick = onImportDebugDatabase,
            )
        }
    }
}

@Composable
private fun AboutMetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = GolhaColors.SoftBlue.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.4f)),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = GolhaColors.PrimaryText,
        )
    }
}
