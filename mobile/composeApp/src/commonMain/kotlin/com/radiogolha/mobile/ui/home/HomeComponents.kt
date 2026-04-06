package com.radiogolha.mobile.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.theme.GolhaTypographyTokens
import kotlin.math.min

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GolhaSpacing.ScreenHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = GolhaColors.PrimaryText,
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = GolhaColors.Border,
            thickness = 1.dp,
        )
    }
}

@Composable
fun HeaderSection(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = GolhaSpacing.ScreenHorizontal),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "گل‌ها",
            style = MaterialTheme.typography.headlineLarge,
            color = GolhaColors.PrimaryText,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(GolhaSpacing.Small),
        ) {
            CircularActionButton(icon = GolhaIcon.Favorites)
            CircularActionButton(icon = GolhaIcon.Profile)
        }
    }
}

// Premium hero banner with a restrained Persian visual accent.
@Composable
fun HeroBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .requiredHeight(220.dp),
        shape = RoundedCornerShape(0.dp),
        color = GolhaColors.BannerBackground,
        tonalElevation = GolhaElevation.Banner,
        shadowElevation = GolhaElevation.Banner,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(GolhaColors.BannerBackground)

                    drawCircle(
                        color = GolhaColors.BannerDetail.copy(alpha = 0.08f),
                        radius = size.minDimension * 0.42f,
                        center = Offset(size.width * 0.18f, size.height * 0.20f),
                    )
                    drawCircle(
                        color = GolhaColors.BannerDetail.copy(alpha = 0.07f),
                        radius = size.minDimension * 0.33f,
                        center = Offset(size.width * 0.88f, size.height * 0.84f),
                    )

                    val swirl = Path().apply {
                        moveTo(size.width * 0.12f, size.height * 0.22f)
                        cubicTo(
                            size.width * 0.28f,
                            size.height * 0.04f,
                            size.width * 0.52f,
                            size.height * 0.18f,
                            size.width * 0.62f,
                            size.height * 0.08f,
                        )
                        cubicTo(
                            size.width * 0.74f,
                            size.height * 0.00f,
                            size.width * 0.78f,
                            size.height * 0.16f,
                            size.width * 0.92f,
                            size.height * 0.14f,
                        )
                    }
                    drawPath(
                        path = swirl,
                        color = GolhaColors.BannerDetail.copy(alpha = 0.12f),
                        style = Stroke(width = 2.2.dp.toPx(), cap = StrokeCap.Round),
                    )

                    repeat(5) { index ->
                        val x = size.width * (0.18f + index * 0.12f)
                        drawCircle(
                            color = GolhaColors.BannerDetail.copy(alpha = 0.09f),
                            radius = 4.dp.toPx(),
                            center = Offset(x, size.height * 0.76f),
                        )
                    }
                }
                .padding(horizontal = 22.dp, vertical = 20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "گل‌های رنگارنگ",
                        style = MaterialTheme.typography.displaySmall,
                        color = GolhaColors.PrimaryText,
                    )
                    Text(
                        text = "برنامه‌ای خاطره‌انگیز",
                        modifier = Modifier.padding(top = 10.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )

                    Row(
                        modifier = Modifier.padding(top = 22.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayPillButton()
                    }
                }

                VintageRadioIllustration()
            }
        }
    }
}

// Programs are intentionally simple white cards with no iconography.
@Composable
fun ProgramsSection(programs: List<ProgramUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "برنامه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(GolhaSpacing.CardGap),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(programs) { program ->
                ProgramCard(
                    item = program,
                    modifier = Modifier.widthIn(min = 128.dp, max = 164.dp),
                )
            }
        }
    }
}

@Composable
fun SingersSection(singers: List<SingerUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "خواننده‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(singers) { singer ->
                AvatarNameItem(
                    title = singer.name,
                    subtitle = null,
                    imageUrl = singer.imageUrl,
                    tint = GolhaColors.SoftBlue,
                )
            }
        }
    }
}

@Composable
fun DastgahSection(items: List<DastgahUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "دستگاه‌ها")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(items) { item ->
                DastgahChip(name = item.name)
            }
        }
    }
}

@Composable
fun MusiciansSection(musicians: List<MusicianUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)) {
        SectionTitle(title = "نوازندگان برجسته")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            contentPadding = PaddingValues(horizontal = GolhaSpacing.ScreenHorizontal),
        ) {
            items(musicians) { musician ->
                AvatarNameItem(
                    title = musician.name,
                    subtitle = musician.instrument,
                    imageUrl = musician.imageUrl,
                    tint = GolhaColors.SoftRose,
                )
            }
        }
    }
}

@Composable
fun TopTracksSection(tracks: List<TrackUiModel>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(GolhaSpacing.Large)
    ) {
        SectionTitle(title = "ترک‌های برتر")
        
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = GolhaSpacing.ScreenHorizontal),
            ) {
                tracks.forEachIndexed { index, track ->
                    TrackRow(track = track)
                    if (index != tracks.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = GolhaColors.Border.copy(alpha = 0.65f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(items: List<BottomNavItemUiModel>) {
    Surface(
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        color = GolhaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border.copy(alpha = 0.8f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items.forEach { item ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BottomNavItem(item = item)
                }
            }
        }
    }
}

@Composable
private fun CircularActionButton(icon: GolhaIcon) {
    Surface(
        modifier = Modifier.size(42.dp),
        shape = CircleShape,
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Box(contentAlignment = Alignment.Center) {
            GolhaLineIcon(
                icon = icon,
                modifier = Modifier.size(19.dp),
                tint = GolhaColors.PrimaryText,
            )
        }
    }
}

@Composable
private fun PlayPillButton() {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Pill),
        color = GolhaColors.PrimaryAccent,
        shadowElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GolhaLineIcon(
                icon = GolhaIcon.Play,
                modifier = Modifier.size(16.dp),
                tint = GolhaColors.Surface,
            )
            Text(
                text = "پخش",
                color = GolhaColors.Surface,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun VintageRadioIllustration() {
    Box(
        modifier = Modifier
            .size(width = 136.dp, height = 146.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(130.dp, 118.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(GolhaColors.Surface.copy(alpha = 0.72f))
                .border(
                    width = 1.dp,
                    color = GolhaColors.BannerDetail.copy(alpha = 0.26f),
                    shape = RoundedCornerShape(30.dp),
                ),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = GolhaColors.SoftSand,
                    size = size,
                    cornerRadius = CornerRadius(30.dp.toPx(), 30.dp.toPx()),
                )
                drawRoundRect(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.24f),
                    topLeft = Offset(size.width * 0.15f, size.height * 0.26f),
                    size = Size(size.width * 0.38f, size.height * 0.38f),
                    cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                    style = Fill,
                )
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.30f),
                    radius = size.minDimension * 0.12f,
                    center = Offset(size.width * 0.73f, size.height * 0.38f),
                )
                drawCircle(
                    color = GolhaColors.BannerDetail.copy(alpha = 0.22f),
                    radius = size.minDimension * 0.07f,
                    center = Offset(size.width * 0.84f, size.height * 0.38f),
                )

                val lineColor = GolhaColors.BannerDetail.copy(alpha = 0.42f)
                repeat(4) { index ->
                    val y = size.height * (0.68f + (index * 0.06f))
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width * 0.18f, y),
                        end = Offset(size.width * 0.82f, y),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round,
                    )
                }

                drawLine(
                    color = lineColor,
                    start = Offset(size.width * 0.28f, size.height * 0.10f),
                    end = Offset(size.width * 0.52f, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
                drawLine(
                    color = lineColor,
                    start = Offset(size.width * 0.72f, size.height * 0.10f),
                    end = Offset(size.width * 0.48f, 0f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}

@Composable
private fun ProgramCard(item: ProgramUiModel, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.episodeCount} قسمت",
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
            )
        }
    }
}

@Composable
private fun AvatarNameItem(
    title: String,
    subtitle: String?,
    imageUrl: String?,
    tint: Color,
) {
    Column(
        modifier = Modifier.widthIn(min = 76.dp, max = 88.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        ArtistAvatar(
            name = title,
            imageUrl = imageUrl,
            tint = tint,
            modifier = Modifier.size(78.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
            textAlign = TextAlign.Center,
            color = GolhaColors.SecondaryText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = GolhaColors.SecondaryText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
internal fun AvatarPlaceholder(
    name: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    val monogramParts = rememberMonogramParts(name)

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(tint)
            .border(1.dp, GolhaColors.Border.copy(alpha = 0.65f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        if (monogramParts.size > 1) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                monogramParts.forEach { part ->
                    Text(
                        text = part,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = GolhaColors.PrimaryText.copy(alpha = 0.85f),
                    )
                }
            }
        } else {
            Text(
                text = monogramParts.firstOrNull().orEmpty(),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = GolhaColors.PrimaryText.copy(alpha = 0.85f),
            )
        }
    }
}

private fun rememberMonogramParts(name: String): List<String> {
    val parts = name
        .split(" ")
        .map { it.trim() }
        .filter { it.isNotEmpty() }

    return when {
        parts.size >= 2 -> listOf(parts.first().take(1), parts.last().take(1))
        parts.isNotEmpty() -> listOf(parts.first().take(2))
        else -> emptyList()
    }
}

@Composable
private fun DastgahChip(name: String) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Pill),
        color = GolhaColors.BadgeBackground,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = GolhaColors.PrimaryText,
        )
    }
}

@Composable
private fun TrackRow(track: TrackUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                )
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = GolhaColors.SecondaryText,
                    maxLines = 1,
                )
            }
        }

        Text(
            text = track.duration,
            style = MaterialTheme.typography.labelMedium,
            color = GolhaColors.SecondaryText,
        )

        SmallCircularIconButton(
            icon = GolhaIcon.Play,
            iconTint = GolhaColors.PrimaryText,
            iconSize = 18.dp,
            background = GolhaColors.BadgeBackground,
            borderColor = GolhaColors.Border,
        )
    }
}

@Composable
private fun TrackCoverPlaceholder(title: String) {
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
private fun SmallCircularIconButton(
    icon: GolhaIcon,
    iconTint: Color,
    background: Color,
    borderColor: Color,
    iconSize: androidx.compose.ui.unit.Dp = 14.dp,
) {
    Surface(
        modifier = Modifier.size(36.dp),
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
private fun BottomNavItem(item: BottomNavItemUiModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        GolhaLineIcon(
            icon = item.icon,
            modifier = Modifier.size(26.dp),
            tint = if (item.selected) GolhaColors.PrimaryAccent else GolhaColors.SecondaryText,
        )
        Text(
            text = item.label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (item.selected) FontWeight.Medium else FontWeight.Normal
            ),
            color = if (item.selected) GolhaColors.PrimaryAccent else GolhaColors.SecondaryText,
        )
    }
}

@Composable
fun GolhaLineIcon(
    icon: GolhaIcon,
    modifier: Modifier = Modifier,
    tint: Color,
) {
    Canvas(modifier = modifier) {
        val stroke = 1.9.dp.toPx()
        when (icon) {
            GolhaIcon.Favorites -> {
                val heart = Path().apply {
                    moveTo(size.width * 0.50f, size.height * 0.82f)
                    cubicTo(
                        size.width * 0.18f,
                        size.height * 0.60f,
                        size.width * 0.14f,
                        size.height * 0.26f,
                        size.width * 0.35f,
                        size.height * 0.22f,
                    )
                    cubicTo(
                        size.width * 0.48f,
                        size.height * 0.22f,
                        size.width * 0.50f,
                        size.height * 0.34f,
                        size.width * 0.50f,
                        size.height * 0.34f,
                    )
                    cubicTo(
                        size.width * 0.50f,
                        size.height * 0.34f,
                        size.width * 0.52f,
                        size.height * 0.22f,
                        size.width * 0.65f,
                        size.height * 0.22f,
                    )
                    cubicTo(
                        size.width * 0.86f,
                        size.height * 0.26f,
                        size.width * 0.82f,
                        size.height * 0.60f,
                        size.width * 0.50f,
                        size.height * 0.82f,
                    )
                    close()
                }
                drawPath(heart, tint, style = Stroke(width = stroke))
            }

            GolhaIcon.Profile,
            GolhaIcon.Account -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.20f,
                    center = Offset(size.width * 0.50f, size.height * 0.30f),
                    style = Stroke(width = stroke),
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.20f, size.height * 0.54f),
                    size = Size(size.width * 0.60f, size.height * 0.22f),
                    cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    style = Stroke(width = stroke),
                )
            }

            GolhaIcon.Home -> {
                val roof = Path().apply {
                    moveTo(size.width * 0.16f, size.height * 0.48f)
                    lineTo(size.width * 0.50f, size.height * 0.16f)
                    lineTo(size.width * 0.84f, size.height * 0.48f)
                }
                drawPath(roof, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(size.width * 0.26f, size.height * 0.46f),
                    size = Size(size.width * 0.48f, size.height * 0.34f),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                    style = Stroke(width = stroke),
                )
            }

            GolhaIcon.Search -> {
                drawCircle(
                    color = tint,
                    radius = size.minDimension * 0.24f,
                    center = Offset(size.width * 0.42f, size.height * 0.42f),
                    style = Stroke(width = stroke),
                )
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.58f, size.height * 0.58f),
                    end = Offset(size.width * 0.82f, size.height * 0.82f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }

            GolhaIcon.Library -> {
                repeat(3) { index ->
                    val y = size.height * (0.24f + index * 0.22f)
                    drawRoundRect(
                        color = tint,
                        topLeft = Offset(size.width * 0.20f, y),
                        size = Size(size.width * 0.60f, size.height * 0.12f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                        style = Stroke(width = stroke),
                    )
                }
            }

            GolhaIcon.Play -> {
                val triangle = Path().apply {
                    moveTo(size.width * 0.32f, size.height * 0.24f)
                    lineTo(size.width * 0.75f, size.height * 0.50f)
                    lineTo(size.width * 0.32f, size.height * 0.76f)
                    close()
                }
                drawPath(triangle, tint)
            }

            GolhaIcon.More -> {
                repeat(3) { index ->
                    drawCircle(
                        color = tint,
                        radius = size.minDimension * 0.08f,
                        center = Offset(size.width * (0.28f + index * 0.22f), size.height * 0.50f),
                    )
                }
            }

            GolhaIcon.Download -> {
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.50f, size.height * 0.18f),
                    end = Offset(size.width * 0.50f, size.height * 0.60f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
                val arrow = Path().apply {
                    moveTo(size.width * 0.34f, size.height * 0.48f)
                    lineTo(size.width * 0.50f, size.height * 0.66f)
                    lineTo(size.width * 0.66f, size.height * 0.48f)
                }
                drawPath(arrow, tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
                drawLine(
                    color = tint,
                    start = Offset(size.width * 0.24f, size.height * 0.78f),
                    end = Offset(size.width * 0.76f, size.height * 0.78f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
