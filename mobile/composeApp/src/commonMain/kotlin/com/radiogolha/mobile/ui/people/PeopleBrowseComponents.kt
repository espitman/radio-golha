package com.radiogolha.mobile.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.ArtistAvatar
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationBar

data class FeaturedPersonCardUiModel(
    val name: String,
    val imageUrl: String? = null,
    val metaTop: String? = null,
    val metaBottom: String? = null,
)

data class BrowsePersonRowUiModel(
    val name: String,
    val imageUrl: String? = null,
    val primaryMeta: String,
    val secondaryMeta: String? = null,
    val groupLabel: String,
)

@Composable
fun PeopleBrowseScreen(
    title: String,
    featuredTitle: String,
    countLabel: String,
    tint: androidx.compose.ui.graphics.Color,
    featuredPeople: List<FeaturedPersonCardUiModel>,
    people: List<BrowsePersonRowUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GolhaColors.ScreenBackground),
            containerColor = GolhaColors.ScreenBackground,
            bottomBar = {
                BottomNavigationBar(
                    items = bottomNavItems,
                    onItemSelected = onBottomNavSelected,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = GolhaSpacing.ScreenHorizontal,
                    end = GolhaSpacing.ScreenHorizontal,
                    top = 22.dp,
                    bottom = innerPadding.calculateBottomPadding() + 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    PeopleHeader(
                        title = title,
                        onBackClick = onBackClick,
                    )
                }

                if (featuredPeople.isNotEmpty()) {
                    item {
                        FeaturedPeopleSection(
                            title = featuredTitle,
                            tint = tint,
                            items = featuredPeople,
                        )
                    }
                }

                item {
                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )
                }

                item {
                    PeopleListCard(
                        people = people,
                        tint = tint,
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleHeader(
    title: String,
    onBackClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
        )

        Surface(
            modifier = Modifier.clickable { onBackClick() },
            shape = CircleShape,
            color = GolhaColors.Surface.copy(alpha = 0.92f),
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.75f)),
        ) {
            Box(
                modifier = Modifier.size(42.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "‹",
                    style = MaterialTheme.typography.headlineSmall,
                    color = GolhaColors.SecondaryText,
                )
            }
        }
    }
}

@Composable
private fun FeaturedPeopleSection(
    title: String,
    tint: androidx.compose.ui.graphics.Color,
    items: List<FeaturedPersonCardUiModel>,
) {
    val pages = items.chunked(3)
    val pagerState = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = GolhaColors.Surface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.72f)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
            ) { page ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    pages[page].forEach { person ->
                        FeaturedPersonCard(
                            item = person,
                            tint = tint,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    repeat(3 - pages[page].size) {
                        SpacerCard(modifier = Modifier.weight(1f))
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(if (index == pagerState.currentPage) 20.dp else 8.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (index == pagerState.currentPage) {
                                    GolhaColors.SecondaryText.copy(alpha = 0.72f)
                                } else {
                                    GolhaColors.Border
                                }
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedPersonCard(
    item: FeaturedPersonCardUiModel,
    tint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ArtistAvatar(
            name = item.name,
            imageUrl = item.imageUrl,
            tint = tint,
            modifier = Modifier.size(92.dp),
        )

        Text(
            text = item.name,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = GolhaColors.PrimaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        if (item.metaTop != null) {
            Text(
                text = item.metaTop,
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
        if (item.metaBottom != null) {
            Text(
                text = item.metaBottom,
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SpacerCard(modifier: Modifier = Modifier) {
    Box(modifier = modifier)
}

@Composable
private fun PeopleListCard(
    people: List<BrowsePersonRowUiModel>,
    tint: androidx.compose.ui.graphics.Color,
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = GolhaColors.Surface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.78f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .padding(start = 22.dp, top = 16.dp)
                    .width(3.dp)
                    .height(28.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(GolhaColors.SecondaryText.copy(alpha = 0.85f)),
            )

            people.forEachIndexed { index, person ->
                PeopleListRow(
                    item = person,
                    tint = tint,
                )
                if (index != people.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = GolhaColors.Border.copy(alpha = 0.72f),
                    )
                }
            }
        }
    }
}

@Composable
private fun PeopleListRow(
    item: BrowsePersonRowUiModel,
    tint: androidx.compose.ui.graphics.Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ArtistAvatar(
            name = item.name,
            imageUrl = item.imageUrl,
            tint = tint,
            modifier = Modifier.size(82.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.primaryMeta,
                style = MaterialTheme.typography.titleMedium,
                color = GolhaColors.SecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.secondaryMeta != null) {
                Text(
                    text = item.secondaryMeta,
                    style = MaterialTheme.typography.bodySmall,
                    color = GolhaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.groupLabel,
                style = MaterialTheme.typography.bodyLarge,
                color = GolhaColors.SecondaryText.copy(alpha = 0.95f),
            )
            Text(
                text = "‹",
                style = MaterialTheme.typography.headlineSmall,
                color = GolhaColors.SecondaryText.copy(alpha = 0.8f),
            )
        }
    }
}

fun browseGroupLabel(name: String): String =
    name.trim()
        .firstOrNull()
        ?.toString()
        ?: "•"
