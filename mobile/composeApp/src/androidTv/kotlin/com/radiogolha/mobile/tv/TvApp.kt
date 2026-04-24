package com.radiogolha.mobile.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvMainPane(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(1f),
                            selectedTop = selectedTop,
                            onSelectTop = { selectedTop = it; selectedSide = TvSideMenuItem.Home },
                        )
                    }
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        TvSidebar(
                            selectedItem = selectedSide,
                            onSelectItem = { item ->
                                selectedSide = item
                                selectedTop = null
                            }
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
) {
    Column(
        modifier = modifier
            .padding(horizontal = 24.dp, vertical = 18.dp)
    ) {
        TvTopMenuBar(selectedTop = selectedTop, onSelect = onSelectTop)
    }
}

@Composable
private fun TvTopMenuBar(
    selectedTop: TvTopMenuItem?,
    onSelect: (TvTopMenuItem) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        TvTopMenuItem.entries.forEach { item ->
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
private fun TvSidebar(
    selectedItem: TvSideMenuItem,
    onSelectItem: (TvSideMenuItem) -> Unit
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

        TvSideMenuItem.entries.forEach { item ->
            if (item == TvSideMenuItem.Settings) {
                Spacer(modifier = Modifier.weight(1f))
            }

            TvSidebarItem(
                item = item,
                selected = item == selectedItem,
                onClick = { onSelectItem(item) },
            )
        }
    }
}

@Composable
private fun TvSidebarItem(
    item: TvSideMenuItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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
