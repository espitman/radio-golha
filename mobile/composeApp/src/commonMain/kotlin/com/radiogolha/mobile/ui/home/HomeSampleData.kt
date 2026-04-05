package com.radiogolha.mobile.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private val samplePrograms = listOf(
    ProgramUiModel(title = "گل‌های رنگارنگ", episodeCount = 45),
    ProgramUiModel(title = "گل‌های جاویدان", episodeCount = 60),
    ProgramUiModel(title = "گل‌های سبز", episodeCount = 30),
)

private val sampleSingers = listOf(
    SingerUiModel(name = "محمدرضا بنان"),
    SingerUiModel(name = "دلکش"),
    SingerUiModel(name = "مرضیه"),
    SingerUiModel(name = "محمدرضا شجریان"),
)

private val sampleDastgahs = listOf(
    DastgahUiModel("ماهور"),
    DastgahUiModel("سه‌گاه"),
    DastgahUiModel("شور"),
    DastgahUiModel("همایون"),
    DastgahUiModel("نوا"),
    DastgahUiModel("راست‌پنجگاه"),
)

private val sampleMusicians = listOf(
    MusicianUiModel(name = "جلیل شهناز", instrument = "تار"),
    MusicianUiModel(name = "حسن کسایی", instrument = "نی"),
    MusicianUiModel(name = "فرامرز پایور", instrument = "سنتور"),
    MusicianUiModel(name = "حسین علیزاده", instrument = "تار"),
)

private val sampleTracks = listOf(
    TrackUiModel(title = "بهار دلنشین", artist = "الهه", duration = "3:24"),
    TrackUiModel(title = "تا بهار دلنشین", artist = "بنان", duration = "2:56"),
    TrackUiModel(title = "خزان جدایی", artist = "مرضیه", duration = "4:12"),
)

private val sampleBottomNav = listOf(
    BottomNavItemUiModel(label = "خانه", icon = GolhaIcon.Home, selected = true),
    BottomNavItemUiModel(label = "جستجو", icon = GolhaIcon.Search),
    BottomNavItemUiModel(label = "کتابخانه", icon = GolhaIcon.Library),
    BottomNavItemUiModel(label = "حساب من", icon = GolhaIcon.Account),
)

@Composable
fun rememberSampleHomeUiState(): HomeUiState = remember {
    HomeUiState(
        programs = samplePrograms,
        singers = sampleSingers,
        dastgahs = sampleDastgahs,
        musicians = sampleMusicians,
        topTracks = sampleTracks,
        bottomNavItems = sampleBottomNav,
    )
}
