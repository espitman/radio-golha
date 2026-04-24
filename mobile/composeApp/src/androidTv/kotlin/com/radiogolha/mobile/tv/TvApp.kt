package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaPatternBackground

private enum class TvTopMenuItem(val title: String) {
    FavoriteSingers("خواننده‌های مورد علاقه"),
    FavoritePlayers("نوازندگان مورد علاقه"),
    MyPlaylists("پلی لیست‌های من"),
    TopPrograms("محبوب‌ترین برنامه‌ها"),
}

private enum class TvSideMenuItem(val title: String, val icon: ImageVector) {
    Home("صفحه اصلی", Icons.Filled.Home),
    Singers("خواننده‌ها", Icons.Filled.Person),
    Players("نوازندگان", Icons.Filled.PlayArrow),
    Recent("شنیده‌شده‌های اخیر", Icons.Filled.DateRange),
    Search("جستجوی پیشرفته", Icons.Filled.Search),
    Settings("تنظیمات", Icons.Filled.Settings),
    Help("راهنما", Icons.Filled.Info),
}

@Composable
fun TvApp() {
    GolhaAppTheme {
        // Keep the shell geometry predictable, but force content RTL.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            GolhaPatternBackground {
                var selectedTop by remember { mutableStateOf<TvTopMenuItem?>(null) }
                var selectedSide by remember { mutableStateOf(TvSideMenuItem.Home) }

                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val topFocusRequesters = remember {
                        TvTopMenuItem.entries.map { FocusRequester() }
                    }
                    val sidebarFocusRequesters = remember {
                        TvSideMenuItem.entries.map { FocusRequester() }
                    }
                    val playerFocusRequester = remember { FocusRequester() }
                    val mainEntryRequester = topFocusRequesters.first()
                    val sidebarEntryRequester = sidebarFocusRequesters.first()

                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvMainPane(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            selectedTop = selectedTop,
                            onSelectTop = { selectedTop = it; selectedSide = TvSideMenuItem.Home },
                            focusRequesters = topFocusRequesters,
                            playerFocusRequester = playerFocusRequester,
                            sidebarEntryRequester = sidebarEntryRequester,
                        )
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvSidebar(
                            selectedItem = selectedSide,
                            onSelectItem = { item ->
                                selectedSide = item
                                selectedTop = null
                            },
                            focusRequesters = sidebarFocusRequesters,
                            mainEntryRequester = mainEntryRequester,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvMainPane(
    modifier: Modifier = Modifier,
    selectedTop: TvTopMenuItem?,
    onSelectTop: (TvTopMenuItem) -> Unit,
    focusRequesters: List<FocusRequester>,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    Column(
        modifier = modifier
    ) {
        TvTopMenuBar(
            selectedTop = selectedTop,
            onSelect = onSelectTop,
            focusRequesters = focusRequesters,
            playerFocusRequester = playerFocusRequester,
            sidebarEntryRequester = sidebarEntryRequester,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
        )
        Spacer(Modifier.weight(1f))
        TvBottomPlayer(
            focusRequester = playerFocusRequester,
            topEntryRequester = focusRequesters.first(),
            sidebarEntryRequester = sidebarEntryRequester,
        )
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvTopMenuBar(
    selectedTop: TvTopMenuItem?,
    onSelect: (TvTopMenuItem) -> Unit,
    focusRequesters: List<FocusRequester>,
    playerFocusRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TvTopMenuItem.entries.forEachIndexed { index, item ->
            val selected = selectedTop == item
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 11.25.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (selected) GolhaColors.PrimaryText else GolhaColors.SecondaryText,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .focusRequester(focusRequesters[index])
                    .focusProperties {
                        up = FocusRequester.Cancel
                        down = playerFocusRequester
                        if (index == 0) {
                            right = sidebarEntryRequester
                        }
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) GolhaColors.BadgeBackground else Color.Transparent)
                    .border(
                        width = if (selected) 1.dp else 0.dp,
                        color = GolhaColors.Border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onSelect(item) }
                    .focusable()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            )
            Spacer(Modifier.width(10.dp))
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvBottomPlayer(
    focusRequester: FocusRequester,
    topEntryRequester: FocusRequester,
    sidebarEntryRequester: FocusRequester,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(Color(0xFF002045))
        ) {
            TvPlayerSeekBar(progress = 0f)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvEqualizerIndicator(isActive = false)
                    Text(
                        text = "00:00 / 00:00",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(
                        modifier = Modifier
                            .width(1.dp)
                            .height(28.dp)
                            .background(Color.White.copy(alpha = 0.1f))
                    )
                }

                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .width(260.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    TvPlayerCanvasIcon(TvPlayerIconKind.Shuffle, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.BackTen, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    Box(
                        modifier = Modifier
                            .focusRequester(focusRequester)
                            .focusProperties {
                                up = topEntryRequester
                                down = FocusRequester.Cancel
                                right = sidebarEntryRequester
                            }
                            .size(42.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(Color.White)
                            .focusable(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF002045),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.ForwardTen, alpha = 0.35f)
                    Spacer(Modifier.width(18.dp))
                    TvPlayerCanvasIcon(TvPlayerIconKind.Repeat, alpha = 0.35f)
                }

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.width(220.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                        Text(
                            text = "—",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "♪",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TvPlayerSeekBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color.White.copy(alpha = 0.1f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(4.dp)
                .background(Color(0xFFD4AF37))
        )
    }
}

@Composable
private fun TvEqualizerIndicator(isActive: Boolean) {
    val heights = listOf(6.dp, 15.dp, 24.dp, 12.dp, 18.dp, 9.dp)
    Row(
        modifier = Modifier
            .width(36.dp)
            .height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        heights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isActive) {
                            Color(0xFFD4AF37)
                        } else {
                            GolhaColors.SecondaryText.copy(alpha = 0.35f)
                        }
                    )
            )
        }
    }
}

private enum class TvPlayerIconKind {
    Shuffle,
    BackTen,
    ForwardTen,
    Repeat,
}

@Composable
private fun TvPlayerCanvasIcon(
    kind: TvPlayerIconKind,
    alpha: Float,
) {
    val tint = Color.White.copy(alpha = alpha)
    Box(
        modifier = Modifier.size(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.2.dp.toPx()
            val w = size.width
            val h = size.height

            when (kind) {
                TvPlayerIconKind.Shuffle -> {
                    drawLine(tint, Offset(w * 0.16f, h * 0.32f), Offset(w * 0.42f, h * 0.32f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.42f, h * 0.32f), Offset(w * 0.74f, h * 0.68f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.16f, h * 0.68f), Offset(w * 0.42f, h * 0.68f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawLine(tint, Offset(w * 0.42f, h * 0.68f), Offset(w * 0.74f, h * 0.32f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.68f), forward = true, strokeWidth = strokeWidth)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.32f), forward = true, strokeWidth = strokeWidth)
                }
                TvPlayerIconKind.BackTen -> {
                    drawLine(tint, Offset(w * 0.28f, h * 0.24f), Offset(w * 0.28f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawTriangle(
                        tint,
                        Offset(w * 0.72f, h * 0.22f),
                        Offset(w * 0.40f, h * 0.50f),
                        Offset(w * 0.72f, h * 0.78f)
                    )
                }
                TvPlayerIconKind.ForwardTen -> {
                    drawTriangle(
                        tint,
                        Offset(w * 0.28f, h * 0.22f),
                        Offset(w * 0.60f, h * 0.50f),
                        Offset(w * 0.28f, h * 0.78f)
                    )
                    drawLine(tint, Offset(w * 0.72f, h * 0.24f), Offset(w * 0.72f, h * 0.76f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                }
                TvPlayerIconKind.Repeat -> {
                    drawLine(tint, Offset(w * 0.18f, h * 0.36f), Offset(w * 0.78f, h * 0.36f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.82f, h * 0.36f), forward = true, strokeWidth = strokeWidth)
                    drawLine(tint, Offset(w * 0.82f, h * 0.64f), Offset(w * 0.22f, h * 0.64f), strokeWidth = strokeWidth, cap = StrokeCap.Round)
                    drawSmallArrow(tint, Offset(w * 0.18f, h * 0.64f), forward = false, strokeWidth = strokeWidth)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSmallArrow(
    color: Color,
    tip: Offset,
    forward: Boolean,
    strokeWidth: Float,
) {
    val x = if (forward) -1f else 1f
    drawLine(color, tip, Offset(tip.x + x * 4.dp.toPx(), tip.y - 3.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
    drawLine(color, tip, Offset(tip.x + x * 4.dp.toPx(), tip.y + 3.dp.toPx()), strokeWidth = strokeWidth, cap = StrokeCap.Round)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTriangle(
    color: Color,
    first: Offset,
    second: Offset,
    third: Offset,
) {
    drawPath(
        path = Path().apply {
            moveTo(first.x, first.y)
            lineTo(second.x, second.y)
            lineTo(third.x, third.y)
            close()
        },
        color = color
    )
}

@Composable
private fun TvSidebar(
    selectedItem: TvSideMenuItem,
    onSelectItem: (TvSideMenuItem) -> Unit,
    focusRequesters: List<FocusRequester>,
    mainEntryRequester: FocusRequester,
) {
    Column(
        modifier = Modifier
            .width(210.dp)
            .fillMaxHeight()
            .background(Color(0xFFF1EEE5))
            .padding(horizontal = 16.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "رادیو گل‌ها",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            color = GolhaColors.PrimaryText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "میراث موسیقی اصیل ایران",
            style = MaterialTheme.typography.bodySmall,
            color = GolhaColors.SecondaryText,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.height(24.dp))

        TvSideMenuItem.entries.forEachIndexed { index, item ->
            if (item == TvSideMenuItem.Settings) {
                Spacer(modifier = Modifier.weight(1f))
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0x1A1C1C17))
                )
                Spacer(Modifier.height(12.dp))
            }

            TvSidebarItem(
                item = item,
                selected = item == selectedItem,
                onClick = { onSelectItem(item) },
                focusRequester = focusRequesters[index],
                isFirst = index == 0,
                isLast = index == TvSideMenuItem.entries.lastIndex,
                mainEntryRequester = mainEntryRequester,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun TvSidebarItem(
    item: TvSideMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
    focusRequester: FocusRequester,
    isFirst: Boolean,
    isLast: Boolean,
    mainEntryRequester: FocusRequester,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .focusProperties {
                left = mainEntryRequester
                right = FocusRequester.Cancel
                if (isFirst) {
                    up = FocusRequester.Cancel
                }
                if (isLast) {
                    down = FocusRequester.Cancel
                }
            }
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color(0xFFE8E2D1) else Color.Transparent)
            .clickable { onClick() }
            .focusable()
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (selected) GolhaColors.PrimaryText else Color(0xFF555555),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 11.25.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (selected) GolhaColors.PrimaryText else Color(0xFF333333),
            textAlign = TextAlign.Start
        )
    }
    Spacer(Modifier.height(6.dp))
}
