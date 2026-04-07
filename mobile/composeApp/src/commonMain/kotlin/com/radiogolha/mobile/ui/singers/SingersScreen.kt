package com.radiogolha.mobile.ui.singers

import androidx.compose.runtime.Composable
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.people.BrowsePersonRowUiModel
import com.radiogolha.mobile.ui.people.FeaturedPersonCardUiModel
import com.radiogolha.mobile.ui.people.PeopleBrowseScreen
import com.radiogolha.mobile.ui.people.browseGroupLabel

@Composable
fun SingersScreen(
    singers: List<SingerListItemUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
) {
    PeopleBrowseScreen(
        title = "همه خواننده‌ها",
        featuredTitle = "خواننده‌های برجسته",
        countLabel = "${singers.size} خواننده",
        tint = GolhaColors.SoftBlue,
        featuredPeople = singers.take(8).map { singer ->
            FeaturedPersonCardUiModel(
                name = singer.name,
                imageUrl = singer.imageUrl,
                metaTop = "${singer.programCount} برنامه",
            )
        },
        people = singers.map { singer ->
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
    )
}
