package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.artists.loadArtistDetail
import com.radiogolha.mobile.ui.home.ArtistDetailUiModel
import com.radiogolha.mobile.ui.home.CategoryProgramUiModel
import com.radiogolha.mobile.ui.home.TrackUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
@OptIn(ExperimentalComposeUiApi::class)
internal fun TvArtistDetailScreen(
    artistId: Long,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    isPlayerLoading: Boolean,
    entryFocusRequester: FocusRequester,
    lastTrackFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    onPlayTrack: (TrackUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    var detail by remember(artistId) { mutableStateOf<ArtistDetailUiModel?>(null) }
    var isLoading by remember(artistId) { mutableStateOf(true) }
    val firstTrackFocusRequester = remember { FocusRequester() }

    LaunchedEffect(artistId) {
        isLoading = true
        detail = withContext(Dispatchers.Default) {
            runCatching { loadArtistDetail(artistId) }
                .onFailure { println("ERROR TvArtistDetailScreen loadArtistDetail: ${it.message}") }
                .getOrNull()
        }
        isLoading = false
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 28.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        when {
            isLoading -> TvArtistDetailLoading(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .focusRequester(entryFocusRequester)
                    .focusRequester(lastTrackFocusRequester)
                    .focusProperties {
                        right = sidebarEntryRequester
                        down = playerFocusRequester
                    }
                    .focusable(),
            )
            detail == null -> TvArtistDetailEmpty(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .focusRequester(entryFocusRequester)
                    .focusRequester(lastTrackFocusRequester)
                    .focusProperties {
                        right = sidebarEntryRequester
                        down = playerFocusRequester
                    }
                    .focusable(),
            )
            else -> {
                val resolved = detail ?: return@Column
                TvArtistHero(
                    detail = resolved,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(entryFocusRequester)
                        .focusProperties {
                            right = sidebarEntryRequester
                            down = firstTrackFocusRequester
                        }
                        .focusable(),
                )

                TvArtistProgramsPanel(
                    detail = resolved,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    firstTrackFocusRequester = firstTrackFocusRequester,
                    lastTrackFocusRequester = lastTrackFocusRequester,
                    heroFocusRequester = entryFocusRequester,
                    playerFocusRequester = playerFocusRequester,
                    onPlayTrack = onPlayTrack,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun TvArtistHero(
    detail: ArtistDetailUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.height(230.dp),
        horizontalArrangement = Arrangement.spacedBy(34.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(210.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE5E2DA)),
            contentAlignment = Alignment.Center,
        ) {
            if (!detail.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = detail.imageUrl,
                    contentDescription = detail.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF002045).copy(alpha = 0.16f)),
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = null,
                    tint = GolhaColors.SecondaryText.copy(alpha = 0.7f),
                    modifier = Modifier.size(72.dp),
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .height(230.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = detail.name,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = GolhaColors.PrimaryText,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
            )

            Spacer(Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(GolhaColors.PrimaryText)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp),
                )
                Text(
                    text = "افزودن به علاقه‌مندی‌ها",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TvArtistStat(value = toPersianDigits(detail.trackCount.toString()), label = "کل برنامه‌ها")
                val stats = detail.categoryCounts.take(3)
                if (stats.isEmpty()) {
                    TvArtistStat(value = toPersianDigits(detail.tracks.size.toString()), label = "برنامه")
                } else {
                    stats.forEach { stat ->
                        TvArtistStat(value = toPersianDigits(stat.count.toString()), label = stat.title)
                    }
                }
            }
        }
    }
}

@Composable
private fun TvArtistStat(
    value: String,
    label: String,
) {
    Column(
        modifier = Modifier.width(76.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
            maxLines = 1,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 7.5.sp),
            color = Color(0xFF6D706F),
            maxLines = 1,
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvArtistProgramsPanel(
    detail: ArtistDetailUiModel,
    currentTrack: TrackUiModel?,
    isPlayerPlaying: Boolean,
    isPlayerLoading: Boolean,
    firstTrackFocusRequester: FocusRequester,
    lastTrackFocusRequester: FocusRequester,
    heroFocusRequester: FocusRequester,
    playerFocusRequester: FocusRequester,
    onPlayTrack: (TrackUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleTracks = remember(detail.artistId, detail.tracks) { detail.tracks }
    val rowFocusRequesters = remember(detail.artistId, visibleTracks.size) {
        List((visibleTracks.size - 1).coerceAtLeast(0)) { FocusRequester() }
    }

    Column(modifier = modifier) {
        TvArtistPanelHeader(title = "برنامه‌ها", fontSize = 18)
        Spacer(Modifier.height(16.dp))

        if (detail.tracks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(92.dp)
                    .focusRequester(lastTrackFocusRequester)
                    .focusProperties {
                        up = heroFocusRequester
                        down = playerFocusRequester
                    }
                    .focusable()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.78f))
                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.7f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text("ترکی برای این هنرمند پیدا نشد", color = GolhaColors.SecondaryText)
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                visibleTracks.forEachIndexed { index, program ->
                    val track = program.toTvTrackUiModel(detail)
                    val currentFocusRequester = when {
                        index == 0 -> firstTrackFocusRequester
                        index == visibleTracks.lastIndex -> lastTrackFocusRequester
                        else -> rowFocusRequesters[index - 1]
                    }
                    val previousFocusRequester = if (index == 0) heroFocusRequester else if (index == 1) firstTrackFocusRequester else rowFocusRequesters[index - 2]
                    val nextFocusRequester = when {
                        index == visibleTracks.lastIndex -> playerFocusRequester
                        index + 1 == visibleTracks.lastIndex -> lastTrackFocusRequester
                        else -> rowFocusRequesters[index]
                    }
                    TvTrackRow(
                        item = TvTrackRowItem(
                            id = track.id,
                            title = track.title,
                            artist = track.artist,
                            duration = track.duration,
                            coverUrl = null,
                        ),
                        isPlaying = currentTrack?.id == track.id && isPlayerPlaying,
                        isLoading = currentTrack?.id == track.id && isPlayerLoading,
                        onClick = { onPlayTrack(track) },
                        modifier = Modifier
                            .focusRequester(currentFocusRequester)
                            .then(
                                if (index == 0 && visibleTracks.size == 1) {
                                    Modifier.focusRequester(lastTrackFocusRequester)
                                } else {
                                    Modifier
                                }
                            )
                            .focusProperties {
                                up = previousFocusRequester
                                down = nextFocusRequester
                            },
                    )
                }
            }
        }
    }
}

@Composable
private fun TvArtistPanelHeader(title: String, fontSize: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = fontSize.sp, fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFE5E2DA)),
        )
    }
}

@Composable
private fun TvArtistDetailLoading(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(color = GolhaColors.BannerDetail, strokeWidth = 3.dp, modifier = Modifier.size(28.dp))
            Text("در حال بارگذاری هنرمند...", color = GolhaColors.PrimaryText)
        }
    }
}

@Composable
private fun TvArtistDetailEmpty(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.75f))
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("اطلاعات این هنرمند پیدا نشد", color = GolhaColors.SecondaryText)
    }
}

private fun CategoryProgramUiModel.toTvTrackUiModel(detail: ArtistDetailUiModel): TrackUiModel {
    return TrackUiModel(
        id = id,
        artistId = artistId ?: detail.artistId,
        title = title,
        artist = singer.ifBlank { detail.name },
        duration = duration,
        coverUrl = detail.imageUrl,
        audioUrl = audioUrl,
        artistImages = detail.imageUrl?.let { listOf(it) }.orEmpty(),
    )
}

private fun toPersianDigits(value: String): String {
    return value
        .replace("0", "۰")
        .replace("1", "۱")
        .replace("2", "۲")
        .replace("3", "۳")
        .replace("4", "۴")
        .replace("5", "۵")
        .replace("6", "۶")
        .replace("7", "۷")
        .replace("8", "۸")
        .replace("9", "۹")
}
