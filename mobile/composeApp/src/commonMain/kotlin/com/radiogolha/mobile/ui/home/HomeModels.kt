package com.radiogolha.mobile.ui.home

data class ProgramUiModel(
    val title: String,
    val episodeCount: Int,
)

data class SingerUiModel(
    val name: String,
    val imageUrl: String? = null,
)

data class SingerListItemUiModel(
    val name: String,
    val imageUrl: String? = null,
    val programCount: Int,
)

data class DastgahUiModel(
    val name: String,
)

data class MusicianUiModel(
    val name: String,
    val instrument: String,
    val imageUrl: String? = null,
)

data class MusicianListItemUiModel(
    val name: String,
    val instrument: String,
    val imageUrl: String? = null,
    val programCount: Int,
)

data class TrackUiModel(
    val title: String,
    val artist: String,
    val duration: String,
    val coverUrl: String? = null,
)

data class BottomNavItemUiModel(
    val label: String,
    val icon: GolhaIcon,
    val tab: AppTab,
    val selected: Boolean = false,
)

enum class AppTab {
    Home,
    Search,
    Library,
    Account,
}

data class HomeUiState(
    val programs: List<ProgramUiModel>,
    val singers: List<SingerUiModel>,
    val dastgahs: List<DastgahUiModel>,
    val musicians: List<MusicianUiModel>,
    val topTracks: List<TrackUiModel>,
    val bottomNavItems: List<BottomNavItemUiModel>,
    val isDatabaseSyncing: Boolean = false,
)

enum class GolhaIcon {
    Favorites,
    Profile,
    Home,
    Search,
    Library,
    Account,
    Play,
    More,
    Download,
}
