package com.radiogolha.mobile.ui.player

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
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
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.TimelineSegmentUiModel
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.home.ArtistAvatar
import com.radiogolha.mobile.ui.programs.loadProgramEpisodeDetail
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

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
    onInfoClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onSeekBack10: () -> Unit = {},
    onSeekForward10: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition()
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Reverse)
    )

    // Seekbar State
    var sliderValue by remember { mutableFloatStateOf(currentPositionMs.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }

    // Episode detail: timeline + singer avatars
    var timeline by remember(currentTrack?.id) { mutableStateOf<List<TimelineSegmentUiModel>>(emptyList()) }
    var singerImages by remember(currentTrack?.id) { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(currentTrack?.id) {
        val id = currentTrack?.id ?: return@LaunchedEffect
        val detail = withContext(Dispatchers.Default) { runCatching { loadProgramEpisodeDetail(id) }.getOrNull() }
        timeline = detail?.timeline ?: emptyList()
        singerImages = detail?.singers?.mapNotNull { it.avatar }?.distinct() ?: emptyList()
    }

    // Image Cycle Logic — singer avatars only, fallback to coverUrl
    val images = remember(singerImages, currentTrack) {
        val list = mutableListOf<String>()
        if (singerImages.isNotEmpty()) {
            singerImages.forEach { list.add(it) }
        } else {
            currentTrack?.coverUrl?.let { list.add(it) }
        }
        list.distinct()
    }
    var currentImageIndex by remember(currentTrack) { mutableStateOf(0) }

    if (images.size > 1) {
        LaunchedEffect(images) {
            while (true) {
                delay(7000)
                currentImageIndex = (currentImageIndex + 1) % images.size
            }
        }
    }
    val activeSegment by remember(timeline) {
        derivedStateOf {
            if (timeline.isEmpty()) null
            else timeline.indices.lastOrNull { i ->
                sliderValue >= parsePlayerTimeToMs(timeline[i].startTime)
            }?.let { timeline[it] }
        }
    }

    // Sync state from player periodically or on significant drift
    LaunchedEffect(currentPositionMs) {
        if (!isDragging) {
            val drift = kotlin.math.abs(sliderValue - currentPositionMs.toFloat())
            if (drift > 2000f || !isPlaying) {
                sliderValue = currentPositionMs.toFloat()
            }
        }
    }

    // Local smooth interpolation loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastUpdate = System.currentTimeMillis()
            while (isPlaying) {
                delay(16)
                if (!isDragging) {
                    val now = System.currentTimeMillis()
                    val elapsed = now - lastUpdate
                    sliderValue = (sliderValue + elapsed).coerceAtMost(durationMs.toFloat())
                    lastUpdate = now
                } else {
                    lastUpdate = System.currentTimeMillis()
                }
            }
        }
    }

    GolhaPatternBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, GolhaColors.SoftSand.copy(alpha = 0.5f), GolhaColors.PrimaryAccent.copy(alpha = 0.15f)))))

            Column(
                modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 28.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    IconButton(onClick = onBackClick, modifier = Modifier.background(GolhaColors.Surface.copy(alpha = 0.5f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.Back, modifier = Modifier.size(24.dp), tint = GolhaColors.PrimaryText)
                    }
                    Text(text = "در حال پخش", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp), color = GolhaColors.PrimaryText)
                    IconButton(onClick = onInfoClick, modifier = Modifier.background(GolhaColors.Surface.copy(alpha = 0.5f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.Info, modifier = Modifier.size(24.dp), tint = GolhaColors.PrimaryText)
                    }
                }

                Spacer(modifier = Modifier.weight(0.12f))

                // Artwork Frame
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                    Box(modifier = Modifier.fillMaxSize(0.95f).border(1.dp, GolhaColors.PrimaryAccent.copy(alpha = 0.3f), CircleShape))
                    Box(modifier = Modifier.fillMaxSize(0.88f).border(2.dp, GolhaColors.PrimaryAccent.copy(alpha = 0.6f), CircleShape).shadow(24.dp, CircleShape, spotColor = GolhaColors.PrimaryAccent))

                    Surface(modifier = Modifier.fillMaxSize(0.82f).clip(CircleShape), color = GolhaColors.Surface, shadowElevation = 12.dp) {
                        Box(contentAlignment = Alignment.Center) {
                            if (images.isNotEmpty()) {
                                AnimatedContent(
                                    targetState = images[currentImageIndex],
                                    transitionSpec = {
                                        (fadeIn(animationSpec = tween(1500)) + scaleIn(initialScale = 0.95f, animationSpec = tween(1500)))
                                            .togetherWith(fadeOut(animationSpec = tween(1500)) + scaleOut(targetScale = 1.05f, animationSpec = tween(1500)))
                                    },
                                    modifier = Modifier.fillMaxSize()
                                ) { imageUrl ->
                                    ArtistAvatar(name = currentTrack?.artist ?: "", imageUrl = imageUrl, tint = GolhaColors.SoftBlue, modifier = Modifier.fillMaxSize())
                                }
                            } else {
                                ArtistAvatar(name = currentTrack?.artist ?: "", imageUrl = null, tint = GolhaColors.SoftBlue, modifier = Modifier.fillMaxSize())
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.1f))

                // Track Info
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = currentTrack?.title ?: "قطعه نامشخص", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center), color = GolhaColors.PrimaryText, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Text(text = currentTrack?.artist ?: "هنرمند نامشخص", style = MaterialTheme.typography.titleLarge.copy(color = GolhaColors.SecondaryText, textAlign = TextAlign.Center), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Spacer(modifier = Modifier.weight(0.06f))

                // Custom Seekbar
                Column(modifier = Modifier.fillMaxWidth()) {
                    val maxVal = durationMs.toFloat().coerceAtLeast(1f)
                    val thumbColor = GolhaColors.PrimaryAccent
                    val activeColor = GolhaColors.PrimaryAccent
                    val inactiveColor = GolhaColors.Border.copy(alpha = 0.4f)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(36.dp)
                            .pointerInput(maxVal) {
                                detectTapGestures { offset ->
                                    val fraction = (offset.x / size.width).coerceIn(0f, 1f)
                                    sliderValue = fraction * maxVal
                                    onSeek(sliderValue.toLong())
                                }
                            }
                            .pointerInput(maxVal) {
                                detectHorizontalDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = {
                                        isDragging = false
                                        onSeek(sliderValue.toLong())
                                    },
                                    onHorizontalDrag = { change, _ ->
                                        val fraction = (change.position.x / size.width).coerceIn(0f, 1f)
                                        sliderValue = fraction * maxVal
                                    }
                                )
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val progress = sliderValue.coerceIn(0f, maxVal) / maxVal
                            val trackY = size.height / 2
                            val trackH = 4.dp.toPx()
                            val thumbR = 10.dp.toPx()
                            val thumbX = progress * size.width

                            drawLine(inactiveColor, Offset(0f, trackY), Offset(size.width, trackY), trackH, StrokeCap.Round)
                            if (thumbX > 0f) {
                                drawLine(activeColor, Offset(0f, trackY), Offset(thumbX, trackY), trackH, StrokeCap.Round)
                            }
                            drawCircle(thumbColor, thumbR, Offset(thumbX, trackY))
                            drawCircle(Color.White.copy(alpha = 0.9f), thumbR, Offset(thumbX, trackY), style = Stroke(2.dp.toPx()))
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = formatTime(sliderValue.toLong()), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.PrimaryText.copy(alpha = 0.8f))
                        Text(text = formatTime(durationMs), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = GolhaColors.SecondaryText.copy(alpha = 0.6f))
                    }
                }

                Spacer(modifier = Modifier.weight(0.12f))

                // Controls
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPreviousClick, modifier = Modifier.size(48.dp).background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.SkipPrevious, modifier = Modifier.size(26.dp), tint = GolhaColors.PrimaryText)
                    }
                    IconButton(onClick = onSeekBack10, modifier = Modifier.size(48.dp).background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.SeekBack10, modifier = Modifier.size(26.dp), tint = GolhaColors.PrimaryText)
                    }
                    Box(contentAlignment = Alignment.Center) {
                        Box(modifier = Modifier.size(88.dp).graphicsLayer { alpha = glowAlpha; scaleX = 1.1f; scaleY = 1.1f }.background(GolhaColors.PrimaryAccent.copy(alpha = 0.4f), CircleShape))
                        Surface(modifier = Modifier.size(80.dp).clickable { onTogglePlayback() }, shape = CircleShape, color = GolhaColors.PrimaryAccent, shadowElevation = 12.dp) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isLoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(34.dp), color = GolhaColors.OnAccent, strokeWidth = 3.dp)
                                } else {
                                    GolhaLineIcon(icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play, modifier = Modifier.size(36.dp), tint = GolhaColors.OnAccent)
                                }
                            }
                        }
                    }
                    IconButton(onClick = onSeekForward10, modifier = Modifier.size(48.dp).background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.SeekForward10, modifier = Modifier.size(26.dp), tint = GolhaColors.PrimaryText)
                    }
                    IconButton(onClick = onNextClick, modifier = Modifier.size(48.dp).background(GolhaColors.Surface.copy(alpha = 0.6f), CircleShape)) {
                        GolhaLineIcon(icon = GolhaIcon.SkipNext, modifier = Modifier.size(26.dp), tint = GolhaColors.PrimaryText)
                    }
                }

                Spacer(modifier = Modifier.weight(0.06f))

                // Active Segment (fixed height — always occupies space, fades content in/out)
                Surface(
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(GolhaRadius.Card),
                    color = if (activeSegment != null) GolhaColors.Surface.copy(alpha = 0.7f) else Color.Transparent,
                    border = if (activeSegment != null) androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.5f)) else null,
                ) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = activeSegment != null,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        activeSegment?.let { seg ->
                            Row(
                                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier.size(26.dp).background(GolhaColors.PrimaryAccent.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    GolhaLineIcon(icon = GolhaIcon.Play, modifier = Modifier.size(11.dp), tint = GolhaColors.PrimaryAccent)
                                }
                                Text(
                                    text = buildString {
                                        append(seg.modeName ?: "بخش اجرایی")
                                        if (seg.singers.isNotEmpty()) append("  •  ${seg.singers.joinToString(" و ")}")
                                    },
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                    color = GolhaColors.PrimaryText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f),
                                )
                                Text(
                                    text = seg.startTime ?: "",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = GolhaColors.PrimaryAccent,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(0.09f))
            }
        }
    }
}

private fun parsePlayerTimeToMs(time: String?): Float {
    if (time == null) return 0f
    return try {
        val parts = time.trim().split(":")
        when (parts.size) {
            2 -> (parts[0].toLong() * 60 + parts[1].toLong()) * 1000f
            3 -> (parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()) * 1000f
            else -> 0f
        }
    } catch (e: Exception) { 0f }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = (timeMs / 1000L).coerceAtLeast(0L)
    val h = totalSeconds / 3600L
    val m = (totalSeconds % 3600L) / 60L
    val s = totalSeconds % 60L
    val mStr = m.toString().padStart(2, '0')
    val sStr = s.toString().padStart(2, '0')
    return if (h > 0) "$h:$mStr:$sStr" else "$mStr:$sStr"
}
