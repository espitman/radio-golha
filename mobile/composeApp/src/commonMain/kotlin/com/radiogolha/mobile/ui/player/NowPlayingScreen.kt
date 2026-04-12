package com.radiogolha.mobile.ui.player

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.home.ArtistAvatar

@Composable
fun NowPlayingScreen(
    currentTrack: TrackUiModel?,
    isPlaying: Boolean,
    isLoading: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    GolhaPatternBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Gradient Overlay for Depth
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Transparent,
                                GolhaColors.SoftSand.copy(alpha = 0.5f),
                                GolhaColors.PrimaryAccent.copy(alpha = 0.15f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 28.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.background(GolhaColors.Surface.copy(alpha = 0.5f), CircleShape)
                    ) {
                        GolhaLineIcon(
                            icon = GolhaIcon.Back,
                            modifier = Modifier.size(24.dp),
                            tint = GolhaColors.PrimaryText
                        )
                    }
                    
                    Text(
                        text = "در حال پخش",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        ),
                        color = GolhaColors.PrimaryText
                    )

                    IconButton(
                        onClick = { /* More options */ },
                        modifier = Modifier.background(GolhaColors.Surface.copy(alpha = 0.5f), CircleShape)
                    ) {
                        GolhaLineIcon(
                            icon = GolhaIcon.More,
                            modifier = Modifier.size(24.dp),
                            tint = GolhaColors.PrimaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.15f))

                // Premium Artwork Frame
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth().aspectRatio(1f)
                ) {
                    // Decorative Rings
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.95f)
                            .border(1.dp, GolhaColors.PrimaryAccent.copy(alpha = 0.3f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize(0.88f)
                            .border(2.dp, GolhaColors.PrimaryAccent.copy(alpha = 0.6f), CircleShape)
                            .shadow(24.dp, CircleShape, spotColor = GolhaColors.PrimaryAccent)
                    )

                    // Main Artwork
                    Surface(
                        modifier = Modifier
                            .fillMaxSize(0.82f)
                            .clip(CircleShape),
                        color = GolhaColors.Surface,
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (currentTrack != null) {
                                ArtistAvatar(
                                    name = currentTrack.artist,
                                    imageUrl = currentTrack.coverUrl,
                                    tint = GolhaColors.SoftBlue,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.radialGradient(
                                                listOf(GolhaColors.SoftSand, GolhaColors.Surface)
                                            )
                                        )
                                ) {
                                    GolhaLineIcon(
                                        icon = GolhaIcon.Note,
                                        modifier = Modifier.size(80.dp).align(Alignment.Center),
                                        tint = GolhaColors.PrimaryAccent.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.12f))

                // Track info with improved typography
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = currentTrack?.title ?: "قطعه نامشخص",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            lineHeight = 32.sp
                        ),
                        color = GolhaColors.PrimaryText,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentTrack?.artist ?: "هنرمند نامشخص",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = GolhaColors.SecondaryText,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(0.12f))

                // Premium Seek Bar
                var sliderValue by remember(currentPositionMs) { mutableFloatStateOf(currentPositionMs.toFloat()) }
                var isDragging by remember { mutableStateOf(false) }

                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = if (isDragging) sliderValue else currentPositionMs.toFloat(),
                        onValueChange = {
                            isDragging = true
                            sliderValue = it
                        },
                        onValueChangeFinished = {
                            isDragging = false
                            onSeek(sliderValue.toLong())
                        },
                        valueRange = 0f..durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = GolhaColors.PrimaryAccent,
                            activeTrackColor = GolhaColors.PrimaryAccent,
                            inactiveTrackColor = GolhaColors.Border.copy(alpha = 0.4f),
                            activeTickColor = Color.Transparent,
                            inactiveTickColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(if (isDragging) sliderValue.toLong() else currentPositionMs),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                            ),
                            color = GolhaColors.PrimaryText.copy(alpha = 0.7f)
                        )
                        Text(
                            text = formatTime(durationMs),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
                            ),
                            color = GolhaColors.SecondaryText.copy(alpha = 0.6f)
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.15f))

                // Controls with depth and animation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous
                    IconButton(
                        onClick = onPreviousClick,
                        modifier = Modifier
                            .size(52.dp)
                            .background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)
                    ) {
                        GolhaLineIcon(
                            icon = GolhaIcon.SkipPrevious,
                            modifier = Modifier.size(28.dp),
                            tint = GolhaColors.PrimaryText
                        )
                    }

                    // Play/Pause with Glow
                    Box(contentAlignment = Alignment.Center) {
                        // Ambient Glow
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .graphicsLayer {
                                    alpha = glowAlpha
                                    scaleX = 1.1f
                                    scaleY = 1.1f
                                }
                                .background(GolhaColors.PrimaryAccent.copy(alpha = 0.4f), CircleShape)
                        )

                        Surface(
                            modifier = Modifier
                                .size(88.dp)
                                .clickable { onTogglePlayback() },
                            shape = CircleShape,
                            color = GolhaColors.PrimaryAccent,
                            shadowElevation = 12.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(38.dp),
                                        color = GolhaColors.OnAccent,
                                        strokeWidth = 3.dp
                                    )
                                } else {
                                    GolhaLineIcon(
                                        icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play,
                                        modifier = Modifier.size(40.dp).graphicsLayer {
                                            // Subtle animation on click or state change could go here
                                        },
                                        tint = GolhaColors.OnAccent
                                    )
                                }
                            }
                        }
                    }

                    // Next
                    IconButton(
                        onClick = onNextClick,
                        modifier = Modifier
                            .size(52.dp)
                            .background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)
                    ) {
                        GolhaLineIcon(
                            icon = GolhaIcon.SkipNext,
                            modifier = Modifier.size(28.dp),
                            tint = GolhaColors.PrimaryText
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.18f))
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}
