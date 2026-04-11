package com.radiogolha.mobile.ui.musicians

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.FeaturedPersonCardUiModel
import com.radiogolha.mobile.ui.people.PeopleCarouselSection
import com.radiogolha.mobile.ui.people.PeopleBrowseScreen
import com.radiogolha.mobile.ui.people.PeopleListRow
import com.radiogolha.mobile.ui.people.compareByPersianText
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.ui.people.PeopleHeader
import com.radiogolha.mobile.ui.home.BottomNavigationWithMiniPlayer
import com.radiogolha.mobile.ui.people.PeopleBrowseContent
import com.radiogolha.mobile.ui.people.comparePersianTexts
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.material3.Scaffold

@Composable
fun MusiciansScreen(
    musicians: List<MusicianListItemUiModel>,
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
                Column(modifier = Modifier.fillMaxSize().padding(bottom = innerPadding.calculateBottomPadding())) {
                    PeopleHeader(
                        title = "نوازندگان",
                        onBackClick = onBackClick,
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                            .padding(top = GolhaSpacing.StatusBarTopGap, bottom = 12.dp)
                    )
                    MusiciansContent(musicians = musicians)
                }
            }
        }
    }
}

@Composable
fun MusiciansContent(musicians: List<MusicianListItemUiModel>) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredBySearch = remember(musicians, searchQuery) {
        if (searchQuery.isEmpty()) musicians
        else musicians.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.instrument.contains(searchQuery, ignoreCase = true)
        }
    }

    val instruments = remember(filteredBySearch) {
        filteredBySearch.map { it.instrument }.distinct().sortedWith(compareByPersianText())
    }

    var selectedInstrument by remember(instruments) {
        mutableStateOf(instruments.firstOrNull() ?: "")
    }

    val filteredMusicians = remember(filteredBySearch, selectedInstrument) {
        filteredBySearch.filter { it.instrument == selectedInstrument }
            .sortedWith { a, b -> comparePersianTexts(a.name, b.name) }
    }

    PeopleBrowseContent(
        tint = GolhaColors.SoftRose,
        topSectionContent = {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                MusicianSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )

                if (searchQuery.isEmpty()) {
                    PeopleCarouselSection(
                        title = "برترین‌ها",
                        tint = GolhaColors.SoftRose,
                        items = musicians.take(10).map { musician ->
                            FeaturedPersonCardUiModel(
                                name = musician.name,
                                imageUrl = musician.imageUrl,
                                metaTop = musician.instrument,
                                metaBottom = "",
                            )
                        },
                    )
                }
            }
        },
        customContent = {
            InstrumentsTabbedList(
                instruments = instruments,
                selectedInstrument = selectedInstrument,
                onInstrumentSelected = { selectedInstrument = it },
                musicians = filteredMusicians.map { musician ->
                    BrowsePersonRowUiModel(
                        name = musician.name,
                        imageUrl = musician.imageUrl,
                        primaryMeta = "",
                        secondaryMeta = "${musician.programCount} برنامه",
                        groupLabel = "",
                    )
                },
                tint = GolhaColors.SoftRose
            )
        }
    )
}

@Composable
private fun MusicianSearchBar(
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
                        text = "جستجوی نوازنده یا ساز...",
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

@Composable
private fun InstrumentsTabbedList(
    instruments: List<String>,
    selectedInstrument: String,
    onInstrumentSelected: (String) -> Unit,
    musicians: List<BrowsePersonRowUiModel>,
    tint: androidx.compose.ui.graphics.Color,
) {
    val selectedTabIndex = instruments.indexOf(selectedInstrument).coerceAtLeast(0)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        if (instruments.isNotEmpty()) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.Transparent,
                contentColor = GolhaColors.PrimaryAccent,
                edgePadding = GolhaSpacing.ScreenHorizontal,
                divider = {},
                indicator = { tabPositions ->
                    if (selectedTabIndex < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = GolhaColors.PrimaryAccent
                        )
                    }
                }
            ) {
                instruments.forEach { instrument ->
                    val isSelected = instrument == selectedInstrument
                    Tab(
                        selected = isSelected,
                        onClick = { onInstrumentSelected(instrument) },
                        text = {
                            Text(
                                text = instrument,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) GolhaColors.PrimaryAccent else GolhaColors.SecondaryText,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    )
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = GolhaColors.Border.copy(alpha = 0.5f)
        )

        // Using a regular Column here because it's inside an 'item' of a LazyColumn in PeopleBrowseScreen.
        // For very large lists this might have performance issues, but for a filtered instrument list it should be fine.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = GolhaSpacing.ScreenHorizontal)
        ) {
            musicians.forEachIndexed { index, person ->
                PeopleListRow(item = person, tint = tint)
                if (index < musicians.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = GolhaColors.Border.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}
