package com.radiogolha.mobile.ui.singers

import androidx.compose.runtime.Composable
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.FeaturedPersonCardUiModel
import com.radiogolha.mobile.ui.people.PeopleBrowseScreen
import com.radiogolha.mobile.ui.people.PeopleCarouselSection
import com.radiogolha.mobile.ui.people.browseGroupLabel
import com.radiogolha.mobile.ui.people.comparePersianTexts
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.theme.GolhaSpacing
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SingersScreen(
    singers: List<SingerListItemUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredSingers = remember(singers, searchQuery) {
        val filtered = if (searchQuery.isEmpty()) singers
        else singers.filter { it.name.contains(searchQuery, ignoreCase = true) }
        
        filtered.sortedWith { a, b -> comparePersianTexts(a.name, b.name) }
    }

    PeopleBrowseScreen(
        title = "خواننده‌ها",
        countLabel = "",
        tint = GolhaColors.SoftBlue,
        topSectionContent = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SingerSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                if (searchQuery.isEmpty()) {
                    PeopleCarouselSection(
                        title = "خواننده‌های برجسته",
                        tint = GolhaColors.SoftBlue,
                        items = singers.take(10).map { singer ->
                            FeaturedPersonCardUiModel(
                                name = singer.name,
                                imageUrl = singer.imageUrl,
                                metaTop = null,
                                metaBottom = "",
                            )
                        },
                    )
                }
            }
        },
        people = filteredSingers.map { singer ->
            BrowsePersonRowUiModel(
                name = singer.name,
                imageUrl = singer.imageUrl,
                primaryMeta = "${singer.programCount} برنامه",
                groupLabel = browseGroupLabel(singer.name),
            )
        },
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        onBackClick = onBackClick,
        currentTrack = currentTrack,
        isPlayerPlaying = isPlayerPlaying,
        isPlayerLoading = isPlayerLoading,
        currentPlaybackPositionMs = currentPlaybackPositionMs,
        currentPlaybackDurationMs = currentPlaybackDurationMs,
        onTogglePlayerPlayback = onTogglePlayerPlayback,
    )
}

@Composable
private fun SingerSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = GolhaSpacing.ScreenHorizontal),
        shape = RoundedCornerShape(16.dp),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GolhaLineIcon(
                icon = GolhaIcon.Search,
                modifier = Modifier.size(20.dp),
                tint = GolhaColors.SecondaryText
            )

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "جستجوی خواننده...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GolhaColors.SecondaryText.copy(alpha = 0.6f)
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = GolhaColors.PrimaryText,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                    ),
                    cursorBrush = SolidColor(GolhaColors.PrimaryAccent),
                    singleLine = true,
                )
            }
        }
    }
}
