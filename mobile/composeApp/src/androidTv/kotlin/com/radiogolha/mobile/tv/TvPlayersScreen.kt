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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.MusicianListItemUiModel
import com.radiogolha.mobile.ui.musicians.loadMusiciansUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun TvPlayersScreen(
    entryFocusRequester: FocusRequester,
    lastCardFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    onOpenArtist: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var players by remember { mutableStateOf<List<MusicianListItemUiModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedInstrument by remember { mutableStateOf("همه") }
    val firstCardFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        isLoading = true
        players = withContext(Dispatchers.Default) {
            runCatching { loadMusiciansUiState() }
                .onFailure { println("ERROR TvPlayersScreen loadMusiciansUiState: ${it.message}") }
                .getOrDefault(emptyList())
        }
        isLoading = false
    }

    val instruments = remember(players) { tvPlayersPageInstruments(players) }
    LaunchedEffect(instruments) {
        if (selectedInstrument !in instruments) selectedInstrument = "همه"
    }
    val filteredPlayers = remember(players, selectedInstrument) {
        if (selectedInstrument == "همه") players
        else players.filter { it.instrument.contains(selectedInstrument) }
    }
    val rows = remember(filteredPlayers) { filteredPlayers.chunked(4) }
    val cardFocusRequesters = remember(filteredPlayers.size) {
        List(filteredPlayers.size) { FocusRequester() }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp, bottom = 28.dp),
    ) {
        Text(
            text = "نوازندگان",
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
            text = "فهرست مشاهیر موسیقی اصیل ایرانی و نوازندگان برجسته برنامه‌های گل‌ها به تفکیک تخصص و ساز.",
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
            color = Color(0xFF43474E),
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(28.dp))

        TvInstrumentFilterRow(
            instruments = instruments,
            selectedInstrument = selectedInstrument,
            entryFocusRequester = entryFocusRequester,
            topEntryRequester = topEntryRequester,
            firstCardFocusRequester = firstCardFocusRequester,
            playerFocusRequester = playerFocusRequester,
            sidebarEntryRequester = sidebarEntryRequester,
            onSelect = { selectedInstrument = it },
        )

        Spacer(Modifier.height(34.dp))

        when {
            isLoading -> TvPlayersLoadingState(
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
            filteredPlayers.isEmpty() -> TvPlayersEmptyState(
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
                        rowItems.forEachIndexed { columnIndex, player ->
                            val flatIndex = rowIndex * 4 + columnIndex
                            val isFirst = flatIndex == 0
                            val isLast = flatIndex == filteredPlayers.lastIndex
                            val focusRequester = when {
                                isFirst -> firstCardFocusRequester
                                isLast -> lastCardFocusRequester
                                else -> cardFocusRequesters[flatIndex]
                            }
                            val upRequester = when {
                                rowIndex == 0 -> entryFocusRequester
                                flatIndex - 4 == 0 -> firstCardFocusRequester
                                else -> cardFocusRequesters.getOrNull(flatIndex - 4) ?: entryFocusRequester
                            }
                            val downRequester = when {
                                rowIndex == rows.lastIndex -> playerFocusRequester
                                flatIndex + 4 == filteredPlayers.lastIndex -> lastCardFocusRequester
                                else -> cardFocusRequesters.getOrNull(flatIndex + 4) ?: playerFocusRequester
                            }
                            val rightRequester = when {
                                columnIndex == 0 -> sidebarEntryRequester
                                flatIndex - 1 == 0 -> firstCardFocusRequester
                                else -> cardFocusRequesters.getOrNull(flatIndex - 1) ?: sidebarEntryRequester
                            }
                            val leftRequester = when {
                                columnIndex == rowItems.lastIndex -> FocusRequester.Cancel
                                flatIndex + 1 == filteredPlayers.lastIndex -> lastCardFocusRequester
                                else -> cardFocusRequesters.getOrNull(flatIndex + 1) ?: FocusRequester.Cancel
                            }

                            TvArtistCard(
                                item = TvArtistCardItem(
                                    id = player.artistId,
                                    name = player.name,
                                    role = if (selectedInstrument == "همه") {
                                        "${player.instrument} • ${player.programCount} برنامه"
                                    } else {
                                        "${player.programCount} برنامه"
                                    },
                                    imageUrl = player.imageUrl,
                                ),
                                modifier = Modifier
                                    .focusRequester(focusRequester)
                                    .focusProperties {
                                        up = upRequester
                                        down = downRequester
                                        right = rightRequester
                                        left = leftRequester
                                    },
                                onClick = { onOpenArtist(player.artistId) },
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
private fun TvInstrumentFilterRow(
    instruments: List<String>,
    selectedInstrument: String,
    entryFocusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    firstCardFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    onSelect: (String) -> Unit,
) {
    val focusRequesters = remember(instruments) { List(instruments.size) { FocusRequester() } }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
        reverseLayout = false,
    ) {
        itemsIndexed(instruments, key = { _, instrument -> instrument }) { index, instrument ->
            TvInstrumentChip(
                title = instrument,
                selected = instrument == selectedInstrument,
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

    if (instruments.isEmpty()) {
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
private fun TvInstrumentChip(
    title: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val chipWidth = if (title == "همه") 72.dp else 104.dp

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
            .width(chipWidth)
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
                else -> Color(0xFF43474E)
            },
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Composable
private fun TvPlayersLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = GolhaColors.BannerDetail, strokeWidth = 3.dp)
            Text("در حال بارگذاری نوازندگان...", color = GolhaColors.PrimaryText)
        }
    }
}

@Composable
private fun TvPlayersEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.72f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("نوازنده‌ای برای نمایش وجود ندارد", color = GolhaColors.SecondaryText)
    }
}

private fun tvPlayersPageInstruments(players: List<MusicianListItemUiModel>): List<String> {
    val instruments = players
        .map { it.instrument.trim() }
        .filter { it.isNotBlank() }
        .distinct()
        .sorted()
    return listOf("همه") + instruments
}
