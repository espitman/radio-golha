package com.radiogolha.mobile

import androidx.compose.runtime.Composable
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
import com.radiogolha.mobile.ui.orchestras.OrchestraDetailScreen
import com.radiogolha.mobile.ui.orchestras.loadOrchestrasUiState
import com.radiogolha.mobile.ui.programs.loadProgramsUiState
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.SingersScreen
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import com.radiogolha.mobile.ui.programs.toTrackUiModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    val searchState = remember { com.radiogolha.mobile.ui.search.SearchState() }
    
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

    GolhaAppTheme {
        val currentRoute = currentStack.last()
        val bottomNavItems = buildBottomNavItems(selectedTab)
        
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
                )
            }

            is AppRoute.ArtistDetail -> {
                com.radiogolha.mobile.ui.artists.ArtistDetailScreen(
                    artistId = currentRoute.id,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    onPlayTrack = { player.play(it) },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onToggleFavorite = {
                        scope.launch {
                            val isFavStr = RustCoreBridge.isFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.id)
                            if (isFavStr == "true") {
                                RustCoreBridge.removeFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.id)
                            } else {
                                RustCoreBridge.addFavoriteArtist(com.radiogolha.mobile.ui.home.requireUserDbPath(), currentRoute.id, "artist")
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
                )
            }

            is AppRoute.ProgramEpisodeDetail -> {
                com.radiogolha.mobile.ui.programs.ProgramEpisodeDetailScreen(
                    programId = currentRoute.programId,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { onTabSelected(it) },
                    onBackClick = { pop() },
                    onArtistClick = { id -> push(AppRoute.ArtistDetail(id)) },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                    onTrackLongClick = { selectedTrackForOptions = it },
                    onSeek = { player.seekTo(it) },
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
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { player.togglePlayback() },
                )
            }

            is AppRoute.Root -> {
                when (currentRoute.tab) {
                    AppTab.Home -> {
                        HomeScreen(
                            state = homeState?.copy(bottomNavItems = bottomNavItems),
                            duets = homeState?.duets ?: emptyList(),
                            bottomNavItems = bottomNavItems,
                            onOpenAllSingers = { push(AppRoute.Singers) },
                            onOpenAllMusicians = { push(AppRoute.Musicians) },
                            onRefreshTopTracks = { reloadToken += 1 },
                            isRefreshingTopTracks = isTopTracksRefreshing,
                            onTrackClick = { track -> push(AppRoute.ProgramEpisodeDetail(track.id)) },
                            onSingerClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onMusicianClick = { id -> push(AppRoute.ArtistDetail(id)) },
                            onDuetClick = { duet -> push(AppRoute.DuetDetail(duet)) },
                            onProgramClick = { category -> push(AppRoute.CategoryPrograms(category)) },
                            onBottomNavSelected = { onTabSelected(it) },
                            onPlayTrack = { player.play(it) },
                            onTrackLongClick = { selectedTrackForOptions = it },
                            recentlyPlayed = recentlyPlayed,
                            savedPlaylists = savedPlaylists,
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                        )
                    }

                    AppTab.Search -> {
                        com.radiogolha.mobile.ui.search.SearchScreen(
                            state = searchState,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = { onTabSelected(it) },
                            onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) },
                            onPlayTrack = { player.play(it) },
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                        )
                    }

                    AppTab.Library -> {
                        LibraryScreen(
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
                            onTrackClick = { trackId -> push(AppRoute.ProgramEpisodeDetail(trackId)) }
                        )
                    }

                    AppTab.Account -> {
                        SettingsScreen(
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = { onTabSelected(it) },
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
                            currentTrack = currentTrack,
                            isPlayerPlaying = isPlayerPlaying,
                            isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs,
                            currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { player.togglePlayback() },
                            onExpandPlayer = { /* maybe open full player? */ }
                        )
                    }
                }
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
    data class OrchestraDetail(val id: Long, val name: String) : AppRoute
    data class DuetDetail(
        val duet: com.radiogolha.mobile.ui.home.DuetPairUiModel
    ) : AppRoute
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
