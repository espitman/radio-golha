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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.DarkPatternBackground
import com.radiogolha.mobile.theme.GolhaColors
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

private val DarkBg = Color(0xFF0A1628)
private val DarkSurface = Color(0xFF132039)
private val Gold = Color(0xFFD4A843)
private val GoldDim = Color(0xFF8B7435)
private val TextWhite = Color(0xFFF0ECE3)
private val TextDim = Color(0xFF8A95A8)

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
    onVisibilityChanged: (Boolean) -> Unit = {},
) {
    DisposableEffect(Unit) {
        onVisibilityChanged(true)
        onDispose { onVisibilityChanged(false) }
    }
    // Seekbar State
    var sliderValue by remember { mutableFloatStateOf(currentPositionMs.toFloat()) }
    var isDragging by remember { mutableStateOf(false) }

    // Episode detail
    var timeline by remember(currentTrack?.id) { mutableStateOf<List<TimelineSegmentUiModel>>(emptyList()) }
    var singerImages by remember(currentTrack?.id) { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(currentTrack?.id) {
        val id = currentTrack?.id ?: return@LaunchedEffect
        val detail: com.radiogolha.mobile.ui.home.ProgramEpisodeDetailUiModel? = withContext(Dispatchers.Default) { 
            runCatching { loadProgramEpisodeDetail(id) }.getOrNull() 
        }
        timeline = detail?.timeline ?: emptyList()
        singerImages = detail?.singers?.mapNotNull { it.avatar }?.distinct() ?: emptyList()
    }

    val images = remember(singerImages, currentTrack) {
        val list = mutableListOf<String>()
        if (singerImages.isNotEmpty()) singerImages.forEach { list.add(it) }
        else currentTrack?.coverUrl?.let { list.add(it) }
        list.distinct()
    }
    var currentImageIndex by remember(currentTrack) { mutableStateOf(0) }
    if (images.size > 1) {
        LaunchedEffect(images) {
            while (true) { delay(7000); currentImageIndex = (currentImageIndex + 1) % images.size }
        }
    }

    val activeSegment by remember(timeline) {
        derivedStateOf {
            if (timeline.isEmpty()) null
            else timeline.indices.lastOrNull { sliderValue >= parsePlayerTimeToMs(timeline[it].startTime) }?.let { timeline[it] }
        }
    }

    LaunchedEffect(currentPositionMs) {
        if (!isDragging) {
            val drift = kotlin.math.abs(sliderValue - currentPositionMs.toFloat())
            if (drift > 2000f || !isPlaying) sliderValue = currentPositionMs.toFloat()
        }
    }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastUpdate = com.radiogolha.mobile.currentTimeMillis()
            while (isPlaying) {
                delay(16)
                if (!isDragging) {
                    val now = com.radiogolha.mobile.currentTimeMillis()
                    sliderValue = (sliderValue + (now - lastUpdate)).coerceAtMost(durationMs.toFloat())
                    lastUpdate = now
                } else lastUpdate = com.radiogolha.mobile.currentTimeMillis()
            }
        }
    }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "player")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(6000, easing = LinearEasing)),
        label = "ringRot",
    )
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(2500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow",
    )
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .drawBehind {
                // Ambient glow behind artwork
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Gold.copy(alpha = glowPulse), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.32f),
                        radius = size.width * 0.5f,
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.5f, size.height * 0.32f),
                )
            }
    ) {
        // Eslimi pattern overlay
        DarkPatternBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                    GolhaLineIcon(icon = GolhaIcon.Back, modifier = Modifier.size(22.dp), tint = TextDim)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("در حال پخش", style = MaterialTheme.typography.labelSmall, color = TextDim)
                }
                IconButton(onClick = onInfoClick, modifier = Modifier.size(40.dp)) {
                    GolhaLineIcon(icon = GolhaIcon.Info, modifier = Modifier.size(22.dp), tint = TextDim)
                }
            }

            Spacer(modifier = Modifier.weight(0.08f))

            // Vinyl Artwork
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth(0.75f).aspectRatio(1f)) {
                // Outer rotating ring
                Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { rotationZ = ringRotation }) {
                    val stroke = 1.5.dp.toPx()
                    val r = (size.minDimension - stroke) / 2f
                    drawCircle(Gold.copy(alpha = 0.15f), r, style = Stroke(stroke))
                    drawArc(Gold.copy(alpha = 0.7f), 0f, 120f, false, style = Stroke(stroke, cap = StrokeCap.Round), topLeft = Offset(stroke / 2, stroke / 2), size = Size(size.width - stroke, size.height - stroke))
                }

                // Avatar
                Surface(
                    modifier = Modifier.fillMaxSize(0.85f).clip(CircleShape),
                    color = DarkSurface,
                    shadowElevation = 24.dp,
                ) {
                    if (images.isNotEmpty()) {
                        AnimatedContent(
                            targetState = images[currentImageIndex],
                            transitionSpec = {
                                (fadeIn(tween(1500)) + scaleIn(initialScale = 0.95f, animationSpec = tween(1500)))
                                    .togetherWith(fadeOut(tween(1500)) + scaleOut(targetScale = 1.05f, animationSpec = tween(1500)))
                            },
                            modifier = Modifier.fillMaxSize(),
                        ) { imageUrl ->
                            ArtistAvatar(name = currentTrack?.artist ?: "", imageUrl = imageUrl, tint = GolhaColors.SoftBlue, modifier = Modifier.fillMaxSize())
                        }
                    } else {
                        ArtistAvatar(name = currentTrack?.artist ?: "", imageUrl = null, tint = GolhaColors.SoftBlue, modifier = Modifier.fillMaxSize())
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.06f))

            // Track Info
            Text(
                text = currentTrack?.title ?: "قطعه نامشخص",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                color = TextWhite,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = currentTrack?.artist ?: "هنرمند نامشخص",
                style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                color = Gold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.weight(0.05f))

            // Seekbar
            PlayerSeekbar(
                value = sliderValue,
                maxValue = durationMs.toFloat().coerceAtLeast(1f),
                isDragging = isDragging,
                onDragStart = { isDragging = true },
                onDragEnd = { isDragging = false; onSeek(sliderValue.toLong()) },
                onValueChange = { sliderValue = it },
                onTap = { sliderValue = it; onSeek(it.toLong()) },
            )
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(formatTime(sliderValue.toLong()), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = TextDim)
                Text(formatTime(durationMs), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = TextDim)
            }

            Spacer(modifier = Modifier.weight(0.05f))

            // Controls
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                ControlButton(icon = GolhaIcon.SkipPrevious, size = 44.dp, iconSize = 22.dp, onClick = onPreviousClick)
                IconButton(onClick = onSeekBack10, modifier = Modifier.size(44.dp)) {
                    Icon(Replay10Icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(26.dp))
                }

                // Play/Pause
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(76.dp)) {
                    Box(modifier = Modifier.size(76.dp).graphicsLayer { alpha = if (isPlaying) glowPulse * 1.5f else 0f }.background(Gold.copy(alpha = 0.3f), CircleShape))
                    Surface(
                        modifier = Modifier.size(68.dp).clickable { onTogglePlayback() },
                        shape = CircleShape,
                        color = Gold,
                        shadowElevation = 8.dp,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(28.dp), color = DarkBg, strokeWidth = 2.5.dp)
                            } else {
                                GolhaLineIcon(icon = if (isPlaying) GolhaIcon.Pause else GolhaIcon.Play, modifier = Modifier.size(30.dp), tint = DarkBg)
                            }
                        }
                    }
                }

                IconButton(onClick = onSeekForward10, modifier = Modifier.size(44.dp)) {
                    Icon(Forward10Icon, contentDescription = null, tint = TextWhite, modifier = Modifier.size(26.dp))
                }
                ControlButton(icon = GolhaIcon.SkipNext, size = 44.dp, iconSize = 22.dp, onClick = onNextClick)
            }

            Spacer(modifier = Modifier.weight(0.04f))

            // Active Segment
            AnimatedVisibility(
                visible = activeSegment != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut(),
            ) {
                activeSegment?.let { seg ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = DarkSurface,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.15f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Box(modifier = Modifier.size(24.dp).background(Gold.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                GolhaLineIcon(icon = GolhaIcon.Play, modifier = Modifier.size(10.dp), tint = Gold)
                            }
                            Text(
                                text = buildString {
                                    append(seg.modeName ?: "بخش اجرایی")
                                    if (seg.singers.isNotEmpty()) append("  •  ${seg.singers.joinToString(" و ")}")
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = TextWhite.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            Text(seg.startTime ?: "", style = MaterialTheme.typography.labelSmall, color = Gold)
                        }
                    }
                }
            }
            if (activeSegment == null) Spacer(modifier = Modifier.height(44.dp))

            Spacer(modifier = Modifier.weight(0.06f))
        }
    }
}

@Composable
private fun ControlButton(icon: GolhaIcon, size: androidx.compose.ui.unit.Dp, iconSize: androidx.compose.ui.unit.Dp, onClick: () -> Unit) {
    IconButton(onClick = onClick, modifier = Modifier.size(size)) {
        GolhaLineIcon(icon = icon, modifier = Modifier.size(iconSize), tint = TextWhite)
    }
}

@Composable
private fun PlayerSeekbar(
    value: Float, maxValue: Float, isDragging: Boolean,
    onDragStart: () -> Unit, onDragEnd: () -> Unit, onValueChange: (Float) -> Unit, onTap: (Float) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp)
            .pointerInput(maxValue) {
                detectTapGestures { offset ->
                    val frac = (offset.x / size.width).coerceIn(0f, 1f)
                    onTap(frac * maxValue)
                }
            }
            .pointerInput(maxValue) {
                detectHorizontalDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onHorizontalDrag = { change, _ ->
                        val frac = (change.position.x / size.width).coerceIn(0f, 1f)
                        onValueChange(frac * maxValue)
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val progress = value.coerceIn(0f, maxValue) / maxValue
            val y = size.height / 2
            val trackH = 3.dp.toPx()
            val thumbR = if (isDragging) 8.dp.toPx() else 6.dp.toPx()
            val thumbX = progress * size.width

            // Track background
            drawLine(TextDim.copy(alpha = 0.2f), Offset(0f, y), Offset(size.width, y), trackH, StrokeCap.Round)
            // Active track
            if (thumbX > 0f) {
                drawLine(
                    brush = Brush.horizontalGradient(listOf(GoldDim, Gold)),
                    start = Offset(0f, y), end = Offset(thumbX, y),
                    strokeWidth = trackH, cap = StrokeCap.Round,
                )
            }
            // Thumb
            drawCircle(Gold, thumbR, Offset(thumbX, y))
            if (isDragging) {
                drawCircle(Gold.copy(alpha = 0.2f), thumbR * 2.5f, Offset(thumbX, y))
            }
        }
    }
}

private val Replay10Icon: ImageVector by lazy {
    val pathData = PathParser().parsePathString(
        "M12 5V1L7 6l5 5V7c3.31 0 6 2.69 6 6s-2.69 6-6 6-6-2.69-6-6H4c0 4.42 3.58 8 8 8s8-3.58 8-8-3.58-8-8-8z"
    ).toNodes()
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply { addPath(pathData, fill = SolidColor(Color.Black)) }
        .build()
}

private val Forward10Icon: ImageVector by lazy {
    val pathData = PathParser().parsePathString(
        "M18 13c0 3.31-2.69 6-6 6s-6-2.69-6-6 2.69-6 6-6v4l5-5-5-5v4c-4.42 0-8 3.58-8 8s3.58 8 8 8 8-3.58 8-8h-2z"
    ).toNodes()
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
        .apply { addPath(pathData, fill = SolidColor(Color.Black)) }
        .build()
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
