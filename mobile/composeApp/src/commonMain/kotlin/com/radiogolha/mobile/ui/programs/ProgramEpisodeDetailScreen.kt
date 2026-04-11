package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.*
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.root.TabRootScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.radiogolha.mobile.ui.home.ArtistAvatar

@Composable
fun ProgramEpisodeDetailScreen(
    programId: Long,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
    currentTrack: TrackUiModel? = null,
    isPlayerPlaying: Boolean = false,
    isPlayerLoading: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    currentPlaybackDurationMs: Long = 0L,
    onTogglePlayerPlayback: () -> Unit = {},
    onPlayProgram: (ProgramEpisodeDetailUiModel) -> Unit = {},
) {
    var detail by remember { mutableStateOf<ProgramEpisodeDetailUiModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(programId) {
        isLoading = true
        detail = withContext(Dispatchers.Default) {
             loadProgramEpisodeDetail(programId)
        }
        isLoading = false
    }

    TabRootScreen(
        title = detail?.categoryName ?: "در حال بارگذاری...",
        subtitle = "",
        bottomNavItems = bottomNavItems,
        onBottomNavSelected = onBottomNavSelected,
        currentTrack = currentTrack,
        isPlayerPlaying = isPlayerPlaying,
        isPlayerLoading = isPlayerLoading,
        currentPlaybackPositionMs = currentPlaybackPositionMs,
        currentPlaybackDurationMs = currentPlaybackDurationMs,
        onTogglePlayerPlayback = onTogglePlayerPlayback,
        onBackClick = onBackClick,
        content = {
            if (isLoading) {
                item { DetailSkeleton() }
            } else if (detail == null) {
                item { ErrorState() }
            } else {
                val d = detail!!
                
                // 1. Header Card with Play Button
                item {
                    ProgramHeaderCard(
                        detail = d,
                        isPlaying = currentTrack?.id == d.id && isPlayerPlaying,
                        onPlayClick = { onPlayProgram(d) }
                    )
                }

                // 2. Info Grid (Dastgah, Duration, etc.)
                item {
                    InfoGrid(detail = d)
                }

                // 3. Artists Section (Singers, Musicians)
                if (d.singers.isNotEmpty() || d.performers.isNotEmpty() || d.orchestraLeaders.isNotEmpty()) {
                    item { SectionHeader("هنرمندان") }
                    items(d.singers) { singer ->
                        ArtistRow(name = singer, role = "خواننده")
                    }
                    items(d.orchestraLeaders) { leader ->
                        ArtistRow(name = leader.name, role = "رهبر ارکستر (${leader.orchestra})")
                    }
                    items(d.performers) { performer ->
                        ArtistRow(
                            name = performer.name, 
                            role = performer.instrument ?: "نوازنده",
                            avatar = performer.avatar
                        )
                    }
                }

                // 4. Credits Section (Poets, Composers, etc.)
                if (d.poets.isNotEmpty() || d.composers.isNotEmpty() || d.announcers.isNotEmpty()) {
                    item { SectionHeader("عوامل برنامه") }
                    item {
                        CreditsCard(detail = d)
                    }
                }

                // 5. Transcript/Poetry Section
                if (d.transcript.isNotEmpty()) {
                    item { SectionHeader("متن برنامه و اشعار") }
                    item {
                        TranscriptCard(transcript = d.transcript)
                    }
                }

                // Spacer
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    )
}

@Composable
private fun ProgramHeaderCard(
    detail: ProgramEpisodeDetailUiModel,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
        shadowElevation = GolhaElevation.Card
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "${detail.categoryName} شماره ${detail.no} — ${detail.title}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = GolhaColors.PrimaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.basicMarquee()
            )

            Button(
                onClick = onPlayClick,
                modifier = Modifier.fillMaxWidth(0.7f).height(54.dp),
                shape = RoundedCornerShape(GolhaRadius.Pill),
                colors = ButtonDefaults.buttonColors(containerColor = GolhaColors.PrimaryAccent)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GolhaLineIcon(
                        icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                    Text(
                        text = if (isPlaying) "توقف پخش" else "پخش برنامه",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoGrid(detail: ProgramEpisodeDetailUiModel) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        InfoTile(
            modifier = Modifier.weight(1f),
            label = "دستگاه/آواز",
            value = detail.modes.joinToString("، ").takeIf { it.isNotBlank() } ?: "نامشخص",
            icon = GolhaIcon.Note
        )
        InfoTile(
            modifier = Modifier.weight(1f),
            label = "زمان",
            value = detail.duration ?: "نامشخص",
            icon = GolhaIcon.Timer
        )
    }
}

@Composable
private fun InfoTile(modifier: Modifier, label: String, value: String, icon: GolhaIcon) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = GolhaColors.BadgeBackground.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                GolhaLineIcon(icon = icon, modifier = Modifier.size(14.dp), tint = GolhaColors.SecondaryText)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = GolhaColors.SecondaryText)
            }
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = GolhaColors.Border.copy(alpha = 0.6f))
    }
}

@Composable
private fun ArtistRow(name: String, role: String, avatar: String? = null) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ArtistAvatar(name = name, imageUrl = avatar, tint = GolhaColors.SoftRose)
        Column {
            Text(text = name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), color = GolhaColors.PrimaryText)
            Text(text = role, style = MaterialTheme.typography.labelMedium, color = GolhaColors.SecondaryText)
        }
    }
}

@Composable
private fun CreditsCard(detail: ProgramEpisodeDetailUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (detail.poets.isNotEmpty()) CreditRow("شاعر", detail.poets.joinToString("، "))
            if (detail.composers.isNotEmpty()) CreditRow("آهنگساز", detail.composers.joinToString("، "))
            if (detail.arrangers.isNotEmpty()) CreditRow("تنظیم", detail.arrangers.joinToString("، "))
            if (detail.announcers.isNotEmpty()) CreditRow("گوینده", detail.announcers.joinToString("، "))
        }
    }
}

@Composable
private fun CreditRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$label:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = GolhaColors.SecondaryText)
        Text(text = value, style = MaterialTheme.typography.bodySmall, color = GolhaColors.PrimaryText)
    }
}

@Composable
private fun TranscriptCard(transcript: List<TranscriptVerseUiModel>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            transcript.forEach { verse ->
                Text(
                    text = verse.text,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 28.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = GolhaColors.PrimaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DetailSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(Modifier.fillMaxWidth().height(180.dp).background(GolhaColors.Surface, RoundedCornerShape(GolhaRadius.Card)))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.weight(1f).height(60.dp).background(GolhaColors.Surface, RoundedCornerShape(12.dp)))
            Box(Modifier.weight(1f).height(60.dp).background(GolhaColors.Surface, RoundedCornerShape(12.dp)))
        }
        repeat(3) {
            Box(Modifier.fillMaxWidth().height(50.dp).background(GolhaColors.Surface, RoundedCornerShape(8.dp)))
        }
    }
}

@Composable
private fun ErrorState() {
    Box(Modifier.fillMaxWidth().height(300.dp), contentAlignment = Alignment.Center) {
        Text("خطا در بارگذاری اطلاعات برنامه", color = GolhaColors.SecondaryText)
    }
}

