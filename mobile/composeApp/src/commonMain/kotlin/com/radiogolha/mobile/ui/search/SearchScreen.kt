package com.radiogolha.mobile.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.radiogolha.mobile.ui.people.comparePersianTexts
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import com.radiogolha.mobile.ui.programs.SkeletonTrackRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private enum class SearchPage { Filters, Results }

@Composable
fun SearchScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onProgramClick: (Long) -> Unit = {},
    onPlayTrack: (TrackUiModel) -> Unit = {},
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
    var currentPage by remember { mutableStateOf(SearchPage.Filters) }
    var selectedFilterType by remember { mutableStateOf(SearchFilterType.Category) }
    var filterSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.Default) {
            searchOptions = runCatching { loadSearchOptions() }.getOrDefault(SearchOptionsUiState())
        }
    }

    LaunchedEffect(selectedFilterType) { filterSearchQuery = "" }

    val doSearch: () -> Unit = {
        currentPage = SearchPage.Results
        isLoading = true
        allResults = emptyList()
    }

    LaunchedEffect(currentPage, isLoading) {
        if (currentPage == SearchPage.Results && isLoading) {
            val r = withContext(Dispatchers.Default) {
                runCatching { searchPrograms(activeFilters, 1) }.getOrDefault(SearchResultsUiState())
            }
            results = r
            allResults = r.results
            isLoading = false
        }
    }

    val listState = rememberLazyListState()
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalItems = listState.layoutInfo.totalItemsCount
            lastVisible >= totalItems - 3 && !isLoading && !isLoadingMore && results.page < results.totalPages && currentPage == SearchPage.Results
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) {
            isLoadingMore = true
            val r = withContext(Dispatchers.Default) {
                runCatching { searchPrograms(activeFilters, results.page + 1) }.getOrDefault(SearchResultsUiState())
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
                when (currentPage) {
                    SearchPage.Filters -> FiltersPage(
                        innerPadding = innerPadding,
                        activeFilters = activeFilters,
                        searchOptions = searchOptions,
                        selectedFilterType = selectedFilterType,
                        filterSearchQuery = filterSearchQuery,
                        onFilterTypeChange = { selectedFilterType = it },
                        onFilterSearchChange = { filterSearchQuery = it },
                        onFiltersChanged = { activeFilters = it },
                        onSearch = doSearch,
                    )
                    SearchPage.Results -> ResultsPage(
                        innerPadding = innerPadding,
                        listState = listState,
                        results = allResults,
                        total = results.total,
                        isLoading = isLoading,
                        isLoadingMore = isLoadingMore,
                        activeFilters = activeFilters,
                        searchOptions = searchOptions,
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        onProgramClick = onProgramClick,
                        onPlayTrack = onPlayTrack,
                        onBackToFilters = { currentPage = SearchPage.Filters },
                    )
                }
            }
        }
    }
}

private data class ActiveChip(val type: SearchFilterType, val id: Long, val label: String)

@Composable
private fun ActiveFilterChips(
    activeFilters: ActiveFilters,
    searchOptions: SearchOptionsUiState,
    onRemove: (SearchFilterType, Long) -> Unit = { _, _ -> },
    onRemoveTranscript: () -> Unit = {},
) {
    val chips = remember(activeFilters, searchOptions) {
        buildList {
            // Transcript as a chip
            if (activeFilters.transcriptQuery.isNotBlank()) {
                add(ActiveChip(SearchFilterType.Transcript, -1, "متن: ${activeFilters.transcriptQuery}"))
            }
            SearchFilterType.entries.filter { it != SearchFilterType.Transcript }.forEach { type ->
                activeFilters.idsFor(type).forEach { id ->
                    val name = searchOptions.optionsFor(type).find { it.id == id }?.name ?: return@forEach
                    add(ActiveChip(type, id, name))
                }
            }
        }
    }
    if (chips.isEmpty()) return
    LazyRow(
        modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal).padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        items(chips, key = { "${it.type}-${it.id}" }) { chip ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = GolhaColors.PrimaryAccent.copy(alpha = 0.12f),
                border = BorderStroke(0.5.dp, GolhaColors.PrimaryAccent.copy(alpha = 0.3f)),
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = chip.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = GolhaColors.PrimaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Surface(
                        modifier = Modifier.size(18.dp).clickable {
                            if (chip.type == SearchFilterType.Transcript) onRemoveTranscript()
                            else onRemove(chip.type, chip.id)
                        },
                        shape = CircleShape,
                        color = GolhaColors.PrimaryAccent.copy(alpha = 0.2f),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("✕", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = GolhaColors.PrimaryText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersPage(
    innerPadding: PaddingValues,
    activeFilters: ActiveFilters,
    searchOptions: SearchOptionsUiState,
    selectedFilterType: SearchFilterType,
    filterSearchQuery: String,
    onFilterTypeChange: (SearchFilterType) -> Unit,
    onFilterSearchChange: (String) -> Unit,
    onFiltersChanged: (ActiveFilters) -> Unit,
    onSearch: () -> Unit,
) {
    val currentOptions = searchOptions.optionsFor(selectedFilterType)
    val selectedIds = activeFilters.idsFor(selectedFilterType)
    val filteredOptions = remember(currentOptions, filterSearchQuery) {
        val base = if (filterSearchQuery.isBlank()) currentOptions
        else currentOptions.filter { it.name.contains(filterSearchQuery, ignoreCase = true) }
        base.sortedWith { a, b -> comparePersianTexts(a.name, b.name) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
            .statusBarsPadding(),
    ) {
        // Header
        Text(
            text = "جستجو",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
            modifier = Modifier
                .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                .padding(top = 16.dp, bottom = 8.dp),
        )

        // Filter type tabs
        ScrollableTabRow(
            selectedTabIndex = SearchFilterType.entries.indexOf(selectedFilterType),
            containerColor = Color.Transparent,
            contentColor = GolhaColors.PrimaryAccent,
            edgePadding = GolhaSpacing.ScreenHorizontal,
            divider = {},
            indicator = { tabPositions ->
                val index = SearchFilterType.entries.indexOf(selectedFilterType)
                if (index in tabPositions.indices) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                        color = GolhaColors.PrimaryAccent,
                    )
                }
            },
        ) {
            SearchFilterType.entries.forEach { type ->
                val selected = selectedFilterType == type
                val count = activeFilters.idsFor(type).size
                Tab(
                    selected = selected,
                    onClick = { onFilterTypeChange(type) },
                    text = {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = type.label,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium),
                                color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText,
                            )
                            if (count > 0) {
                                Surface(shape = CircleShape, color = GolhaColors.PrimaryAccent, modifier = Modifier.size(18.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$count", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp), color = Color.White)
                                    }
                                }
                            }
                        }
                    },
                )
            }
        }

        // Any/All toggle
        if (selectedFilterType != SearchFilterType.Category && selectedFilterType != SearchFilterType.Transcript && selectedIds.isNotEmpty()) {
            MatchModeToggle(
                matchMode = activeFilters.matchModeFor(selectedFilterType),
                onChanged = { onFiltersChanged(activeFilters.withMatchMode(selectedFilterType, it)) },
                modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal).padding(top = 8.dp),
            )
        }

        // Active filter chips (below toggle)
        ActiveFilterChips(
            activeFilters = activeFilters,
            searchOptions = searchOptions,
            onRemove = { type, id -> onFiltersChanged(activeFilters.withToggled(type, id)) },
            onRemoveTranscript = { onFiltersChanged(activeFilters.copy(transcriptQuery = "")) },
        )

        // Transcript tab content
        if (selectedFilterType == SearchFilterType.Transcript) {
            SearchInputField(
                query = activeFilters.transcriptQuery,
                onQueryChange = { onFiltersChanged(activeFilters.copy(transcriptQuery = it)) },
                modifier = Modifier.padding(horizontal = GolhaSpacing.ScreenHorizontal).padding(top = 8.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        // In-filter search (for non-transcript tabs)
        if (selectedFilterType != SearchFilterType.Transcript && currentOptions.size > 10) {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = GolhaSpacing.ScreenHorizontal).padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp),
                color = GolhaColors.Surface,
                border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GolhaLineIcon(icon = GolhaIcon.Search, modifier = Modifier.size(16.dp), tint = GolhaColors.SecondaryText)
                    Box(modifier = Modifier.weight(1f)) {
                        if (filterSearchQuery.isEmpty()) {
                            Text("فیلتر ${selectedFilterType.label}...", style = MaterialTheme.typography.bodyMedium, color = GolhaColors.SecondaryText.copy(alpha = 0.5f))
                        }
                        BasicTextField(
                            value = filterSearchQuery, onValueChange = onFilterSearchChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(color = GolhaColors.PrimaryText, fontSize = MaterialTheme.typography.bodyMedium.fontSize),
                            cursorBrush = SolidColor(GolhaColors.PrimaryAccent), singleLine = true,
                        )
                    }
                }
            }
        }

        // Options list (not for Transcript tab)
        if (selectedFilterType == SearchFilterType.Transcript) {
            // Already handled above
        } else LazyColumn(
            modifier = Modifier.weight(1f).padding(top = 8.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filteredOptions, key = { it.id }) { option ->
                val isSelected = option.id in selectedIds
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) GolhaColors.PrimaryAccent.copy(alpha = 0.1f) else Color.Transparent,
                    onClick = { onFiltersChanged(activeFilters.withToggled(selectedFilterType, option.id)) },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Checkbox(
                            checked = isSelected, onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = GolhaColors.PrimaryAccent, uncheckedColor = GolhaColors.Border),
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = option.name,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal),
                            color = if (isSelected) GolhaColors.PrimaryAccent else GolhaColors.PrimaryText,
                            maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        // Search button
        Button(
            onClick = onSearch,
            modifier = Modifier.fillMaxWidth().padding(horizontal = GolhaSpacing.ScreenHorizontal).padding(top = 8.dp, bottom = 8.dp).height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = GolhaColors.PrimaryAccent),
            enabled = activeFilters.hasAnyFilter,
        ) {
            Text("جستجو", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
        }
    }
}

@Composable
private fun ResultsPage(
    innerPadding: PaddingValues,
    listState: androidx.compose.foundation.lazy.LazyListState,
    results: List<SearchResultUiModel>,
    total: Int,
    isLoading: Boolean,
    isLoadingMore: Boolean,
    activeFilters: ActiveFilters,
    searchOptions: SearchOptionsUiState,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    onProgramClick: (Long) -> Unit,
    onPlayTrack: (TrackUiModel) -> Unit,
    onBackToFilters: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = innerPadding.calculateBottomPadding())
            .statusBarsPadding(),
    ) {
        // Header - back button on the left (end in RTL)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                .padding(top = 16.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Title + badge on the right (start in RTL)
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "نتایج جستجو",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = GolhaColors.PrimaryText,
                )
                if (!isLoading) {
                    Surface(shape = RoundedCornerShape(20.dp), color = GolhaColors.PrimaryAccent) {
                        Text(
                            text = "$total",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
            // Back button on the left (end in RTL)
            Surface(
                modifier = Modifier.size(36.dp).clickable { onBackToFilters() },
                shape = CircleShape,
                color = GolhaColors.Surface,
                border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    GolhaLineIcon(icon = GolhaIcon.Back, modifier = Modifier.size(18.dp), tint = GolhaColors.PrimaryText)
                }
            }
        }

        // Active filter chips (read-only in results)
        ActiveFilterChips(activeFilters = activeFilters, searchOptions = searchOptions)
        if (activeFilters.hasAnyFilter) Spacer(modifier = Modifier.height(4.dp))

        // Results
        if (isLoading) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 4.dp),
            ) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(GolhaRadius.Card),
                        color = GolhaColors.Surface,
                        border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)),
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            repeat(8) { index ->
                                SkeletonTrackRow()
                                if (index != 7) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                            }
                        }
                    }
                }
            }
        } else if (results.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("نتیجه‌ای یافت نشد", style = MaterialTheme.typography.titleMedium, color = GolhaColors.SecondaryText)
                    Text("فیلترها را کمتر کنید یا متن جستجو را تغییر دهید", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText.copy(alpha = 0.7f))
                }
            }
        } else {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 4.dp),
                ) {
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(GolhaRadius.Card),
                            color = GolhaColors.Surface,
                            border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)),
                        ) {
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                results.forEachIndexed { index, result ->
                                    val track = TrackUiModel(
                                        id = result.id,
                                        title = result.title,
                                        artist = result.categoryName,
                                        duration = result.duration,
                                        audioUrl = result.audioUrl,
                                    )
                                    val isActive = currentTrack?.id == result.id
                                    ProgramTrackRow(
                                        track = track,
                                        isActive = isActive,
                                        isPlaying = isActive && isPlayerPlaying,
                                        onTrackClick = { onProgramClick(result.id) },
                                        onPlayClick = { onPlayTrack(track) },
                                    )
                                    if (index != results.lastIndex) {
                                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                                    }
                                }
                            }
                        }
                    }
                    if (isLoadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GolhaColors.PrimaryAccent, strokeWidth = 2.dp)
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}

@Composable
private fun MatchModeToggle(matchMode: MatchMode, onChanged: (MatchMode) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("تطبیق:", style = MaterialTheme.typography.bodySmall, color = GolhaColors.SecondaryText)
        Surface(shape = RoundedCornerShape(20.dp), color = GolhaColors.Surface, border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f))) {
            Row(modifier = Modifier.padding(2.dp)) {
                MatchMode.entries.forEach { mode ->
                    val selected = matchMode == mode
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(if (selected) GolhaColors.PrimaryAccent else Color.Transparent).clickable { onChanged(mode) }.padding(horizontal = 14.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(mode.label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal), color = if (selected) Color.White else GolhaColors.SecondaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchInputField(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = GolhaColors.Surface, border = BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.6f)), shadowElevation = 2.dp) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GolhaLineIcon(icon = GolhaIcon.Search, modifier = Modifier.size(20.dp), tint = GolhaColors.SecondaryText)
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) Text("جستجو در متن برنامه‌ها...", style = MaterialTheme.typography.bodyLarge, color = GolhaColors.SecondaryText.copy(alpha = 0.6f))
                BasicTextField(value = query, onValueChange = onQueryChange, modifier = Modifier.fillMaxWidth(), textStyle = TextStyle(color = GolhaColors.PrimaryText, fontSize = MaterialTheme.typography.bodyLarge.fontSize), cursorBrush = SolidColor(GolhaColors.PrimaryAccent), singleLine = true)
            }
        }
    }
}
