package com.radiogolha.mobile.ui.singers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.ArtistAvatar
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationBar
import com.radiogolha.mobile.ui.home.SingerListItemUiModel

@Composable
fun SingersScreen(
    singers: List<SingerListItemUiModel>,
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    onBackClick: () -> Unit,
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .background(GolhaColors.ScreenBackground),
            containerColor = GolhaColors.ScreenBackground,
            bottomBar = {
                BottomNavigationBar(
                    items = bottomNavItems,
                    onItemSelected = onBottomNavSelected,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = GolhaSpacing.ScreenHorizontal,
                    end = GolhaSpacing.ScreenHorizontal,
                    top = 18.dp,
                    bottom = innerPadding.calculateBottomPadding() + 18.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item {
                    SingersHeader(onBackClick = onBackClick)
                }

                item {
                    Text(
                        text = "همه خواننده‌ها",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GolhaColors.PrimaryText,
                    )
                }

                item {
                    Text(
                        text = "${singers.size} خواننده",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )
                }

                item {
                    Surface(
                        shape = RoundedCornerShape(GolhaRadius.Card),
                        color = GolhaColors.Surface,
                        tonalElevation = 0.dp,
                        shadowElevation = GolhaElevation.Card,
                        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
                    ) {
                        Column {
                            singers.forEachIndexed { index, singer ->
                                SingerRow(item = singer)
                                if (index != singers.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 18.dp),
                                        color = GolhaColors.Border.copy(alpha = 0.8f),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SingersHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "خواننده‌ها",
            style = MaterialTheme.typography.titleLarge,
            color = GolhaColors.PrimaryText,
        )

        Surface(
            modifier = Modifier.clickable { onBackClick() },
            shape = RoundedCornerShape(999.dp),
            color = GolhaColors.Surface,
            tonalElevation = 0.dp,
            shadowElevation = 1.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
        ) {
            Text(
                text = "بازگشت",
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                style = MaterialTheme.typography.labelLarge,
                color = GolhaColors.PrimaryText,
            )
        }
    }
}

@Composable
private fun SingerRow(item: SingerListItemUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArtistAvatar(
            name = item.name,
            imageUrl = item.imageUrl,
            tint = GolhaColors.SoftBlue,
            modifier = Modifier.size(64.dp),
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = GolhaColors.PrimaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${item.programCount} برنامه",
                style = MaterialTheme.typography.bodySmall,
                color = GolhaColors.SecondaryText,
            )
        }
    }
}
