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
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.FeaturedPersonCardUiModel
import com.radiogolha.mobile.ui.people.PeopleCarouselSection
import com.radiogolha.mobile.ui.people.PeopleBrowseScreen
import com.radiogolha.mobile.ui.people.PeopleListRow
import com.radiogolha.mobile.ui.people.compareByPersianText
import com.radiogolha.mobile.ui.people.comparePersianTexts

@Composable
fun MusiciansScreen(
    musicians: List<MusicianListItemUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
) {
    val instruments = remember(musicians) {
        musicians.map { it.instrument }.distinct().sortedWith(compareByPersianText())
    }
    var selectedInstrument by remember(instruments) { 
        mutableStateOf(instruments.firstOrNull() ?: "") 
    }

    val filteredMusicians = remember(musicians, selectedInstrument) {
        musicians.filter { it.instrument == selectedInstrument }
            .sortedWith { a, b -> comparePersianTexts(a.name, b.name) }
    }

    PeopleBrowseScreen(
        title = "نوازندگان",
        countLabel = "",
        tint = GolhaColors.SoftRose,
        topSectionContent = {
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
        },
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        onBackClick = onBackClick,
    )
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
