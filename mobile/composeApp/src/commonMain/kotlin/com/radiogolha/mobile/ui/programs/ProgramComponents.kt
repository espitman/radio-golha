package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProgramTrackRow(
    track: TrackUiModel,
    isActive: Boolean,
    isPlaying: Boolean,
    onTrackClick: () -> Unit,
    onPlayClick: () -> Unit,
    onArtistClick: (Long) -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(onClick = onTrackClick, onLongClick = onLongClick)
            .background(if (isActive) GolhaColors.BadgeBackground.copy(alpha = 0.42f) else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrackCoverPlaceholder(title = track.title)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = GolhaColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (track.artistId != null) GolhaColors.PrimaryText else GolhaColors.SecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start,
                    modifier = if (track.artistId != null) {
                        Modifier.clickable { onArtistClick(track.artistId) }
                    } else Modifier
                )
            }
        }

        if (!track.duration.isNullOrBlank()) {
            Text(
                text = track.duration,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = GolhaColors.SecondaryText,
            )
        }

        SmallCircularIconButton(
            icon = if (isActive && isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
            iconTint = GolhaColors.PrimaryText,
            iconSize = 18.dp,
            background = GolhaColors.BadgeBackground,
            borderColor = GolhaColors.Border,
            onClick = onPlayClick,
        )
    }
}

@Composable
fun TrackCoverPlaceholder(title: String) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GolhaColors.SoftSand)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = title.take(1),
            style = MaterialTheme.typography.titleLarge,
            color = GolhaColors.SecondaryText,
        )
    }
}

@Composable
fun SmallCircularIconButton(
    icon: GolhaIcon,
    iconTint: Color,
    background: Color,
    borderColor: Color,
    iconSize: androidx.compose.ui.unit.Dp = 14.dp,
    onClick: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = background,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
    ) {
        Box(contentAlignment = Alignment.Center) {
            GolhaLineIcon(
                icon = icon,
                modifier = Modifier.size(iconSize),
                tint = iconTint,
            )
        }
    }
}

@Composable
fun SkeletonTrackRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GolhaColors.Border.copy(alpha = 0.25f))
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
            )
            Box(
                Modifier
                    .fillMaxWidth(0.4f)
                    .height(10.dp)
                    .background(GolhaColors.Border.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(GolhaColors.Border.copy(alpha = 0.2f))
        )
    }
}

@Composable
fun TopTracksSectionSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = GolhaSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(5) {
            SkeletonTrackRow()
        }
    }
}

// Extension to convert CategoryProgramUiModel to TrackUiModel for easy display
fun CategoryProgramUiModel.toTrackUiModel(): TrackUiModel {
    return TrackUiModel(
        id = id,
        artistId = artistId,
        title = title,
        artist = singer,
        duration = duration,
        audioUrl = audioUrl
    )
}
