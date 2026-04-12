package com.radiogolha.mobile.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

internal val samplePrograms = listOf(
    ProgramUiModel(title = "گل‌های رنگارنگ", episodeCount = 45),
    ProgramUiModel(title = "گل‌های جاویدان", episodeCount = 60),
    ProgramUiModel(title = "گل‌های سبز", episodeCount = 30),
)

internal val sampleSingers = listOf(
    SingerUiModel(id = 1, name = "محمدرضا بنان"),
    SingerUiModel(id = 2, name = "دلکش"),
    SingerUiModel(id = 3, name = "مرضیه"),
    SingerUiModel(id = 4, name = "محمدرضا شجریان"),
)

internal val sampleDastgahs = listOf(
    DastgahUiModel("ماهور"),
    DastgahUiModel("سه‌گاه"),
    DastgahUiModel("شور"),
    DastgahUiModel("همایون"),
    DastgahUiModel("نوا"),
    DastgahUiModel("راست‌پنجگاه"),
)

internal val sampleMusicians = listOf(
    MusicianUiModel(id = 5, name = "جلیل شهناز", instrument = "تار"),
    MusicianUiModel(id = 6, name = "حسن کسایی", instrument = "نی"),
    MusicianUiModel(id = 7, name = "فرامرز پایور", instrument = "سنتور"),
    MusicianUiModel(id = 8, name = "حسین علیزاده", instrument = "تار"),
)

internal val sampleTracks = listOf(
    TrackUiModel(id = 1, title = "بهار دلنشین", artist = "الهه", duration = "3:24"),
    TrackUiModel(id = 2, title = "تا بهار دلنشین", artist = "بنان", duration = "2:56"),
    TrackUiModel(id = 3, title = "خزان جدایی", artist = "مرضیه", duration = "4:12"),
)

internal val sampleBottomNav = listOf(
    BottomNavItemUiModel(label = "خانه", icon = GolhaIcon.Home, tab = AppTab.Home, selected = true),
    BottomNavItemUiModel(label = "جستجو", icon = GolhaIcon.Search, tab = AppTab.Search),
    BottomNavItemUiModel(label = "کتابخانه", icon = GolhaIcon.Library, tab = AppTab.Library),
    BottomNavItemUiModel(label = "حساب من", icon = GolhaIcon.Account, tab = AppTab.Account),
)

fun sampleHomeUiState(): HomeUiState = HomeUiState(
    programs = samplePrograms,
    singers = sampleSingers,
    dastgahs = sampleDastgahs,
    musicians = sampleMusicians,
    topTracks = sampleTracks,
    bottomNavItems = sampleBottomNav,
)

@Composable
fun rememberSampleHomeUiState(): HomeUiState = remember {
    sampleHomeUiState()
}
