package com.radiogolha.mobile.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterSheet(
    options: SearchOptionsUiState,
    activeFilters: ActiveFilters,
    onFiltersChanged: (ActiveFilters) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(SearchFilterType.Category) }
    var filterQuery by remember { mutableStateOf("") }

    // Reset filter query when switching tabs
    LaunchedEffect(selectedType) { filterQuery = "" }

    val currentOptions = options.optionsFor(selectedType)
    val selectedIds = activeFilters.idsFor(selectedType)

    val filteredOptions = remember(currentOptions, filterQuery) {
        if (filterQuery.isBlank()) currentOptions
        else currentOptions.filter { it.name.contains(filterQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = GolhaColors.ScreenBackground,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .statusBarsPadding()
                    .padding(top = 8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(0.dp)
                        .let {
                            it
                        },
                )
            }
        },
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f)
                    .padding(bottom = 16.dp),
            ) {
                // Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "فیلترها",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.PrimaryText,
                    )
                    if (activeFilters.activeFilterCount > 0) {
                        TextButton(onClick = { onFiltersChanged(ActiveFilters(transcriptQuery = activeFilters.transcriptQuery)) }) {
                            Text("پاک کردن همه", color = GolhaColors.PrimaryAccent)
                        }
                    }
                }

                // Filter type tabs
                ScrollableTabRow(
                    selectedTabIndex = SearchFilterType.entries.indexOf(selectedType),
                    containerColor = Color.Transparent,
                    contentColor = GolhaColors.PrimaryAccent,
                    edgePadding = GolhaSpacing.ScreenHorizontal,
                    divider = {},
                    indicator = { tabPositions ->
                        val index = SearchFilterType.entries.indexOf(selectedType)
                        if (index in tabPositions.indices) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = GolhaColors.PrimaryAccent,
                            )
                        }
                    },
                ) {
                    SearchFilterType.entries.forEach { type ->
                        val selected = selectedType == type
                        val count = activeFilters.idsFor(type).size
                        Tab(
                            selected = selected,
                            onClick = { selectedType = type },
                            text = {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = type.label,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        ),
                                        color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText,
                                    )
                                    if (count > 0) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = GolhaColors.PrimaryAccent,
                                            modifier = Modifier.size(18.dp),
                                        ) {
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

                Spacer(modifier = Modifier.height(8.dp))

                // In-sheet search
                if (currentOptions.size > 10) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = GolhaSpacing.ScreenHorizontal)
                            .padding(bottom = 8.dp),
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
                                if (filterQuery.isEmpty()) {
                                    Text(
                                        text = "جستجو در ${selectedType.label}...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = GolhaColors.SecondaryText.copy(alpha = 0.5f),
                                    )
                                }
                                BasicTextField(
                                    value = filterQuery,
                                    onValueChange = { filterQuery = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    textStyle = TextStyle(
                                        color = GolhaColors.PrimaryText,
                                        fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                    ),
                                    cursorBrush = SolidColor(GolhaColors.PrimaryAccent),
                                    singleLine = true,
                                )
                            }
                        }
                    }
                }

                // Options list
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(filteredOptions, key = { it.id }) { option ->
                        val isSelected = option.id in selectedIds
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) GolhaColors.PrimaryAccent.copy(alpha = 0.1f) else GolhaColors.Surface,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GolhaColors.PrimaryAccent.copy(alpha = 0.5f) else GolhaColors.Border.copy(alpha = 0.4f),
                            ),
                            onClick = { onFiltersChanged(activeFilters.withToggled(selectedType, option.id)) },
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = GolhaColors.PrimaryAccent,
                                        uncheckedColor = GolhaColors.Border,
                                    ),
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = option.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    ),
                                    color = if (isSelected) GolhaColors.PrimaryAccent else GolhaColors.PrimaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
