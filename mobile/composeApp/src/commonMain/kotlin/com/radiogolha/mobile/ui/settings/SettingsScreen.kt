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
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
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
    favoriteMusicians: List<MusicianListItemUiModel> = emptyList(),
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
    mostPlayedTracks: List<TrackUiModel> = emptyList(),
    recentlyPlayedTracks: List<TrackUiModel> = emptyList(),
    onTrackClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
    onTrackLongClick: (TrackUiModel) -> Unit = {},
) {
    var aboutTapCount by rememberSaveable { mutableIntStateOf(0) }
    var isDebugToolsVisible by rememberSaveable { mutableStateOf(false) }
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("علاقه‌مندی‌ها", "تاریخچه", "درباره")

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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                ) {
                    // Title
                    Text(
                        text = "حساب من",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GolhaColors.PrimaryText,
                        modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 20.dp)
                    )

                    // Tab Selector
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                            .padding(bottom = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = GolhaColors.Surface.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(4.dp)) {
                            tabs.forEachIndexed { index, title ->
                                val selected = selectedTabIndex == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GolhaColors.PrimaryAccent else Color.Transparent)
                                        .clickable { selectedTabIndex = index },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
                                        color = if (selected) Color.White else GolhaColors.SecondaryText
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = GolhaSpacing.ScreenHorizontal,
                            end = GolhaSpacing.ScreenHorizontal,
                            bottom = innerPadding.calculateBottomPadding() + 22.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(18.dp),
                    ) {
                        when (selectedTabIndex) {
                            0 -> { // Favorites
                                if (favoriteSingers.isEmpty() && favoriteMusicians.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "هنوز هنرمندی را به علاقه‌مندی‌ها اضافه نکرده‌اید.")
                                    }
                                }
                                
                                if (favoriteSingers.isNotEmpty()) {
                                    item {
                                        Text(text = "خوانندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    }
                                    items(favoriteSingers) { singer ->
                                        ArtistFavoriteItem(singer, onArtistClick)
                                    }
                                }

                                if (favoriteMusicians.isNotEmpty()) {
                                    item {
                                        Text(text = "نوازندگان مورد علاقه", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    }
                                    items(favoriteMusicians) { musician ->
                                        MusicianFavoriteItem(musician, onArtistClick)
                                    }
                                }
                            }
                            1 -> { // History
                                if (recentlyPlayedTracks.isEmpty() && mostPlayedTracks.isEmpty()) {
                                    item {
                                        EmptyStatePlaceholder(text = "تاریخچه پخش شما خالی است.")
                                    }
                                }

                                if (recentlyPlayedTracks.isNotEmpty()) {
                                    item {
                                        Text(text = "اخیراً شنیده شده", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    }
                                    items(recentlyPlayedTracks) { track ->
                                        com.radiogolha.mobile.ui.programs.ProgramTrackRow(
                                            track = track,
                                            onTrackClick = { onTrackClick(track.id) },
                                            onPlayClick = { onPlayTrack(track) },
                                            onLongClick = { onTrackLongClick(track) },
                                            onArtistClick = { id -> onArtistClick(id) },
                                            isActive = currentTrack?.id == track.id,
                                            isPlaying = currentTrack?.id == track.id && isPlayerPlaying,
                                        )
                                    }
                                }

                                if (mostPlayedTracks.isNotEmpty()) {
                                    item {
                                        Text(text = "محبوب‌ترین‌های شما", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                                    }
                                    items(mostPlayedTracks) { track ->
                                        com.radiogolha.mobile.ui.programs.ProgramTrackRow(
                                            track = track,
                                            onTrackClick = { onTrackClick(track.id) },
                                            onPlayClick = { onPlayTrack(track) },
                                            onLongClick = { onTrackLongClick(track) },
                                            onArtistClick = { id -> onArtistClick(id) },
                                            isActive = currentTrack?.id == track.id,
                                            isPlaying = currentTrack?.id == track.id && isPlayerPlaying,
                                        )
                                    }
                                }
                            }
                            2 -> { // About
                                item { AboutAppSection(onTap = {
                                    aboutTapCount++
                                    if (aboutTapCount == 2) {
                                        com.radiogolha.mobile.debug.showDebugToast("یک کلیک دیگر تا فعال‌سازی حالت توسعه‌دهنده")
                                    } else if (aboutTapCount >= 3) {
                                        isDebugToolsVisible = true
                                        onOpenDebug()
                                    }
                                }) }
                                
                                item {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = isDebugToolsVisible && isDebugDatabaseToolsEnabled,
                                        enter = androidx.compose.animation.expandVertically() + androidx.compose.animation.fadeIn(),
                                        exit = androidx.compose.animation.shrinkVertically() + androidx.compose.animation.fadeOut()
                                    ) {
                                        Surface(
                                            modifier = Modifier.padding(top = 8.dp),
                                            shape = RoundedCornerShape(GolhaRadius.Card),
                                            color = GolhaColors.Surface,
                                            tonalElevation = 0.dp,
                                            shadowElevation = GolhaElevation.Card,
                                            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth().padding(18.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                            ) {
                                                Text(text = "ابزار توسعه", style = MaterialTheme.typography.titleLarge, color = GolhaColors.PrimaryText)
                                                SmallPrimaryButton(
                                                    label = "دریافت دیتابیس جدید",
                                                    enabled = !isImportingDatabase,
                                                    loading = isImportingDatabase,
                                                    onClick = onImportDebugDatabase,
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(text: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GolhaLineIcon(icon = GolhaIcon.Note, modifier = Modifier.size(64.dp), tint = GolhaColors.SecondaryText.copy(alpha = 0.3f))
            Text(text, style = MaterialTheme.typography.bodyMedium, color = GolhaColors.SecondaryText, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun ArtistFavoriteItem(singer: SingerListItemUiModel, onClick: (Long) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(singer.artistId) },
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

@Composable
private fun MusicianFavoriteItem(musician: MusicianListItemUiModel, onClick: (Long) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick(musician.artistId) },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ArtistAvatar(name = musician.name, imageUrl = musician.imageUrl, tint = GolhaColors.SoftRose, modifier = Modifier.size(48.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(musician.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                Text(musician.instrument ?: "نوازنده", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
            }
            GolhaLineIcon(icon = GolhaIcon.FavoritesFilled, modifier = Modifier.size(18.dp), tint = GolhaColors.PrimaryAccent)
        }
    }
}

@Composable
private fun AboutAppSection(onTap: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onTap() },
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
                AboutMetaChip(label = "آرشیو شنیداری")
            }
        }
    }
}

@Composable
private fun AboutMetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = com.radiogolha.mobile.theme.GolhaColors.SoftBlue.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, com.radiogolha.mobile.theme.GolhaColors.Border.copy(alpha = 0.4f)),
    ) {
        Text(
            text = label,
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = com.radiogolha.mobile.theme.GolhaColors.PrimaryText,
        )
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
