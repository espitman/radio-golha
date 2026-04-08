package com.radiogolha.mobile.ui.people

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.ArtistAvatar
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationBar
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PeopleListCard(
    people: List<BrowsePersonRowUiModel>,
    tint: androidx.compose.ui.graphics.Color,
) {
    val groupedPeople = rememberGroupedPeople(people)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val headerIndexes = remember(groupedPeople) {
        buildMap {
            var index = 0
            groupedPeople.forEach { group ->
                put(group.label, index)
                index += 1 + group.items.size
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = GolhaColors.Surface,
        shadowElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.78f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(640.dp),
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 42.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
            ) {
                groupedPeople.forEachIndexed { groupIndex, group ->
                    stickyHeader(key = "header-${group.label}") {
                        AlphabetGroupHeader(label = group.label)
                    }

                    itemsIndexed(
                        items = group.items,
                        key = { index, person -> "${group.label}-${person.name}-${index}" },
                    ) { index, person ->
                        PeopleListRow(item = person, tint = tint)
                        val isLastInGroup = index == group.items.lastIndex
                        val isLastGroup = groupIndex == groupedPeople.lastIndex
                        if (!(isLastInGroup && isLastGroup)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 20.dp),
                                color = GolhaColors.Border.copy(alpha = 0.72f),
                            )
                        }
                    }
                }
            }

            AlphabetJumpRail(
                labels = groupedPeople.map { it.label },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp),
                onJump = { label ->
                    scope.launch {
                        headerIndexes[label]?.let { target ->
                            listState.animateScrollToItem(target)
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun AlphabetGroupHeader(
    label: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.SecondaryText,
        )
    }
}

@Composable
private fun AlphabetJumpRail(
    labels: List<String>,
    onJump: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (labels.isEmpty()) return

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = GolhaColors.ScreenBackground.copy(alpha = 0.9f),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            labels.forEach { label ->
                Text(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onJump(label) }
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                    ),
                    color = GolhaColors.SecondaryText,
                )
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
                text = "‹",
                style = MaterialTheme.typography.headlineSmall,
                color = GolhaColors.SecondaryText.copy(alpha = 0.8f),
            )
        }
    }
}

fun browseGroupLabel(name: String): String =
    firstPersianLetter(name) ?: "•"

private data class PeopleGroupUiModel(
    val label: String,
    val items: List<BrowsePersonRowUiModel>,
)

@Composable
private fun rememberGroupedPeople(people: List<BrowsePersonRowUiModel>): List<PeopleGroupUiModel> {
    return androidx.compose.runtime.remember(people) {
        people
            .sortedWith(compareByPersianName())
            .groupBy { browseGroupLabel(it.name) }
            .entries
            .sortedBy { persianAlphabetIndex(it.key) }
            .map { (label, items) ->
                PeopleGroupUiModel(label = label, items = items)
            }
    }
}

private val persianAlphabet = listOf(
    "ا", "ب", "پ", "ت", "ث", "ج", "چ", "ح", "خ",
    "د", "ذ", "ر", "ز", "ژ", "س", "ش", "ص", "ض", "ط",
    "ظ", "ع", "غ", "ف", "ق", "ک", "گ", "ل", "م", "ن",
    "و", "ه", "ی",
)

private fun compareByPersianName(): Comparator<BrowsePersonRowUiModel> = Comparator { left, right ->
    comparePersianTexts(left.name, right.name)
}

private fun comparePersianTexts(left: String, right: String): Int {
    val leftChars = normalizePersianText(left)
    val rightChars = normalizePersianText(right)
    val commonLength = minOf(leftChars.length, rightChars.length)

    for (index in 0 until commonLength) {
        val comparison = persianAlphabetIndex(leftChars[index]) - persianAlphabetIndex(rightChars[index])
        if (comparison != 0) return comparison
    }

    return leftChars.length - rightChars.length
}

private fun persianAlphabetIndex(charOrLabel: Char): Int =
    persianAlphabetIndex(charOrLabel.toString())

private fun persianAlphabetIndex(charOrLabel: String): Int {
    val normalized = normalizePersianText(charOrLabel)
    val first = normalized.firstOrNull()?.toString() ?: return Int.MAX_VALUE
    return persianAlphabet.indexOf(first).takeIf { it >= 0 } ?: Int.MAX_VALUE
}

private fun firstPersianLetter(value: String): String? =
    normalizePersianText(value)
        .firstOrNull { candidate ->
            persianAlphabet.contains(candidate.toString())
        }
        ?.toString()

private fun normalizePersianText(value: String): String =
    buildString {
        value.trim().forEach { char ->
            append(
                when (char) {
                    'ي', 'ى' -> 'ی'
                    'ك' -> 'ک'
                    'آ', 'أ', 'إ' -> 'ا'
                    'ؤ' -> 'و'
                    'ۀ', 'ة' -> 'ه'
                    'ئ' -> 'ی'
                    else -> char
                }
            )
        }
    }
