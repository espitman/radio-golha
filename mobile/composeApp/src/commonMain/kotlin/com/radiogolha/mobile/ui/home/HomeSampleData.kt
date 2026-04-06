package com.radiogolha.mobile.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

internal val samplePrograms = listOf(
    ProgramUiModel(title = "گل‌های رنگارنگ", episodeCount = 45),
    ProgramUiModel(title = "گل‌های جاویدان", episodeCount = 60),
    ProgramUiModel(title = "گل‌های سبز", episodeCount = 30),
)

internal val sampleSingers = listOf(
    SingerUiModel(name = "محمدرضا بنان"),
    SingerUiModel(name = "دلکش"),
    SingerUiModel(name = "مرضیه"),
    SingerUiModel(name = "محمدرضا شجریان"),
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
    MusicianUiModel(name = "جلیل شهناز", instrument = "تار"),
    MusicianUiModel(name = "حسن کسایی", instrument = "نی"),
    MusicianUiModel(name = "فرامرز پایور", instrument = "سنتور"),
    MusicianUiModel(name = "حسین علیزاده", instrument = "تار"),
)

internal val sampleTracks = listOf(
    TrackUiModel(title = "بهار دلنشین", artist = "الهه", duration = "3:24"),
    TrackUiModel(title = "تا بهار دلنشین", artist = "بنان", duration = "2:56"),
    TrackUiModel(title = "خزان جدایی", artist = "مرضیه", duration = "4:12"),
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
