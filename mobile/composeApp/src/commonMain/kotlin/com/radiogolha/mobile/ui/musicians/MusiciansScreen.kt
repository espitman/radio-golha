package com.radiogolha.mobile.ui.musicians

import androidx.compose.runtime.Composable
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.FeaturedPersonCardUiModel
import com.radiogolha.mobile.ui.people.PeopleBrowseScreen
import com.radiogolha.mobile.ui.people.browseGroupLabel

@Composable
fun MusiciansScreen(
    musicians: List<MusicianListItemUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
) {
    PeopleBrowseScreen(
        title = "همه نوازندگان",
        featuredTitle = "نوازندگان برجسته",
        countLabel = "${musicians.size} نوازنده",
        tint = GolhaColors.SoftRose,
        featuredPeople = musicians.take(8).map { musician ->
            FeaturedPersonCardUiModel(
                name = musician.name,
                imageUrl = musician.imageUrl,
                metaTop = musician.instrument,
                metaBottom = "${musician.programCount} برنامه",
            )
        },
        people = musicians.map { musician ->
            BrowsePersonRowUiModel(
                name = musician.name,
                imageUrl = musician.imageUrl,
                primaryMeta = musician.instrument,
                secondaryMeta = "${musician.programCount} برنامه",
                groupLabel = browseGroupLabel(musician.name),
            )
        },
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        onBackClick = onBackClick,
    )
}
