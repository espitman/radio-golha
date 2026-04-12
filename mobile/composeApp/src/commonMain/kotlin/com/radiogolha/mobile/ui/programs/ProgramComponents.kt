package com.radiogolha.mobile.ui.programs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.radiogolha.mobile.ui.home.*

@Composable
fun CategoryProgramTrackRow(
    program: CategoryProgramUiModel,
    isActive: Boolean,
    isPlaying: Boolean,
    onRowClick: () -> Unit,
    onPlayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .background(if (isActive) GolhaColors.BadgeBackground.copy(alpha = 0.42f) else Color.Transparent)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Placeholder/Number
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(GolhaColors.SoftSand)
                    .border(1.dp, GolhaColors.Border.copy(alpha = 0.6f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = program.programNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GolhaColors.PrimaryAccent,
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = program.singer,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = GolhaColors.PrimaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )
                if (!program.dastgah.isNullOrBlank()) {
                    Text(
                        text = program.dastgah,
                        style = MaterialTheme.typography.bodySmall,
                        color = GolhaColors.SecondaryText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        if (!program.duration.isNullOrBlank()) {
            Text(
                text = program.duration,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = GolhaColors.SecondaryText,
            )
        }

        Surface(
            modifier = Modifier
                .size(38.dp)
                .clickable { onPlayClick() },
            shape = CircleShape,
            color = if (isActive) GolhaColors.PrimaryAccent else GolhaColors.BadgeBackground,
            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
        ) {
            Box(contentAlignment = Alignment.Center) {
                GolhaLineIcon(
                    icon = if (isActive && isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
                    modifier = Modifier.size(18.dp),
                    tint = if (isActive) Color.White else GolhaColors.PrimaryText,
                )
            }
        }
    }
}

@Composable
fun SkeletonTrackRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
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
                .size(38.dp)
                .clip(CircleShape)
                .background(GolhaColors.Border.copy(alpha = 0.2f))
        )
    }
}
