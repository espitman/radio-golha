package com.radiogolha.mobile.ui.search

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.programs.ProgramTrackRow
import com.radiogolha.mobile.ui.programs.SkeletonTrackRow
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    filters: ActiveFilters,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    onRename: (String) -> Unit = {},
    onDelete: () -> Unit = {},
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
    var results by remember { mutableStateOf<List<SearchResultUiModel>?>(null) }
    var allResults by remember { mutableStateOf<List<SearchResultUiModel>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }

    LaunchedEffect(filters) {
        val r = withContext(Dispatchers.Default) {
            runCatching { searchPrograms(filters, 1) }.getOrDefault(SearchResultsUiState())
        }
        results = r.results
        allResults = r.results
        page = r.page
        totalPages = r.totalPages
    }

    val isLoading = results == null
    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var renameText by remember(playlistName) { mutableStateOf(playlistName) }

    // Rename bottom sheet
    if (showRenameSheet) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showRenameSheet = false },
            containerColor = GolhaColors.ScreenBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("تغییر نام", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                    Surface(shape = RoundedCornerShape(12.dp), color = GolhaColors.Surface, border = BorderStroke(1.dp, GolhaColors.Border)) {
                        BasicTextField(
                            value = renameText, onValueChange = { renameText = it },
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            textStyle = TextStyle(color = GolhaColors.PrimaryText, fontSize = 16.sp),
                            cursorBrush = SolidColor(GolhaColors.PrimaryAccent), singleLine = true,
                        )
                    }
                    Button(
                        onClick = { onRename(renameText); showRenameSheet = false },
                        enabled = renameText.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GolhaColors.PrimaryAccent),
                    ) { Text("ذخیره", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                }
            }
        }
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        @OptIn(ExperimentalMaterial3Api::class)
        ModalBottomSheet(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = GolhaColors.ScreenBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("حذف لیست", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText)
                    Text("آیا از حذف «$playlistName» مطمئنید؟", style = MaterialTheme.typography.bodyMedium, color = GolhaColors.SecondaryText)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GolhaColors.Border),
                        ) { Text("انصراف", color = GolhaColors.SecondaryText) }
                        Button(
                            onClick = { showDeleteConfirm = false; onDelete() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFCC3333)),
                        ) { Text("حذف", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) }
                    }
                }
            }
        }
    }

    TabRootScreen(
        title = playlistName,
        subtitle = "",
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        currentTrack = currentTrack,
        isPlayerPlaying = isPlayerPlaying,
        isPlayerLoading = isPlayerLoading,
        currentPlaybackPositionMs = currentPlaybackPositionMs,
        currentPlaybackDurationMs = currentPlaybackDurationMs,
        onTogglePlayerPlayback = onTogglePlayerPlayback,
        onTrackClick = onTrackClick,
        onExpandPlayer = onExpandPlayer,
        onBackClick = onBackClick,
        content = {
            // Action buttons
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        modifier = Modifier.clickable { showRenameSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = GolhaColors.BadgeBackground,
                        border = BorderStroke(1.dp, GolhaColors.Border),
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            GolhaLineIcon(icon = GolhaIcon.Note, modifier = Modifier.size(14.dp), tint = GolhaColors.PrimaryText)
                            Text("تغییر نام", style = MaterialTheme.typography.labelMedium, color = GolhaColors.PrimaryText)
                        }
                    }
                    Surface(
                        modifier = Modifier.clickable { showDeleteConfirm = true },
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Red.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f)),
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("حذف", style = MaterialTheme.typography.labelMedium, color = Color.Red.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            repeat(8) { index ->
                                SkeletonTrackRow()
                                if (index != 7) HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                            }
                        }
                    }
                }
            } else if (allResults.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        Text("برنامه‌ای یافت نشد", color = GolhaColors.SecondaryText)
                    }
                }
            } else {
                item {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card))
                                .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(GolhaRadius.Card))
                                .padding(vertical = 8.dp),
                        ) {
                            allResults.forEachIndexed { index, result ->
                                val track = TrackUiModel(
                                    id = result.id,
                                    title = result.title,
                                    artist = result.artist ?: result.categoryName,
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
                                if (index != allResults.lastIndex) {
                                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = GolhaColors.Border.copy(alpha = 0.65f))
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    )
}
