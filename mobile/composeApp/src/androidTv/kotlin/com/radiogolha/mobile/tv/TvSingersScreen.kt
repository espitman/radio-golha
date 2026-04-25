package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun TvSingersScreen(
    entryFocusRequester: FocusRequester,
    lastCardFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    onOpenArtist: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var singers by remember { mutableStateOf<List<SingerListItemUiModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedLetter by remember { mutableStateOf("همه") }
    val firstCardFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        isLoading = true
        singers = withContext(Dispatchers.Default) {
            runCatching { loadSingersUiState() }
                .onFailure { println("ERROR TvSingersScreen loadSingersUiState: ${it.message}") }
                .getOrDefault(emptyList())
        }
        isLoading = false
    }

    val alphabet = remember(singers) { singersPageAlphabet(singers) }
    LaunchedEffect(alphabet) {
        if (selectedLetter !in alphabet) selectedLetter = "همه"
    }
    val filteredSingers = remember(singers, selectedLetter) {
        if (selectedLetter == "همه") singers else singers.filter { matchesTvSingerLetter(it.name, selectedLetter) }
    }
    val rows = remember(filteredSingers) { filteredSingers.chunked(4) }
    val rowFocusRequesters = remember(filteredSingers.size) {
        List(filteredSingers.size) { FocusRequester() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 7.dp, bottom = 28.dp),
    ) {
        Text(
            text = "خوانندگان",
            style = MaterialTheme.typography.displaySmall.copy(
                fontSize = 27.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "فهرست جامع اساتید و خوانندگان تاریخ رادیو گل‌ها به ترتیب حروف الفبا.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
            color = Color(0xFF43474E),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))

        TvSingerAlphabetRow(
            letters = alphabet,
            selectedLetter = selectedLetter,
            entryFocusRequester = entryFocusRequester,
            topEntryRequester = topEntryRequester,
            firstCardFocusRequester = firstCardFocusRequester,
            playerFocusRequester = playerFocusRequester,
            sidebarEntryRequester = sidebarEntryRequester,
            onSelect = { selectedLetter = it },
        )

        Spacer(Modifier.height(34.dp))

        when {
            isLoading -> TvSingersLoadingState(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .focusRequester(firstCardFocusRequester)
                    .focusRequester(lastCardFocusRequester)
                    .focusProperties {
                        up = entryFocusRequester
                        down = playerFocusRequester
                        right = sidebarEntryRequester
                    }
                    .focusable(),
            )
            filteredSingers.isEmpty() -> TvSingersEmptyState(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .focusRequester(firstCardFocusRequester)
                    .focusRequester(lastCardFocusRequester)
                    .focusProperties {
                        up = entryFocusRequester
                        down = playerFocusRequester
                        right = sidebarEntryRequester
                    }
                    .focusable(),
            )
            else -> Column(
                verticalArrangement = Arrangement.spacedBy(32.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rows.forEachIndexed { rowIndex, rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(32.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        rowItems.forEachIndexed { columnIndex, singer ->
                            val flatIndex = rowIndex * 4 + columnIndex
                            val isFirst = flatIndex == 0
                            val isLast = flatIndex == filteredSingers.lastIndex
                            val focusRequester = when {
                                isFirst -> firstCardFocusRequester
                                isLast -> lastCardFocusRequester
                                else -> rowFocusRequesters[flatIndex]
                            }
                            val upRequester = when {
                                rowIndex == 0 -> entryFocusRequester
                                flatIndex - 4 == 0 -> firstCardFocusRequester
                                else -> rowFocusRequesters.getOrNull(flatIndex - 4) ?: entryFocusRequester
                            }
                            val downRequester = when {
                                rowIndex == rows.lastIndex -> playerFocusRequester
                                flatIndex + 4 == filteredSingers.lastIndex -> lastCardFocusRequester
                                else -> rowFocusRequesters.getOrNull(flatIndex + 4) ?: playerFocusRequester
                            }
                            val rightRequester = when {
                                columnIndex == 0 -> sidebarEntryRequester
                                flatIndex - 1 == 0 -> firstCardFocusRequester
                                else -> rowFocusRequesters.getOrNull(flatIndex - 1) ?: sidebarEntryRequester
                            }
                            val leftRequester = when {
                                columnIndex == rowItems.lastIndex -> FocusRequester.Cancel
                                flatIndex + 1 == filteredSingers.lastIndex -> lastCardFocusRequester
                                else -> rowFocusRequesters.getOrNull(flatIndex + 1) ?: FocusRequester.Cancel
                            }

                            TvArtistCard(
                                item = TvArtistCardItem(
                                    id = singer.artistId,
                                    name = singer.name,
                                    role = "${singer.programCount} برنامه",
                                    imageUrl = singer.imageUrl,
                                ),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .focusProperties {
                                        up = upRequester
                                        down = downRequester
                                        right = rightRequester
                                        left = leftRequester
                                    },
                                onClick = { onOpenArtist(singer.artistId) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvSingerAlphabetRow(
    letters: List<String>,
    selectedLetter: String,
    entryFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    firstCardFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    onSelect: (String) -> Unit,
) {
    val focusRequesters = remember(letters) { List(letters.size) { FocusRequester() } }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        reverseLayout = false,
    ) {
        itemsIndexed(letters, key = { _, letter -> letter }) { index, letter ->
            TvSingerAlphabetChip(
                title = letter,
                selected = letter == selectedLetter,
                modifier = Modifier
                    .focusRequester(if (index == 0) entryFocusRequester else focusRequesters[index])
                    .focusProperties {
                        up = topEntryRequester
                        down = firstCardFocusRequester
                        right = if (index == 0) sidebarEntryRequester else focusRequesters[index - 1]
                        left = focusRequesters.getOrNull(index + 1) ?: FocusRequester.Cancel
                    },
                onClick = onSelect,
            )
        }
    }

    if (letters.isEmpty()) {
        Spacer(
            modifier = Modifier
                .height(1.dp)
                .focusRequester(entryFocusRequester)
                .focusProperties {
                    up = topEntryRequester
                    down = playerFocusRequester
                    right = sidebarEntryRequester
                }
                .focusable(),
        )
    }
}

@Composable
private fun TvSingerAlphabetChip(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                when {
                    selected -> GolhaColors.BannerDetail
                    isFocused -> Color.White
                    else -> Color(0xFFF1EEE5)
                }
            )
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) GolhaColors.BannerDetail else Color.Transparent,
                shape = CircleShape,
            )
            .clickable { onClick(title) }
            .onFocusChanged { isFocused = it.isFocused }
            .focusable()
            .width(if (title == "همه") 72.dp else 56.dp)
            .height(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
            ),
            color = when {
                selected -> Color.White
                isFocused -> GolhaColors.PrimaryText
                else -> GolhaColors.PrimaryText.copy(alpha = 0.82f)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun TvSingersLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = GolhaColors.BannerDetail, strokeWidth = 3.dp)
            Text("در حال بارگذاری خوانندگان...", color = GolhaColors.PrimaryText)
        }
    }
}

@Composable
private fun TvSingersEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("خواننده‌ای برای نمایش وجود ندارد", color = GolhaColors.SecondaryText)
    }
}

private fun singersPageAlphabet(singers: List<SingerListItemUiModel>): List<String> {
    val allGroups = listOf("الف", "ب", "پ", "ت", "ج", "چ", "ح", "خ", "د", "ر", "ز", "س", "ش", "ع", "ق", "م", "ن", "و", "ه", "ی")
    return listOf("همه") + allGroups.filter { group ->
        singers.any { matchesTvSingerLetter(it.name, group) }
    }
}

private fun matchesTvSingerLetter(name: String, letterGroup: String): Boolean {
    val head = name.trim().firstOrNull()?.toString() ?: return false
    return when (letterGroup) {
        "الف" -> head in listOf("ا", "آ", "أ", "إ")
        "ب" -> head == "ب"
        "پ" -> head == "پ"
        "ت" -> head == "ت"
        "ج" -> head == "ج"
        "چ" -> head == "چ"
        "ح" -> head == "ح"
        "خ" -> head == "خ"
        "د" -> head == "د"
        "ر" -> head == "ر"
        "ز" -> head == "ز"
        "س" -> head == "س"
        "ش" -> head == "ش"
        "ع" -> head == "ع"
        "ق" -> head == "ق"
        "م" -> head == "م"
        "ن" -> head == "ن"
        "و" -> head == "و"
        "ه" -> head == "ه"
        "ی" -> head in listOf("ی", "ي")
        else -> true
    }
}
