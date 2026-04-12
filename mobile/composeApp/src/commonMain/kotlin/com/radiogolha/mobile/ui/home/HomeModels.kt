package com.radiogolha.mobile.ui.home

data class ProgramUiModel(
    val title: String,
    val episodeCount: Int,
)

data class CategoryProgramUiModel(
    val id: Long,
    val artistId: Long? = null,
    val title: String,
    val categoryName: String?,
    val programNumber: String,
    val singer: String,
    val duration: String?,
    val dastgah: String?,
    val audioUrl: String? = null,
)

data class SingerUiModel(
    val id: Long,
    val name: String,
    val imageUrl: String? = null,
)

data class SingerListItemUiModel(
    val artistId: Long,
    val name: String,
    val imageUrl: String? = null,
    val programCount: Int,
)

data class DastgahUiModel(
    val name: String,
)

data class MusicianUiModel(
    val id: Long,
    val name: String,
    val instrument: String,
    val imageUrl: String? = null,
)

data class MusicianListItemUiModel(
    val artistId: Long,
    val name: String,
    val instrument: String,
    val imageUrl: String? = null,
    val programCount: Int,
)

data class TrackUiModel(
    val id: Long,
    val artistId: Long? = null,
    val title: String,
    val artist: String,
    val duration: String? = null,
    val coverUrl: String? = null,
    val audioUrl: String? = null,
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
    val isRefreshing: Boolean = false,
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
    Pause,
    More,
    Download,
    Back,
    Refresh,
    Timer,
    Note,
    History,
    People,
}

data class PerformerUiModel(
    val name: String,
    val avatar: String?,
    val instrument: String?
)

data class OrchestraLeaderUiModel(
    val orchestra: String,
    val name: String
)

data class TimelineSegmentUiModel(
    val id: Long,
    val startTime: String?,
    val endTime: String?,
    val modeName: String?,
    val singers: List<String>,
    val poets: List<String>,
    val announcers: List<String>,
    val orchestras: List<String>,
    val orchestraLeaders: List<OrchestraLeaderUiModel>,
    val performers: List<PerformerUiModel>,
)

data class TranscriptVerseUiModel(
    val segmentOrder: Int,
    val verseOrder: Int,
    val text: String
)

data class ArtistCreditUiModel(
    val name: String,
    val avatar: String?
)

data class ProgramEpisodeDetailUiModel(
    val id: Long,
    val title: String,
    val categoryName: String,
    val no: Int,
    val subNo: String?,
    val duration: String? = null,
    val audioUrl: String?,
    val singers: List<ArtistCreditUiModel>,
    val poets: List<ArtistCreditUiModel>,
    val announcers: List<ArtistCreditUiModel>,
    val composers: List<ArtistCreditUiModel>,
    val arrangers: List<ArtistCreditUiModel>,
    val modes: List<String>,
    val orchestras: List<ArtistCreditUiModel>,
    val orchestraLeaders: List<OrchestraLeaderUiModel>,
    val performers: List<PerformerUiModel>,
    val timeline: List<TimelineSegmentUiModel>,
    val transcript: List<TranscriptVerseUiModel>,
)

data class ArtistDetailUiModel(
    val artistId: Long,
    val name: String,
    val imageUrl: String? = null,
    val instrument: String? = null,
    val trackCount: Int,
    val tracks: List<CategoryProgramUiModel>,
)
