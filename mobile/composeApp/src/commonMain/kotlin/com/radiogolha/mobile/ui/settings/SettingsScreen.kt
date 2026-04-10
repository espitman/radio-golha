package com.radiogolha.mobile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.theme.GolhaElevation
import com.radiogolha.mobile.theme.GolhaPatternBackground
import com.radiogolha.mobile.theme.GolhaRadius
import com.radiogolha.mobile.theme.GolhaSpacing
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.BottomNavigationBar
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.GolhaLineIcon
import com.radiogolha.mobile.ui.home.SmallPrimaryButton

@Composable
fun SettingsScreen(
    bottomNavItems: List<BottomNavItemUiModel>,
    onBottomNavSelected: (AppTab) -> Unit,
    isDebugDatabaseToolsEnabled: Boolean,
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
) {
    CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides LayoutDirection.Rtl) {
        GolhaPatternBackground {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                containerColor = GolhaColors.ScreenBackground.copy(alpha = 0f),
            bottomBar = {
                BottomNavigationBar(
                    items = bottomNavItems,
                    onItemSelected = onBottomNavSelected,
                )
            },
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding(),
                contentPadding = PaddingValues(
                    start = GolhaSpacing.ScreenHorizontal,
                    end = GolhaSpacing.ScreenHorizontal,
                    top = GolhaSpacing.StatusBarTopGap,
                    bottom = innerPadding.calculateBottomPadding() + 22.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                item {
                    Text(
                        text = "حساب من",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GolhaColors.PrimaryText,
                    )
                }
                item {
                    Text(
                        text = "تنظیمات و ابزارهای این نسخه از اپ را اینجا می‌بینی.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = GolhaColors.SecondaryText,
                    )
                }
                item {
                    if (isDebugDatabaseToolsEnabled) {
                        DebugDatabaseCard(
                            isImportingDatabase = isImportingDatabase,
                            onImportDebugDatabase = onImportDebugDatabase,
                        )
                    } else {
                        PlaceholderSettingsCard()
                    }
                }
            }
        }
    }
}
}

@Composable
private fun DebugDatabaseCard(
    isImportingDatabase: Boolean,
    onImportDebugDatabase: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "ابزار توسعه",
                style = MaterialTheme.typography.titleLarge,
                color = GolhaColors.PrimaryText,
            )

            SmallPrimaryButton(
                label = "دریافت دیتابیس جدید",
                enabled = !isImportingDatabase,
                loading = isImportingDatabase,
                onClick = onImportDebugDatabase,
            )
        }
    }
}

@Composable
private fun PlaceholderSettingsCard() {
    Surface(
        shape = RoundedCornerShape(GolhaRadius.Card),
        color = GolhaColors.Surface,
        tonalElevation = 0.dp,
        shadowElevation = GolhaElevation.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, GolhaColors.Border),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                GolhaLineIcon(
                    icon = GolhaIcon.Account,
                    modifier = Modifier.padding(2.dp),
                    tint = GolhaColors.SecondaryText,
                )
                Text(
                    text = "بخش‌های بیشتر این صفحه بعداً کامل می‌شوند.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = GolhaColors.PrimaryText,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
