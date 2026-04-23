package com.radiogolha.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.radiogolha.mobile.debug.importDebugDatabase
import com.radiogolha.mobile.debug.isDebugDatabaseToolsEnabled
import com.radiogolha.mobile.debug.showDebugToast
import com.radiogolha.mobile.data.updateArchiveDatabaseFromCdn
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.HomeScreen
import com.radiogolha.mobile.ui.home.HomeUiState
import com.radiogolha.mobile.ui.home.TrackUiModel
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.home.loadTopTracks
import com.radiogolha.mobile.ui.musicians.MusiciansScreen
import com.radiogolha.mobile.ui.musicians.loadMusiciansUiState
import com.radiogolha.mobile.ui.library.LibraryScreen
import com.radiogolha.mobile.ui.library.LibraryTab
import com.radiogolha.mobile.ui.orchestras.OrchestraDetailScreen
import com.radiogolha.mobile.ui.orchestras.loadOrchestrasUiState
import com.radiogolha.mobile.ui.programs.loadProgramsUiState
import com.radiogolha.mobile.ui.search.loadSearchOptions
import com.radiogolha.mobile.ui.search.searchPrograms
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.SingersScreen
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import com.radiogolha.mobile.ui.player.NowPlayingScreen
import com.radiogolha.mobile.ui.programs.toTrackUiModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import androidx.compose.runtime.CompositionLocalProvider
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.home.GolhaLineIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val player = rememberGolhaPlayer()
    val currentTrack by player.currentTrack.collectAsState()
    val isPlayerPlaying by player.isPlaying.collectAsState()
    val isPlayerLoading by player.isLoading.collectAsState()
    val currentPlaybackPositionMs by player.currentPositionMs.collectAsState()
    val currentPlaybackDurationMs by player.durationMs.collectAsState()

    // Multi-stack navigation state
    val navigationStacks = remember {
        val map = SnapshotStateMap<AppTab, List<AppRoute>>()
        AppTab.entries.forEach { tab ->
            map[tab] = listOf(AppRoute.Root(tab))
        }
        map
    }
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var libraryInitialTab by remember { mutableStateOf(LibraryTab.Programs) }
    var accountInitialTab by remember { mutableStateOf(0) }
    val searchState = remember { com.radiogolha.mobile.ui.search.SearchState() }
    var showNowPlayingSheet by remember { mutableStateOf(false) }
    
    val currentStack: List<AppRoute> = navigationStacks[selectedTab] ?: listOf(AppRoute.Root(selectedTab))
    
    fun push(route: AppRoute) {
        val stack = navigationStacks[selectedTab]?.toMutableList() ?: mutableListOf()
        if (stack.lastOrNull() != route) {
            stack.add(route)
            navigationStacks[selectedTab] = stack
        }
    }
    
    fun pop() {
        val stack = navigationStacks[selectedTab]?.toMutableList() ?: mutableListOf()
        if (stack.size > 1) {
            stack.removeAt(stack.lastIndex)
            navigationStacks[selectedTab] = stack
        }
    }
    
    fun onTabSelected(tab: AppTab) {
        if (selectedTab == tab) {
            // Reset current tab stack on re-click
            navigationStacks[tab] = listOf(AppRoute.Root(tab))
        } else {
            selectedTab = tab
        }
    }

    var reloadToken by remember { mutableStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    var isUpdatingDatabaseFromCdn by remember { mutableStateOf(false) }
    var databaseUpdateProgress by remember { mutableStateOf<Float?>(null) }
    val scope = rememberCoroutineScope()

    val homeState by produceState<HomeUiState?>(initialValue = null, reloadToken) {
        value = loadHomeUiState()
    }

    var isProgramsLoading by remember { mutableStateOf(false) }
    val programs by produceState(initialValue = emptyList<com.radiogolha.mobile.ui.home.ProgramUiModel>(), reloadToken) {
        isProgramsLoading = true
        value = loadProgramsUiState()
        isProgramsLoading = false
    }

    var isSingersLoading by remember { mutableStateOf(false) }
    val singers by produceState<List<com.radiogolha.mobile.ui.home.SingerListItemUiModel>>(initialValue = emptyList(), reloadToken) {
        isSingersLoading = true
        value = loadSingersUiState()
        isSingersLoading = false
    }

    var isMusiciansLoading by remember { mutableStateOf(false) }
    val musicians by produceState<List<com.radiogolha.mobile.ui.home.MusicianListItemUiModel>>(initialValue = emptyList(), reloadToken) {
        isMusiciansLoading = true
        value = loadMusiciansUiState()
        isMusiciansLoading = false
    }

    var isOrchestrasLoading by remember { mutableStateOf(false) }
    val orchestras by produceState<List<com.radiogolha.mobile.ui.home.OrchestraListItemUiModel>>(initialValue = emptyList(), reloadToken) {
        isOrchestrasLoading = true
        value = loadOrchestrasUiState()
        isOrchestrasLoading = false
    }

    // User-specific state
    val recentlyPlayedIds by produceState(initialValue = emptyList<Long>(), reloadToken, currentTrack) {
        value = com.radiogolha.mobile.ui.home.loadRecentlyPlayedIds(12)
    }
    
    val recentlyPlayed by produceState(initialValue = emptyList<TrackUiModel>(), recentlyPlayedIds) {
        value = com.radiogolha.mobile.ui.home.loadProgramsByIds(recentlyPlayedIds).map { it.toTrackUiModel() }
    }

    val savedPlaylists by produceState(initialValue = emptyList<com.radiogolha.mobile.ui.home.SavedPlaylistUiModel>(), reloadToken) {
        value = com.radiogolha.mobile.ui.home.loadSavedPlaylists()
    }

    val favoriteSingers by produceState(initialValue = emptyList<com.radiogolha.mobile.ui.home.SingerListItemUiModel>(), reloadToken) {
        value = com.radiogolha.mobile.ui.home.loadFavoriteSingers()
    }

    val favoriteMusicians by produceState(initialValue = emptyList<com.radiogolha.mobile.ui.home.MusicianListItemUiModel>(), reloadToken) {
        value = com.radiogolha.mobile.ui.home.loadFavoriteMusicians()
    }

    val mostPlayedIds by produceState(initialValue = emptyList<Long>(), reloadToken) {
        value = com.radiogolha.mobile.ui.home.loadMostPlayedIds(10)
    }

    val mostPlayed by produceState(initialValue = emptyList<TrackUiModel>(), mostPlayedIds) {
        value = com.radiogolha.mobile.ui.home.loadProgramsByIds(mostPlayedIds).map { it.toTrackUiModel() }
    }

    val isTopTracksRefreshing by produceState(initialValue = false, reloadToken) {
        value = false
    }

    var selectedTrackForOptions by remember { mutableStateOf<TrackUiModel?>(null) }
    data class ArtistQuickAction(
        val artistId: Long,
        val name: String,
        val type: String,
        val isFavorite: Boolean,
    )
    var artistForQuickActions by remember { mutableStateOf<ArtistQuickAction?>(null) }
    LaunchedEffect(currentTrack) {
        if (currentTrack == null) {
            showNowPlayingSheet = false
        }
    }

    GolhaAppTheme {
        val currentRoute = currentStack.last()
        val bottomNavItems = buildBottomNavItems(selectedTab)
        val openNowPlaying: () -> Unit = {
            if (currentTrack != null) {
                showNowPlayingSheet = true
            }
        }
        
        when (currentRoute) {
            AppRoute.Singers -> {
                SingersScreen(
                    singers = singers,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onSingerClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            AppRoute.Musicians -> {
                MusiciansScreen(
                    musicians = musicians,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onMusicianClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.ArtistDetail -> {
                val isArtistFavorite by produceState(initialValue = false, currentRoute.id, reloadToken) {
                    value = runCatching {
                        RustCoreBridge.isFavoriteArtist(
                            com.radiogolha.mobile.ui.home.requireUserDbPath(),
                            currentRoute.id
                        ) == "true"
                    }.getOrDefault(false)
                }
                com.radiogolha.mobile.ui.artists.ArtistDetailScreen(
                    artistId = currentRoute.id,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    onProgramClick = { program -> push(AppRoute.ProgramEpisodeDetail(program.id)) },
                    onPlayTrack = { player.play(it) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    isFavorite = isArtistFavorite,
                    onToggleFavorite = {
                        scope.launch {
                            val isFavStr = RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.id)
                            if (isFavStr == "true") {
                                RustCoreBridge.removeFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.id)
                            } else {
                                val favoriteType = if (musicians.any { it.artistId == currentRoute.id }) "musician" else "artist"
                                RustCoreBridge.addFavoriteArtist(
                                    com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                    currentRoute.id,
                                    favoriteType
                                )
                            }
                            reloadToken += 1
                        }
                    },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.CategoryPrograms -> {
                com.radiogolha.mobile.ui.programs.CategoryProgramsScreen(
                    categoryId = currentRoute.category.id,
                    categoryTitle = currentRoute.category.title,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onPlayTrack = { player.play(it) },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onProgramClick = { program -> push(AppRoute.ProgramEpisodeDetail(program.id)) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                )
            }

            is AppRoute.OrchestraDetail -> {
                OrchestraDetailScreen(
                    orchestraId = currentRoute.id,
                    orchestraName = currentRoute.name,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    onPlayTrack = { player.play(it) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.ProgramEpisodeDetail -> {
                com.radiogolha.mobile.ui.programs.ProgramEpisodeDetailScreen(
                    programId = currentRoute.programId,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    onOrchestraClick = { orchestraName ->
                        val match = orchestras.firstOrNull { it.name == orchestraName }
                            ?: orchestras.firstOrNull { it.name.trim().equals(orchestraName.trim(), ignoreCase = true) }
                        match?.let { push(AppRoute.OrchestraDetail(it.id, it.name)) }
                    },
                    onPlayProgram = { detail ->
                        player.play(
                            TrackUiModel(
                                id = detail.id,
                                artistId = detail.singers.firstOrNull()?.artistId,
                                title = detail.title,
                                artist = detail.singers.joinToString(" و ") { it.name }.ifBlank { "نامشخص" },
                                duration = detail.duration ?: "",
                                audioUrl = detail.audioUrl,
                                coverUrl = detail.singers.firstOrNull()?.avatar,
                                artistImages = detail.singers.mapNotNull { it.avatar },
                            )
                        )
                    },
                    onAddToPlaylist = { track ->
                        selectedTrackForOptions = track
                    },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onSeek = { player.seekTo(it) },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.PlaylistDetail -> {
                val playlistDetail by produceState<ManualPlaylistDetail?>(initialValue = null, currentRoute.playlistId, reloadToken) {
                    value = loadManualPlaylistDetail(currentRoute.playlistId)
                }

                com.radiogolha.mobile.ui.search.PlaylistDetailScreen(
                    playlistName = playlistDetail?.name ?: currentRoute.playlistName,
                    trackIds = playlistDetail?.trackIds ?: emptyList(),
                    isManual = true,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onRename = { newName ->
                        scope.launch {
                            RustCoreBridge.renamePlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.playlistId, newName)
                            reloadToken += 1
                        }
                    },
                    onDelete = {
                        scope.launch {
                            RustCoreBridge.deletePlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.playlistId)
                            pop()
                            reloadToken += 1
                        }
                    },
                    onProgramClick = { id -> push(AppRoute.ProgramEpisodeDetail(id)) },
                    onPlayTrack = { player.play(it) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.DuetDetail -> {
                com.radiogolha.mobile.ui.home.DuetDetailScreen(
                    singer1 = currentRoute.duet.singer1,
                    singer2 = currentRoute.duet.singer2,
                    singer1Avatar = currentRoute.duet.singer1Avatar,
                    singer2Avatar = currentRoute.duet.singer2Avatar,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onPlayTrack = { player.play(it) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            is AppRoute.ModeDetail -> {
                com.radiogolha.mobile.ui.home.ModeDetailScreen(
                    modeId = currentRoute.modeId,
                    modeName = currentRoute.modeName,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onPlayTrack = { player.play(it) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onExpandPlayer = openNowPlaying,
                )
            }

            AppRoute.NowPlaying -> Unit

            is AppRoute.Root -> {
                when (currentRoute.tab) {
                    AppTab.Home -> {
                        HomeScreen(
                            state = homeState?.copy(bottomNavItems = bottomNavItems),
                            duets = homeState?.duets ?: emptyList(),
                            bottomNavItems = bottomNavItems,
                            onOpenAllSingers = {
                                libraryInitialTab = LibraryTab.Singers
                                selectedTab = AppTab.Library
                            },
                            onOpenAllMusicians = {
                                libraryInitialTab = LibraryTab.Musicians
                                selectedTab = AppTab.Library
                            },
                            onOpenAllPlaylists = {
                                accountInitialTab = 1
                                selectedTab = AppTab.Account
                            },
                            onRefreshTopTracks = { reloadToken += 1 },
                            isRefreshingTopTracks = isTopTracksRefreshing,
                            onDastgahClick = { modeName ->
                                scope.launch {
                                    val modeId = withContext(Dispatchers.Default) {
                                        runCatching {
                                            val modes = loadSearchOptions().modes
                                            modes.firstOrNull { it.name == modeName }?.id
                                                ?: modes.firstOrNull { it.name.trim().equals(modeName.trim(), ignoreCase = true) }?.id
                                        }.getOrNull()
                                    }
                                    if (modeId != null) {
                                        push(AppRoute.ModeDetail(modeId = modeId, modeName = modeName))
                                    }
                                }
                            },
                            onTrackClick = { track -> push(AppRoute.ProgramEpisodeDetail(track.id)) },
                            onSingerClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onSingerLongPress = { singer ->
                                artistForQuickActions = ArtistQuickAction(
                                    artistId = singer.id,
                                    name = singer.name,
                                    type = "singer",
                                    isFavorite = runCatching {
                                        RustCoreBridge.isFavoriteArtist(
                                            com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                            singer.id
                                        ) == "true"
                                    }.getOrDefault(false),
                                )
                            },
                            onMusicianClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onMusicianLongPress = { musician ->
                                artistForQuickActions = ArtistQuickAction(
                                    artistId = musician.id,
                                    name = musician.name,
                                    type = "performer",
                                    isFavorite = runCatching {
                                        RustCoreBridge.isFavoriteArtist(
                                            com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                            musician.id
                                        ) == "true"
                                    }.getOrDefault(false),
                                )
                            },
                            onDuetClick = { duet -> push(AppRoute.DuetDetail(duet)) },
                            onProgramClick = { category -> push(AppRoute.CategoryPrograms(category)) },
                            onBottomNavSelected = { onTabSelected(it) },
                            onPlayTrack = { player.play(it) },
                            onTrackLongClick = { selectedTrackForOptions = it },
                            recentlyPlayed = recentlyPlayed,
                            savedPlaylists = savedPlaylists,
                            onPlaylistClick = { id ->
                                val playlist = savedPlaylists.find { it.id == id }
                                push(
                                    AppRoute.PlaylistDetail(
                                        playlistId = id,
                                        playlistName = playlist?.name ?: "لیست من"
                                    )
                                )
                            },
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                            onExpandPlayer = openNowPlaying,
                        )
                    }

                    AppTab.Search -> {
                        com.radiogolha.mobile.ui.search.SearchScreen(
                            state = searchState,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = { onTabSelected(it) },
                            onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                            onPlayTrack = { player.play(it) },
                            onTrackLongClick = { selectedTrackForOptions = it },
                            onSavePlaylist = { name, filters ->
                                val trimmedName = name.trim()
                                if (trimmedName.isNotBlank()) {
                                    scope.launch {
                                        val requestJson = "{\"name\":\"${escapeJson(trimmedName)}\",\"type\":\"manual\"}"
                                        val newId = RustCoreBridge
                                            .createPlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), requestJson)
                                            .toLongOrNull() ?: 0L
                                        if (newId <= 0L) return@launch

                                        val trackIds = linkedSetOf<Long>()
                                        var page = 1
                                        var totalPages = 1
                                        do {
                                            val result = withContext(Dispatchers.Default) {
                                                runCatching { searchPrograms(filters, page) }
                                                    .getOrDefault(com.radiogolha.mobile.ui.search.SearchResultsUiState())
                                            }
                                            result.results.forEach { trackIds += it.id }
                                            totalPages = result.totalPages.coerceAtLeast(1)
                                            page += 1
                                        } while (page <= totalPages)

                                        trackIds.forEach { trackId ->
                                            RustCoreBridge.addTrackToPlaylist(
                                                com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                                newId,
                                                trackId
                                            )
                                        }

                                        reloadToken += 1
                                        push(AppRoute.PlaylistDetail(playlistId = newId, playlistName = trimmedName))
                                    }
                                }
                            },
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                            onExpandPlayer = openNowPlaying,
                        )
                    }

                    AppTab.Library -> {
                        LibraryScreen(
                            initialTab = libraryInitialTab,
                            programs = programs,
                            singers = singers,
                            musicians = musicians,
                            orchestras = orchestras,
                            isProgramsLoading = isProgramsLoading,
                            isSingersLoading = isSingersLoading,
                            isMusiciansLoading = isMusiciansLoading,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = { onTabSelected(it) },
                            onProgramClick = { category -> push(AppRoute.CategoryPrograms(category)) },
                            onSingerClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onMusicianClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onOrchestraClick = { id ->
                                push(AppRoute.OrchestraDetail(id, orchestras.find { it.id == id }?.name ?: ""))
                            },
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                            onExpandPlayer = openNowPlaying,
                            onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) }
                        )
                    }

                    AppTab.Account -> {
                        SettingsScreen(
                            initialTabIndex = accountInitialTab,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = { onTabSelected(it) },
                            isUpdatingDatabaseFromCdn = isUpdatingDatabaseFromCdn,
                            databaseUpdateProgress = databaseUpdateProgress,
                            onUpdateDatabaseFromCdn = {
                                if (!isUpdatingDatabaseFromCdn) {
                                    scope.launch {
                                        isUpdatingDatabaseFromCdn = true
                                        databaseUpdateProgress = null
                                        val result = updateArchiveDatabaseFromCdn(
                                            forceDownload = false,
                                            onProgress = { progress ->
                                                scope.launch {
                                                    databaseUpdateProgress = progress
                                                }
                                            }
                                        )
                                        showDebugToast(result.message)
                                        if (result.success && result.didUpdate) {
                                            searchState.optionsLoaded = false
                                            searchState.results = com.radiogolha.mobile.ui.search.SearchResultsUiState()
                                            searchState.allResults = emptyList()
                                            searchState.currentPage = com.radiogolha.mobile.ui.search.SearchPage.Filters
                                            reloadToken += 1
                                            onTabSelected(AppTab.Home)
                                        }
                                        databaseUpdateProgress = null
                                        isUpdatingDatabaseFromCdn = false
                                    }
                                }
                            },
                            isDebugDatabaseToolsEnabled = isDebugDatabaseToolsEnabled(),
                            isImportingDatabase = isImportingDatabase,
                            onImportDebugDatabase = {
                                if (!isImportingDatabase) {
                                    scope.launch {
                                        isImportingDatabase = true
                                        val result = importDebugDatabase()
                                        showDebugToast(result.message)
                                        if (result.success) {
                                            reloadToken += 1
                                            onTabSelected(AppTab.Home)
                                        }
                                        isImportingDatabase = false
                                    }
                                }
                            },
                            favoriteSingers = favoriteSingers,
                            favoriteMusicians = favoriteMusicians,
                            onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            recentlyPlayedTracks = recentlyPlayed,
                            mostPlayedTracks = mostPlayed,
                            savedPlaylists = savedPlaylists,
                            onTrackClick = { id -> push(AppRoute.ProgramEpisodeDetail(id)) },
                            onPlayTrack = { player.play(it) },
                            onTrackLongClick = { selectedTrackForOptions = it },
                            onPlaylistClick = { id ->
                                val playlist = savedPlaylists.find { it.id == id }
                                push(
                                    AppRoute.PlaylistDetail(
                                        playlistId = id,
                                        playlistName = playlist?.name ?: "لیست من"
                                    )
                                )
                            },
                            onPlaylistLongClick = { id ->
                                val playlist = savedPlaylists.find { it.id == id }
                                push(
                                    AppRoute.PlaylistDetail(
                                        playlistId = id,
                                        playlistName = playlist?.name ?: "لیست من"
                                    )
                                )
                            },
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                            onExpandPlayer = openNowPlaying
                        )
                    }
                }
            }
        }


        if (showNowPlayingSheet && currentTrack != null) {
            ModalBottomSheet(
                onDismissRequest = { showNowPlayingSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = null,
                containerColor = Color(0xFF0A1628),
                tonalElevation = 0.dp,
            ) {
                NowPlayingScreen(
                    currentTrack = currentTrack,
                    isPlaying = isPlayerPlaying,
                    isLoading = isPlayerLoading,
                    currentPositionMs = currentPlaybackPositionMs,
                    durationMs = currentPlaybackDurationMs,
                    onTogglePlayback = { player.togglePlayback() },
                    onSeek = { player.seekTo(it) },
                    onBackClick = { showNowPlayingSheet = false },
                    onInfoClick = {
                        showNowPlayingSheet = false
                        currentTrack?.let { push(AppRoute.ProgramEpisodeDetail(it.id)) }
                    },
                    onSeekBack10 = {
                        val target = (currentPlaybackPositionMs - 10_000L).coerceAtLeast(0L)
                        player.seekTo(target)
                    },
                    onSeekForward10 = {
                        val upper = if (currentPlaybackDurationMs > 0L) currentPlaybackDurationMs else Long.MAX_VALUE
                        val target = (currentPlaybackPositionMs + 10_000L).coerceAtMost(upper)
                        player.seekTo(target)
                    },
                )
            }
        }

        
        selectedTrackForOptions?.let { track ->
            com.radiogolha.mobile.ui.programs.TrackOptionsSheet(
                track = track,
                manualPlaylists = savedPlaylists.map { com.radiogolha.mobile.ui.programs.PlaylistOptionItem(it.id, it.name) },
                onDismiss = { selectedTrackForOptions = null },
                onGoToProgram = { 
                    push(AppRoute.ProgramEpisodeDetail(track.id))
                    selectedTrackForOptions = null
                },
                onGoToArtist = { 
                    track.artistId?.let { id -> push(AppRoute.ArtistDetail(id)) }
                    selectedTrackForOptions = null
                },
                onAddToPlaylist = { playlistId ->
                    scope.launch {
                        RustCoreBridge.addTrackToPlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), playlistId, track.id)
                        reloadToken += 1
                    }
                },
                onCreatePlaylist = { name ->
                    scope.launch {
                        val json = "{\"name\":\"$name\",\"type\":\"manual\"}"
                        val newId = RustCoreBridge.createPlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), json).toLongOrNull() ?: 0L
                        if (newId > 0) {
                            RustCoreBridge.addTrackToPlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), newId, track.id)
                            reloadToken += 1
                        }
                    }
                }
            )
        }

        artistForQuickActions?.let { artist ->
            ModalBottomSheet(
                onDismissRequest = { artistForQuickActions = null },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = GolhaColors.ScreenBackground,
                tonalElevation = 0.dp,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = artist.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = GolhaColors.PrimaryText,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }

                        HorizontalDivider(color = GolhaColors.Border.copy(alpha = 0.5f))

                        ArtistOptionRow(
                            icon = GolhaIcon.Favorites,
                            text = if (artist.isFavorite) "حذف از لیست مورد علاقه" else "افزودن به لیست مورد علاقه",
                            onClick = {
                                scope.launch {
                                    if (artist.isFavorite) {
                                        RustCoreBridge.removeFavoriteArtist(
                                            com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                            artist.artistId
                                        )
                                    } else {
                                        RustCoreBridge.addFavoriteArtist(
                                            com.radiogolha.mobile.ui.home.requireUserDbPath(),
                                            artist.artistId,
                                            artist.type
                                        )
                                    }
                                    reloadToken += 1
                                    artistForQuickActions = null
                                }
                            }
                        )

                        ArtistOptionRow(
                            icon = GolhaIcon.Info,
                            text = "رفتن به صفحه هنرمند",
                            onClick = {
                                push(AppRoute.ArtistDetail(artist.artistId))
                                artistForQuickActions = null
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArtistOptionRow(icon: GolhaIcon, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        GolhaLineIcon(icon = icon, modifier = Modifier.size(20.dp), tint = GolhaColors.SecondaryText)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = GolhaColors.PrimaryText)
    }
}

private sealed interface AppRoute {
    data class Root(val tab: AppTab) : AppRoute
    data object Singers : AppRoute
    data object Musicians : AppRoute
    data class ArtistDetail(val id: Long) : AppRoute
    data class CategoryPrograms(
        val category: com.radiogolha.mobile.ui.home.ProgramUiModel
    ) : AppRoute
    data class ProgramEpisodeDetail(val programId: Long) : AppRoute
    data class ModeDetail(val modeId: Long, val modeName: String) : AppRoute
    data class PlaylistDetail(val playlistId: Long, val playlistName: String) : AppRoute
    data object NowPlaying : AppRoute
    data class OrchestraDetail(val id: Long, val name: String) : AppRoute
    data class DuetDetail(
        val duet: com.radiogolha.mobile.ui.home.DuetPairUiModel
    ) : AppRoute
}

@Serializable
private data class PlaylistBridgeDto(
    val id: Long = 0,
    val name: String = "",
    val trackIds: List<Long> = emptyList(),
)

private data class ManualPlaylistDetail(
    val id: Long,
    val name: String,
    val trackIds: List<Long>,
)

private val playlistJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private fun escapeJson(value: String): String =
    value.replace("\\", "\\\\").replace("\"", "\\\"")

private fun loadManualPlaylistDetail(playlistId: Long): ManualPlaylistDetail? {
    return try {
        val payload = RustCoreBridge.getPlaylist(com.radiogolha.mobile.ui.home.requireUserDbPath(), playlistId)
        if (payload.isBlank() || payload == "null" || payload == "{}") return null
        val dto = playlistJson.decodeFromString<PlaylistBridgeDto>(payload)
        ManualPlaylistDetail(
            id = dto.id,
            name = dto.name,
            trackIds = dto.trackIds
        )
    } catch (_: Exception) {
        null
    }
}

private fun buildBottomNavItems(selectedTab: AppTab): List<BottomNavItemUiModel> = listOf(
    BottomNavItemUiModel(
        label = "خانه",
        icon = GolhaIcon.Home,
        tab = AppTab.Home,
        selected = selectedTab == AppTab.Home,
    ),
    BottomNavItemUiModel(
        label = "جستجو",
        icon = GolhaIcon.Search,
        tab = AppTab.Search,
        selected = selectedTab == AppTab.Search,
    ),
    BottomNavItemUiModel(
        label = "کتابخانه",
        icon = GolhaIcon.Library,
        tab = AppTab.Library,
        selected = selectedTab == AppTab.Library,
    ),
    BottomNavItemUiModel(
        label = "حساب من",
        icon = GolhaIcon.Account,
        tab = AppTab.Account,
        selected = selectedTab == AppTab.Account,
    ),
)
