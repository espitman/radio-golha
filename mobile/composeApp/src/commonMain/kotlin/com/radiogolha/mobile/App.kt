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
    val singers by produceState(initialValue = emptyList(), reloadToken) {
        isSingersLoading = true
        value = loadSingersUiState()
        isSingersLoading = false
    }

    var isMusiciansLoading by remember { mutableStateOf(false) }
    val musicians by produceState(initialValue = emptyList(), reloadToken) {
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

    // duetPairs is now part of homeState



    val isTopTracksRefreshing by produceState(initialValue = false, reloadToken) {
        value = false
    }

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
                        )
                    }
                }
            }
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
