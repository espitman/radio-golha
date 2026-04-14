package com.radiogolha.mobile.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onProgramClick: (Long) -> Unit = {},
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onTrackClick: (Long) -> Unit = {},
    onExpandPlayer: () -> Unit = {},
) {
    var searchOptions by remember { mutableStateOf(SearchOptionsUiState()) }
    var activeFilters by remember { mutableStateOf(ActiveFilters()) }
    var results by remember { mutableStateOf(SearchResultsUiState()) }
    var allResults by remember { mutableStateOf<List<SearchResultUiModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    // Load search options once
    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            searchOptions = runCatching { loadSearchOptions() }.getOrDefault(SearchOptionsUiState())
        }
    }

    // Debounced search
    LaunchedEffect(activeFilters) {
        if (!activeFilters.hasAnyFilter) {
            hasSearched = false
            allResults = emptyList()
            results = SearchResultsUiState()
            return@LaunchedEffect
        }
        delay(400)
        isLoading = true
        hasSearched = true
        val r = withContext(Dispatchers.Default) {
            runCatching { searchPrograms(activeFilters, 1) }.getOrDefault(SearchResultsUiState())
        }
        results = r
        allResults = r.results
        isLoading = false
    }

    val listState = rememberLazyListState()

    // Load more when near bottom
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 && !isLoading && !isLoadingMore && results.page < results.totalPages && hasSearched
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            isLoadingMore = true
            val nextPage = results.page + 1
            val r = withContext(Dispatchers.Default) {
                runCatching { searchPrograms(activeFilters, nextPage) }.getOrDefault(SearchResultsUiState())
            }
            results = r
            allResults = allResults + r.results
            isLoadingMore = false
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        com.radiogolha.mobile.theme.GolhaPatternBackground {
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
                        onTrackClick = onTrackClick,
                        onExpand = onExpandPlayer,
                    )
                }
            ) { innerPadding ->
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = innerPadding.calculateBottomPadding())
                        .statusBarsPadding(),
                    contentPadding = PaddingValues(
                        start = GolhaSpacing.ScreenHorizontal,
                        end = GolhaSpacing.ScreenHorizontal,
                        top = 16.dp,
                        bottom = 24.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // Header
                    item {
                        Text(
                            text = "جستجو",
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                            color = GolhaColors.PrimaryText,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }

                    // Search bar
                    item {
                        SearchBar(
                            query = activeFilters.transcriptQuery,
                            onQueryChange = { activeFilters = activeFilters.copy(transcriptQuery = it) },
                        )
                    }

                    // Filter chips row
                    item {
                        FilterChipsRow(
                            activeFilters = activeFilters,
                            searchOptions = searchOptions,
                            onOpenFilters = { showFilterSheet = true },
                            onRemoveFilter = { type, id -> activeFilters = activeFilters.withToggled(type, id) },
                        )
                    }

                    // Results count
                    if (hasSearched && !isLoading) {
                        item {
                            Text(
                                text = "${results.total} نتیجه",
                                style = MaterialTheme.typography.bodyMedium,
                                color = GolhaColors.SecondaryText,
                            )
                        }
                    }

                    // Results
                    if (isLoading && allResults.isEmpty()) {
                        items(6) {
                            SkeletonResultCard()
                        }
                    } else {
                        itemsIndexed(allResults, key = { _, item -> item.id }) { _, result ->
                            SearchResultCard(
                                result = result,
                                onClick = { onProgramClick(result.id) },
                            )
                        }
                    }

                    // Loading more indicator
                    if (isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = GolhaColors.PrimaryAccent,
                                    strokeWidth = 2.dp,
                                )
                            }
                        }
                    }

                    // Empty state
                    if (hasSearched && !isLoading && allResults.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("نتیجه‌ای یافت نشد", color = GolhaColors.SecondaryText)
                            }
                        }
                    }
                }
            }
        }
    }

    // Filter bottom sheet
    if (showFilterSheet) {
        SearchFilterSheet(
            options = searchOptions,
            activeFilters = activeFilters,
            onFiltersChanged = { activeFilters = it },
            onDismiss = { showFilterSheet = false },
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GolhaLineIcon(icon = GolhaIcon.Search, modifier = Modifier.size(20.dp), tint = GolhaColors.SecondaryText)
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = "جستجو در متن برنامه‌ها...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GolhaColors.SecondaryText.copy(alpha = 0.6f),
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(
                        color = GolhaColors.PrimaryText,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    ),
                    cursorBrush = SolidColor(GolhaColors.PrimaryAccent),
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    activeFilters: ActiveFilters,
    searchOptions: SearchOptionsUiState,
    onOpenFilters: () -> Unit,
    onRemoveFilter: (SearchFilterType, Long) -> Unit,
) {
    val activeChips = remember(activeFilters, searchOptions) {
        buildList {
            SearchFilterType.entries.forEach { type ->
                val ids = activeFilters.idsFor(type)
                val options = searchOptions.optionsFor(type)
                ids.forEach { id ->
                    val name = options.find { it.id == id }?.name ?: return@forEach
                    add(Triple(type, id, name))
                }
            }
        }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(0.dp),
    ) {
        item {
            FilterChip(
                selected = false,
                onClick = onOpenFilters,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("فیلترها")
                        if (activeFilters.activeFilterCount > 0) {
                            Surface(shape = CircleShape, color = GolhaColors.PrimaryAccent, modifier = Modifier.size(20.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${activeFilters.activeFilterCount}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color.White,
                                    )
                                }
                            }
                        }
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = GolhaColors.Surface,
                    labelColor = GolhaColors.PrimaryText,
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = GolhaColors.Border,
                    enabled = true,
                    selected = false,
                ),
            )
        }

        items(activeChips, key = { "${it.first}-${it.second}" }) { (type, id, name) ->
            FilterChip(
                selected = true,
                onClick = { onRemoveFilter(type, id) },
                label = { Text(name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                trailingIcon = {
                    Text("✕", style = MaterialTheme.typography.labelSmall, color = Color.White)
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GolhaColors.PrimaryAccent,
                    selectedLabelColor = Color.White,
                ),
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResultUiModel,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Program number badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = GolhaColors.BadgeBackground,
                border = BorderStroke(0.5.dp, GolhaColors.Border),
            ) {
                Text(
                    text = "${result.no}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = GolhaColors.PrimaryText,
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GolhaColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = result.categoryName,
                    style = MaterialTheme.typography.bodySmall,
                    color = GolhaColors.SecondaryText,
                    maxLines = 1,
                )
            }

            if (result.duration != null) {
                Text(
                    text = result.duration,
                    style = MaterialTheme.typography.labelSmall,
                    color = GolhaColors.SecondaryText.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun SkeletonResultCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .width(180.dp)
                        .height(16.dp)
                        .background(GolhaColors.Border.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                )
                Box(
                    modifier = Modifier
                        .width(100.dp)
                        .height(12.dp)
                        .background(GolhaColors.Border.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
