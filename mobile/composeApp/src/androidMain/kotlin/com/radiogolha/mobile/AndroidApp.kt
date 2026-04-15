package com.radiogolha.mobile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.radiogolha.mobile.debug.importDebugDatabase
import com.radiogolha.mobile.debug.isDebugDatabaseToolsEnabled
import com.radiogolha.mobile.debug.showDebugToast
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.theme.GolhaColors
import com.radiogolha.mobile.ui.artists.ArtistDetailScreen
import com.radiogolha.mobile.ui.home.*
import com.radiogolha.mobile.ui.library.*
import com.radiogolha.mobile.ui.musicians.*
import com.radiogolha.mobile.ui.programs.*
import com.radiogolha.mobile.ui.root.TabRootScreen
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.*
import com.radiogolha.mobile.ui.orchestras.*
import com.radiogolha.mobile.ui.search.SearchScreen
import com.radiogolha.mobile.ui.search.SearchState
import com.radiogolha.mobile.ui.search.PlaylistDetailScreen
import com.radiogolha.mobile.data.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidApp() {
    val context = LocalContext.current
    val playerManager = remember(context) { GolhaPlayerManager(context) }
    
    DisposableEffect(playerManager) {
        onDispose { playerManager.release() }
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    
    val currentTrack by playerManager.currentTrack.collectAsState()
    val isPlayerPlaying by playerManager.isPlaying.collectAsState()
    val isPlayerLoading by playerManager.isLoading.collectAsState()
    val currentPlaybackPositionMs by playerManager.currentPositionMs.collectAsState()
    val currentPlaybackDurationMs by playerManager.durationMs.collectAsState()
    
    // Bottom Sheet State
    var showPlayerSheet by remember { mutableStateOf(false) }
    val playerSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedTab = remember(currentDestination) {
        val route = currentDestination?.route ?: ""
        when {
            route.startsWith(AndroidRoute.Account.route) -> AppTab.Account
            route.startsWith(AndroidRoute.Library.route) -> AppTab.Library
            route.startsWith(AndroidRoute.Search.route) -> AppTab.Search
            else -> AppTab.Home
        }
    }
    val bottomNavItems = remember(selectedTab) {
        buildBottomNavItems(selectedTab)
    }
    val visibleTrackCount = 5

    // Shared State
    var reloadToken by remember { mutableIntStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    
    var librarySingers by remember { mutableStateOf<List<SingerListItemUiModel>>(emptyList()) }
    var libraryMusicians by remember { mutableStateOf<List<MusicianListItemUiModel>>(emptyList()) }
    var libraryOrchestras by remember { mutableStateOf<List<OrchestraListItemUiModel>>(emptyList()) }
    var libraryPrograms by remember { mutableStateOf<List<ProgramUiModel>>(emptyList()) }
    var isLibraryLoading by remember { mutableStateOf(false) }

    var categoryPrograms by remember { mutableStateOf<List<CategoryProgramUiModel>>(emptyList()) }
    var lastCategoryTitle by remember { mutableStateOf("") }
    var isCategoryLoading by remember { mutableStateOf(false) }

    val searchState = remember { SearchState() }
    val playlistRepo = remember { PlaylistRepository(context) }
    var savedPlaylists by remember { mutableStateOf(playlistRepo.getAll().map { SavedPlaylistUiModel(it.id, it.name) }) }
    var orderedModes by remember { mutableStateOf<List<String>>(emptyList()) }
    var enrichedDuets by remember { mutableStateOf<List<DuetPairUiModel>>(emptyList()) }
    var homeUiState by remember { mutableStateOf<HomeUiState?>(null) }
    var isHomeLoading by remember { mutableStateOf(true) }
    var isRefreshingTopTracks by remember { mutableStateOf(false) }
    var prefetchedTopTracks by remember { mutableStateOf<List<TrackUiModel>>(emptyList()) }

    LaunchedEffect(reloadToken) {
        isLibraryLoading = true
        isHomeLoading = true
        withContext(Dispatchers.Default) {
            // All queries in parallel
            val dHome = async { runCatching { loadHomeUiState() }.getOrNull() }
            val dSingers = async { runCatching { loadSingersUiState() }.getOrDefault(emptyList()) }
            val dModes = async { runCatching { loadOrderedModes() }.getOrDefault(emptyList()) }
            val dDuets = async { runCatching { loadDuetPairsConfig() }.getOrDefault(emptyList()) }
            val dMusicians = async { runCatching { loadMusiciansUiState() }.getOrDefault(emptyList()) }
            val dOrchestras = async { runCatching { loadOrchestrasUiState() }.getOrDefault(emptyList()) }
            val dPrograms = async { runCatching { com.radiogolha.mobile.ui.programs.loadProgramsUiState() }.getOrDefault(emptyList()) }
            val dSearchOpts = async {
                if (!searchState.optionsLoaded) runCatching { com.radiogolha.mobile.ui.search.loadSearchOptions() }.getOrDefault(com.radiogolha.mobile.ui.search.SearchOptionsUiState())
                else searchState.searchOptions
            }

            // Await all
            val hState = dHome.await()
            librarySingers = dSingers.await()
            orderedModes = dModes.await()
            enrichedDuets = dDuets.await()

            // Show home
            if (hState != null) {
                homeUiState = hState.copy(topTracks = hState.topTracks.take(visibleTrackCount))
                prefetchedTopTracks = hState.topTracks.drop(visibleTrackCount).take(visibleTrackCount)
            }
            isHomeLoading = false

            // Library & search options
            searchState.searchOptions = dSearchOpts.await()
            searchState.optionsLoaded = true
            libraryMusicians = dMusicians.await()
            libraryOrchestras = dOrchestras.await()
            libraryPrograms = dPrograms.await()
            isLibraryLoading = false
        }
    }

    val onRefreshTracks: () -> Unit = {
        if (!isRefreshingTopTracks) {
            scope.launch {
                isRefreshingTopTracks = true
                if (prefetchedTopTracks.size >= visibleTrackCount) {
                    homeUiState = homeUiState?.copy(topTracks = prefetchedTopTracks.take(visibleTrackCount))
                    prefetchedTopTracks = emptyList()
                }
                val fresh = withContext(Dispatchers.Default) {
                    runCatching { loadTopTracks() }.getOrNull()
                }
                if (fresh != null) {
                    if (homeUiState?.topTracks.isNullOrEmpty() || homeUiState?.topTracks?.size ?: 0 < visibleTrackCount) {
                        homeUiState = homeUiState?.copy(topTracks = fresh.take(visibleTrackCount))
                    } else {
                        prefetchedTopTracks = fresh.take(visibleTrackCount)
                    }
                }
                isRefreshingTopTracks = false
            }
        }
    }

    val onTabSelected: (AppTab) -> Unit = { tab ->
        if (tab != selectedTab) {
            val route = tab.toRoute().route
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = false }
                launchSingleTop = true
            }
        }
    }

    BackHandler(showPlayerSheet) {
        scope.launch {
            playerSheetState.hide()
            showPlayerSheet = false
        }
    }

    GolhaAppTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = AndroidRoute.Home.route,
            ) {
                composable(AndroidRoute.Home.route) {
                    HomeScreen(
                        state = if (isHomeLoading && homeUiState == null) null else homeUiState?.copy(bottomNavItems = bottomNavItems),
                        bottomNavItems = bottomNavItems,
                        duets = enrichedDuets,
                        savedPlaylists = savedPlaylists,
                        onPlaylistClick = { id -> navController.navigate(AndroidRoute.PlaylistDetail.createRoute(id)) },
                        onOpenAllSingers = { navController.navigate(AndroidRoute.Library.createRoute("singers")) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        onOpenAllMusicians = { navController.navigate(AndroidRoute.Library.createRoute("musicians")) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        isRefreshingTopTracks = isRefreshingTopTracks,
                        onRefreshTopTracks = onRefreshTracks,
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs,
                        currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onPlayTrack = { track -> playerManager.play(track) },
                        onTrackClick = { track -> if (track.id == currentTrack?.id) showPlayerSheet = true else navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(track.id)) },
                        onProgramClick = { program -> navController.navigate(AndroidRoute.CategoryPrograms.createRoute(program.title)) },
                        onSingerClick = { artistId -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId)) },
                        onMusicianClick = { artistId -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId)) },
                        onDuetClick = { duet -> navController.navigate(AndroidRoute.DuetDetail.createRoute(duet.singer1, duet.singer2)) },
                        orderedModes = orderedModes,
                        onDastgahClick = { name ->
                            val id = searchState.searchOptions.modes.find { it.name == name }?.id
                            if (id != null) navController.navigate(AndroidRoute.DastgahDetail.createRoute(id, name))
                        },
                        onExpandPlayer = { showPlayerSheet = true },
                        onBottomNavSelected = onTabSelected,
                    )
                }

                composable(AndroidRoute.Search.route) {
                    SearchScreen(
                        state = searchState,
                        bottomNavItems = bottomNavItems,
                        onBottomNavSelected = onTabSelected,
                        onProgramClick = { programId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(programId)) },
                        onPlayTrack = { track -> playerManager.play(track) },
                        onSavePlaylist = { name, filters ->
                            playlistRepo.save(name, filters)
                            savedPlaylists = playlistRepo.getAll().map { SavedPlaylistUiModel(it.id, it.name) }
                        },
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs,
                        currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) },
                        onExpandPlayer = { showPlayerSheet = true },
                    )
                }

                composable(route = AndroidRoute.Library.route + "?tab={tab}", arguments = listOf(navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = "programs" })) { backStackEntry ->
                    val tabName = backStackEntry.arguments?.getString("tab") ?: "programs"
                    val initialTab = LibraryTab.entries.find { it.name.equals(tabName, ignoreCase = true) } ?: LibraryTab.Programs
                    LibraryScreen(
                        initialTab = initialTab,
                        programs = libraryPrograms,
                        singers = librarySingers,
                        musicians = libraryMusicians,
                        orchestras = libraryOrchestras,
                        isProgramsLoading = isLibraryLoading,
                        isSingersLoading = isLibraryLoading,
                        isMusiciansLoading = isLibraryLoading,
                        bottomNavItems = bottomNavItems,
                        onExpandPlayer = { showPlayerSheet = true },
                        onBottomNavSelected = onTabSelected,
                        onProgramClick = { program -> navController.navigate(AndroidRoute.CategoryPrograms.createRoute(program.title)) },
                        onSingerClick = { id -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(id)) },
                        onMusicianClick = { id -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(id)) },
                        onOrchestraClick = { id ->
                            val orchestra = libraryOrchestras.find { it.id == id }
                            if (orchestra != null) {
                                navController.navigate(AndroidRoute.OrchestraDetail.createRoute(id, orchestra.name))
                            }
                        },
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs,
                        currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) }
                    )
                }

                composable(route = AndroidRoute.CategoryPrograms.route, arguments = listOf(navArgument("title") { type = NavType.StringType })) { backStackEntry ->
                    val title = backStackEntry.arguments?.getString("title") ?: ""
                    LaunchedEffect(title) { if (title != lastCategoryTitle) { categoryPrograms = emptyList(); isCategoryLoading = true; try { categoryPrograms = withContext(Dispatchers.Default) { loadCategoryPrograms(title) } } finally { isCategoryLoading = false; lastCategoryTitle = title } } }
                    CategoryProgramsScreen(categoryTitle = title, programs = categoryPrograms, isLoading = isCategoryLoading, bottomNavItems = bottomNavItems, onExpandPlayer = { showPlayerSheet = true }, onBottomNavSelected = onTabSelected, onProgramClick = { program -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(program.id)) }, onPlayTrack = { track -> playerManager.play(track) }, onBackClick = { navController.popBackStack() }, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) })
                }

                composable(route = AndroidRoute.ProgramEpisodeDetail.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStackEntry ->
                    val programId = backStackEntry.arguments?.getLong("id") ?: 0L
                    ProgramEpisodeDetailScreen(programId = programId, bottomNavItems = bottomNavItems, onExpandPlayer = { showPlayerSheet = true }, onBottomNavSelected = onTabSelected, onBackClick = { navController.popBackStack() }, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onSeek = { pos -> playerManager.seekTo(pos) }, onArtistClick = { artistId -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId)) }, onPlayProgram = { detail -> val artistImages = (detail.singers.mapNotNull { it.avatar } + detail.performers.mapNotNull { it.avatar } + detail.composers.mapNotNull { it.avatar } + detail.arrangers.mapNotNull { it.avatar } + detail.orchestras.mapNotNull { it.avatar }).distinct(); playerManager.play(TrackUiModel(id = detail.id, title = detail.title, artist = detail.singers.map { it.name }.joinToString(" و ").takeIf { it.isNotBlank() } ?: "ناشناس", duration = detail.duration, audioUrl = detail.audioUrl, artistImages = artistImages)) })
                }

                composable(route = AndroidRoute.ArtistDetail.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStackEntry ->
                    val artistId = backStackEntry.arguments?.getLong("id") ?: 0L
                    ArtistDetailScreen(artistId = artistId, bottomNavItems = bottomNavItems, onExpandPlayer = { showPlayerSheet = true }, onBottomNavSelected = onTabSelected, onBackClick = { navController.popBackStack() }, onProgramClick = { program -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(program.id)) }, onPlayTrack = { track -> playerManager.play(track) }, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) })
                }

                composable(route = AndroidRoute.OrchestraDetail.route, arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("name") { type = NavType.StringType })) { backStackEntry ->
                    val orchestraId = backStackEntry.arguments?.getLong("id") ?: 0L
                    val orchestraName = backStackEntry.arguments?.getString("name") ?: ""
                    OrchestraDetailScreen(
                        orchestraId = orchestraId,
                        orchestraName = orchestraName,
                        bottomNavItems = bottomNavItems,
                        onBottomNavSelected = onTabSelected,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) },
                        onArtistClick = { id -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(id)) },
                        currentTrack = currentTrack,
                        isPlayerPlaying = isPlayerPlaying,
                        isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs,
                        currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onExpandPlayer = { showPlayerSheet = true },
                    )
                }

                composable(route = AndroidRoute.DuetDetail.route, arguments = listOf(navArgument("singer1") { type = NavType.StringType }, navArgument("singer2") { type = NavType.StringType })) { backStackEntry ->
                    val s1 = backStackEntry.arguments?.getString("singer1") ?: ""
                    val s2 = backStackEntry.arguments?.getString("singer2") ?: ""
                    fun findAvatar(name: String) = librarySingers.find { it.name == name }?.imageUrl
                    DuetDetailScreen(
                        singer1 = s1, singer2 = s2,
                        singer1Avatar = findAvatar(s1), singer2Avatar = findAvatar(s2),
                        bottomNavItems = bottomNavItems,
                        onBottomNavSelected = onTabSelected,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { trackId -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(trackId)) },
                        onPlayTrack = { track -> playerManager.play(track) },
                        currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onExpandPlayer = { showPlayerSheet = true },
                    )
                }

                composable(route = AndroidRoute.PlaylistDetail.route, arguments = listOf(navArgument("id") { type = NavType.LongType })) { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getLong("id") ?: 0L
                    val entry = remember(playlistId) { playlistRepo.getById(playlistId) }
                    val filters = remember(entry) { entry?.let { playlistRepo.parseFilters(it) } }
                    if (entry != null && filters != null) {
                        val playlistName = entry.name
                        val singerIdToName = remember(searchState.searchOptions) {
                            searchState.searchOptions.singers.associate { it.id to it.name }
                        }
                        val singerAvatarByName = remember(librarySingers) {
                            librarySingers.filter { it.imageUrl != null }.associate { it.name to it.imageUrl!! }
                        }
                        PlaylistDetailScreen(
                            playlistName = playlistName,
                            filters = filters!!,
                            singerIdToName = singerIdToName,
                            singerAvatarsByName = singerAvatarByName,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = onTabSelected,
                            onBackClick = { navController.popBackStack() },
                            onRename = { newName ->
                                playlistRepo.rename(playlistId, newName)
                                savedPlaylists = playlistRepo.getAll().map { SavedPlaylistUiModel(it.id, it.name) }
                            },
                            onDelete = {
                                playlistRepo.delete(playlistId)
                                savedPlaylists = playlistRepo.getAll().map { SavedPlaylistUiModel(it.id, it.name) }
                                navController.popBackStack()
                            },
                            onProgramClick = { id -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(id)) },
                            onPlayTrack = { track -> playerManager.play(track) },
                            currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading,
                            currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs,
                            onTogglePlayerPlayback = { playerManager.togglePlayback() },
                            onTrackClick = { id -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(id)) },
                            onExpandPlayer = { showPlayerSheet = true },
                        )
                    }
                }

                composable(route = AndroidRoute.DastgahDetail.route, arguments = listOf(navArgument("id") { type = NavType.LongType }, navArgument("name") { type = NavType.StringType })) { backStackEntry ->
                    val modeId = backStackEntry.arguments?.getLong("id") ?: 0L
                    val modeName = backStackEntry.arguments?.getString("name") ?: ""
                    ModeDetailScreen(
                        modeId = modeId, modeName = modeName,
                        bottomNavItems = bottomNavItems,
                        onBottomNavSelected = onTabSelected,
                        onBackClick = { navController.popBackStack() },
                        onTrackClick = { id -> navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(id)) },
                        onPlayTrack = { track -> playerManager.play(track) },
                        onArtistClick = { id -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(id)) },
                        currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading,
                        currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs,
                        onTogglePlayerPlayback = { playerManager.togglePlayback() },
                        onExpandPlayer = { showPlayerSheet = true },
                    )
                }

                composable(AndroidRoute.Account.route) {
                    SettingsScreen(bottomNavItems = bottomNavItems, onExpandPlayer = { showPlayerSheet = true }, onBottomNavSelected = onTabSelected, isDebugDatabaseToolsEnabled = isDebugDatabaseToolsEnabled(), isImportingDatabase = isImportingDatabase, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onImportDebugDatabase = { if (!isImportingDatabase) { scope.launch { isImportingDatabase = true; val result = importDebugDatabase(); showDebugToast(result.message); if (result.success) { reloadToken += 1; navController.navigate(AndroidRoute.Home.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } } ; isImportingDatabase = false } } })
                }
                
                composable(AndroidRoute.Singers.route) { 
                    val singers by produceState(initialValue = emptyList<SingerListItemUiModel>(), key1 = reloadToken) { value = runCatching { withContext(Dispatchers.Default) { loadSingersUiState() } }.getOrNull() ?: emptyList() }
                    SingersScreen(singers = singers, bottomNavItems = bottomNavItems, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onBottomNavSelected = onTabSelected, onBackClick = { navController.popBackStack() }, onSingerClick = { artistId -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId)) }, onExpandPlayer = { showPlayerSheet = true })
                }

                composable(AndroidRoute.Musicians.route) {
                    val musicians by produceState(initialValue = emptyList<MusicianListItemUiModel>(), key1 = reloadToken) { value = runCatching { withContext(Dispatchers.Default) { loadMusiciansUiState() } }.getOrNull() ?: emptyList() }
                    MusiciansScreen(musicians = musicians, bottomNavItems = bottomNavItems, currentTrack = currentTrack, isPlayerPlaying = isPlayerPlaying, isPlayerLoading = isPlayerLoading, currentPlaybackPositionMs = currentPlaybackPositionMs, currentPlaybackDurationMs = currentPlaybackDurationMs, onTogglePlayerPlayback = { playerManager.togglePlayback() }, onBottomNavSelected = onTabSelected, onBackClick = { navController.popBackStack() }, onMusicianClick = { artistId -> navController.navigate(AndroidRoute.ArtistDetail.createRoute(artistId)) }, onExpandPlayer = { showPlayerSheet = true })
                }
            }

            if (showPlayerSheet) {
                val playerDarkBg = Color(0xFF0A1628)
                ModalBottomSheet(
                    onDismissRequest = { showPlayerSheet = false },
                    sheetState = playerSheetState,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .size(width = 32.dp, height = 4.dp)
                                .background(Color.White.copy(alpha = 0.25f), CircleShape)
                        )
                    },
                    containerColor = playerDarkBg,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    scrimColor = Color.Black.copy(alpha = 0.45f),
                ) {
                    com.radiogolha.mobile.ui.player.NowPlayingScreen(
                        currentTrack = currentTrack,
                        isPlaying = isPlayerPlaying,
                        isLoading = isPlayerLoading,
                        currentPositionMs = currentPlaybackPositionMs,
                        durationMs = currentPlaybackDurationMs,
                        onTogglePlayback = { playerManager.togglePlayback() },
                        onSeek = { pos -> playerManager.seekTo(pos) },
                        onBackClick = {
                            scope.launch {
                                playerSheetState.hide()
                                showPlayerSheet = false
                            }
                        },
                        onInfoClick = {
                            scope.launch {
                                playerSheetState.hide()
                                showPlayerSheet = false
                            }
                            currentTrack?.id?.let { id ->
                                navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(id))
                            }
                        },
                        onSeekBack10 = { playerManager.seekTo((currentPlaybackPositionMs - 10_000L).coerceAtLeast(0L)) },
                        onSeekForward10 = { playerManager.seekTo((currentPlaybackPositionMs + 10_000L).coerceAtMost(currentPlaybackDurationMs)) },
                        onVisibilityChanged = { visible ->
                            val act = context as? android.app.Activity ?: return@NowPlayingScreen
                            val ctrl = androidx.core.view.WindowCompat.getInsetsController(act.window, act.window.decorView)
                            ctrl.isAppearanceLightStatusBars = !visible
                        },
                    )
                }
            }

        }
    }
}

private sealed class AndroidRoute(val route: String) {
    data object Home : AndroidRoute("home")
    data object Search : AndroidRoute("search")
    data object Library : AndroidRoute("library") { fun createRoute(tab: String) = "library?tab=$tab" }
    data object Account : AndroidRoute("account")
    data object Singers : AndroidRoute("singers")
    data object Musicians : AndroidRoute("musicians")
    data object ArtistDetail : AndroidRoute("artist_detail/{id}") { fun createRoute(id: Long) = "artist_detail/$id" }
    data object CategoryPrograms : AndroidRoute("category_programs/{title}") { fun createRoute(title: String) = "category_programs/$title" }
    data object ProgramEpisodeDetail : AndroidRoute("program_episode_detail/{id}") { fun createRoute(id: Long) = "program_episode_detail/$id" }
    data object OrchestraDetail : AndroidRoute("orchestra_detail/{id}/{name}") { fun createRoute(id: Long, name: String) = "orchestra_detail/$id/$name" }
    data object DuetDetail : AndroidRoute("duet_detail/{singer1}/{singer2}") { fun createRoute(s1: String, s2: String) = "duet_detail/$s1/$s2" }
    data object PlaylistDetail : AndroidRoute("playlist_detail/{id}") { fun createRoute(id: Long) = "playlist_detail/$id" }
    data object DastgahDetail : AndroidRoute("dastgah_detail/{id}/{name}") { fun createRoute(id: Long, name: String) = "dastgah_detail/$id/$name" }
}

private fun AppTab.toRoute(): AndroidRoute = when (this) {
    AppTab.Home -> AndroidRoute.Home
    AppTab.Search -> AndroidRoute.Search
    AppTab.Library -> AndroidRoute.Library
    AppTab.Account -> AndroidRoute.Account
}

private fun buildBottomNavItems(selectedTab: AppTab): List<BottomNavItemUiModel> = listOf(
    BottomNavItemUiModel(label = "خانه", icon = GolhaIcon.Home, tab = AppTab.Home, selected = selectedTab == AppTab.Home),
    BottomNavItemUiModel(label = "کتابخانه", icon = GolhaIcon.Library, tab = AppTab.Library, selected = selectedTab == AppTab.Library),
    BottomNavItemUiModel(label = "جستجو", icon = GolhaIcon.Search, tab = AppTab.Search, selected = selectedTab == AppTab.Search),
    BottomNavItemUiModel(label = "حساب من", icon = GolhaIcon.Account, tab = AppTab.Account, selected = selectedTab == AppTab.Account),
)
