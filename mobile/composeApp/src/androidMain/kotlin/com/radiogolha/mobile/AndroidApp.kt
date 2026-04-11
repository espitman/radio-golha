package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.radiogolha.mobile.debug.importDebugDatabase
import com.radiogolha.mobile.debug.isDebugDatabaseToolsEnabled
import com.radiogolha.mobile.debug.showDebugToast
import com.radiogolha.mobile.theme.GolhaAppTheme
import com.radiogolha.mobile.ui.home.AppTab
import com.radiogolha.mobile.ui.home.BottomNavItemUiModel
import com.radiogolha.mobile.ui.home.GolhaIcon
import com.radiogolha.mobile.ui.home.HomeScreen
import com.radiogolha.mobile.ui.home.HomeUiState
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.musicians.MusiciansScreen
import com.radiogolha.mobile.ui.musicians.loadMusiciansUiState
import com.radiogolha.mobile.ui.root.TabRootScreen
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.SingersScreen
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AndroidApp() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val playerManager = remember(context) { GolhaPlayerManager(context) }
    DisposableEffect(playerManager) {
        onDispose { playerManager.release() }
    }
    val currentTrack by playerManager.currentTrack.collectAsState()
    val isPlayerPlaying by playerManager.isPlaying.collectAsState()
    val isPlayerLoading by playerManager.isLoading.collectAsState()
    val currentPlaybackPositionMs by playerManager.currentPositionMs.collectAsState()
    val currentPlaybackDurationMs by playerManager.durationMs.collectAsState()
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

    // --- SHARED PERSISTENT STATE ---
    var reloadToken by remember { mutableIntStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    
    // Library State
    var librarySingers by remember { mutableStateOf<List<com.radiogolha.mobile.ui.home.SingerListItemUiModel>>(emptyList()) }
    var libraryMusicians by remember { mutableStateOf<List<com.radiogolha.mobile.ui.home.MusicianListItemUiModel>>(emptyList()) }
    var libraryPrograms by remember { mutableStateOf<List<com.radiogolha.mobile.ui.home.ProgramUiModel>>(emptyList()) }
    var isLibraryLoading by remember { mutableStateOf(false) }

    // Category Programs State
    var categoryPrograms by remember { mutableStateOf<List<com.radiogolha.mobile.ui.home.CategoryProgramUiModel>>(emptyList()) }
    var lastCategoryTitle by remember { mutableStateOf("") }
    var isCategoryLoading by remember { mutableStateOf(false) }

    // Home State
    var homeUiState by remember { mutableStateOf<HomeUiState?>(null) }
    var isHomeLoading by remember { mutableStateOf(false) }
    var isRefreshingTopTracks by remember { mutableStateOf(false) }
    var prefetchedTopTracks by remember { mutableStateOf<List<com.radiogolha.mobile.ui.home.TrackUiModel>>(emptyList()) }
    
    val visibleTrackCount = 5

    // --- DATA LOADING LOGIC ---
    LaunchedEffect(reloadToken) {
        isHomeLoading = true
        isLibraryLoading = true
        
        withContext(Dispatchers.Default) {
            // Load Home
            val hState = runCatching { loadHomeUiState() }.getOrNull()
            if (hState != null) {
                homeUiState = hState.copy(topTracks = hState.topTracks.take(visibleTrackCount))
                prefetchedTopTracks = hState.topTracks.drop(visibleTrackCount).take(visibleTrackCount)
            }
            isHomeLoading = false

            // Load Library
            librarySingers = runCatching { loadSingersUiState() }.getOrDefault(emptyList())
            libraryMusicians = runCatching { loadMusiciansUiState() }.getOrDefault(emptyList())
            libraryPrograms = runCatching { com.radiogolha.mobile.ui.programs.loadProgramsUiState() }.getOrDefault(emptyList())
            isLibraryLoading = false
        }
    }

    // Unified Refresh Logic
    val onRefreshTracks: () -> Unit = {
        if (!isRefreshingTopTracks) {
            scope.launch {
                isRefreshingTopTracks = true
                // 1. If we have prefetched tracks, use them immediately
                if (prefetchedTopTracks.size >= visibleTrackCount) {
                    homeUiState = homeUiState?.copy(topTracks = prefetchedTopTracks.take(visibleTrackCount))
                    prefetchedTopTracks = emptyList()
                }
                
                // 2. Always fetch fresh tracks for the NEXT refresh
                val fresh = withContext(Dispatchers.Default) {
                    runCatching { com.radiogolha.mobile.ui.home.loadTopTracks() }.getOrNull()
                }
                
                if (fresh != null) {
                    // If we didn't have pre-fetched ones, use these now
                    if (homeUiState?.topTracks.isNullOrEmpty() || homeUiState?.topTracks?.size ?: 0 < visibleTrackCount) {
                        homeUiState = homeUiState?.copy(topTracks = fresh.take(visibleTrackCount))
                    } else {
                        // Otherwise store for next time
                        prefetchedTopTracks = fresh.take(visibleTrackCount)
                    }
                }
                isRefreshingTopTracks = false
            }
        }
    }

    GolhaAppTheme {
        NavHost(
            navController = navController,
            startDestination = AndroidRoute.Home.route,
        ) {
            composable(AndroidRoute.Home.route) {
                HomeScreen(
                    state = if (isHomeLoading && homeUiState == null) {
                        null
                    } else {
                        homeUiState?.copy(bottomNavItems = bottomNavItems)
                    },
                    bottomNavItems = bottomNavItems,
                    onOpenAllSingers = {
                        navController.navigate(AndroidRoute.Library.createRoute("singers")) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onOpenAllMusicians = {
                        navController.navigate(AndroidRoute.Library.createRoute("musicians")) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isRefreshingTopTracks = isRefreshingTopTracks,
                    onRefreshTopTracks = onRefreshTracks,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onPlayTrack = { track -> playerManager.play(track) },
                    onTrackClick = { track ->
                        navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(track.id))
                    },
                    onProgramClick = { program ->
                        navController.navigate(AndroidRoute.CategoryPrograms.createRoute(program.title))
                    },
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(AndroidRoute.Search.route) {
                TabRootScreen(
                    title = "جستجو",
                    subtitle = "جستجو و فیلترهای اپ در این تب قرار می‌گیرد.",
                    bottomNavItems = bottomNavItems,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onTrackClick = { trackId ->
                        navController.navigate("program_detail/$trackId")
                    },
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }

            composable(
                route = AndroidRoute.Library.route + "?tab={tab}",
                arguments = listOf(
                    androidx.navigation.navArgument("tab") {
                        type = androidx.navigation.NavType.StringType
                        nullable = true
                        defaultValue = "programs"
                    }
                )
            ) { backStackEntry ->
                val tabName = backStackEntry.arguments?.getString("tab") ?: "programs"
                val initialTab = com.radiogolha.mobile.ui.library.LibraryTab.entries.find { 
                    it.name.equals(tabName, ignoreCase = true) 
                } ?: com.radiogolha.mobile.ui.library.LibraryTab.Programs

                com.radiogolha.mobile.ui.library.LibraryScreen(
                    initialTab = initialTab,
                    programs = libraryPrograms,
                    singers = librarySingers,
                    musicians = libraryMusicians,
                    isProgramsLoading = isLibraryLoading,
                    isSingersLoading = isLibraryLoading,
                    isMusiciansLoading = isLibraryLoading,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onProgramClick = { program ->
                        navController.navigate(AndroidRoute.CategoryPrograms.createRoute(program.title))
                    },
                    onTrackClick = { trackId ->
                        navController.navigate("program_detail/$trackId")
                    }
                )
            }

            composable(
                route = AndroidRoute.CategoryPrograms.route,
                arguments = listOf(
                    androidx.navigation.navArgument("title") { type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                
                LaunchedEffect(title) {
                    if (categoryPrograms.isNotEmpty() && lastCategoryTitle == title) {
                         return@LaunchedEffect
                    }
                    
                    isCategoryLoading = true
                    categoryPrograms = withContext(Dispatchers.Default) {
                        runCatching { 
                            com.radiogolha.mobile.ui.programs.loadCategoryPrograms(title) 
                        }.getOrDefault(emptyList())
                    }
                    lastCategoryTitle = title
                    isCategoryLoading = false
                }

                com.radiogolha.mobile.ui.programs.CategoryProgramsScreen(
                    categoryTitle = title,
                    programs = categoryPrograms,
                    isLoading = isCategoryLoading,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onProgramClick = { program ->
                        navController.navigate(AndroidRoute.ProgramEpisodeDetail.createRoute(program.id))
                    },
                    onPlayTrack = { track -> playerManager.play(track) },
                    onBackClick = { navController.popBackStack() },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onTrackClick = { trackId ->
                        navController.navigate("program_detail/$trackId")
                    }
                )
            }

            composable(
                route = AndroidRoute.ProgramEpisodeDetail.route,
                arguments = listOf(
                    androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.LongType }
                )
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getLong("id") ?: 0L
                com.radiogolha.mobile.ui.programs.ProgramEpisodeDetailScreen(
                    programId = programId,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onPlayProgram = { detail ->
                        playerManager.play(com.radiogolha.mobile.ui.home.TrackUiModel(
                            id = detail.id,
                            title = detail.title,
                            artist = detail.singers.map { it.name }.joinToString(" و ").takeIf { it.isNotBlank() } ?: "ناشناس",
                            duration = detail.duration, 
                            audioUrl = detail.audioUrl
                        ))
                    }
                )
            }

            composable(AndroidRoute.Account.route) {
                SettingsScreen(
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    isDebugDatabaseToolsEnabled = isDebugDatabaseToolsEnabled(),
                    isImportingDatabase = isImportingDatabase,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onImportDebugDatabase = {
                        if (!isImportingDatabase) {
                            scope.launch {
                                isImportingDatabase = true
                                val result = importDebugDatabase()
                                showDebugToast(result.message)
                                if (result.success) {
                                    reloadToken += 1
                                    navController.navigate(AndroidRoute.Home.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                isImportingDatabase = false
                            }
                        }
                    },
                )
            }

            composable(AndroidRoute.Singers.route) {
                val singers by produceState(initialValue = emptyList(), key1 = reloadToken) {
                    value = runCatching {
                        withContext(Dispatchers.Default) { loadSingersUiState() }
                    }.getOrNull() ?: emptyList()
                }
                SingersScreen(
                    singers = singers,
                    bottomNavItems = bottomNavItems,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }

            composable(AndroidRoute.Musicians.route) {
                val musicians by produceState(initialValue = emptyList(), key1 = reloadToken) {
                    value = runCatching {
                        withContext(Dispatchers.Default) { loadMusiciansUiState() }
                    }.getOrNull() ?: emptyList()
                }
                MusiciansScreen(
                    musicians = musicians,
                    bottomNavItems = bottomNavItems,
                    currentTrack = currentTrack,
                    isPlayerPlaying = isPlayerPlaying,
                    isPlayerLoading = isPlayerLoading,
                    currentPlaybackPositionMs = currentPlaybackPositionMs,
                    currentPlaybackDurationMs = currentPlaybackDurationMs,
                    onTogglePlayerPlayback = { playerManager.togglePlayback() },
                    onBottomNavSelected = { tab ->
                        navController.navigate(tab.toRoute().route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onBackClick = { navController.popBackStack() },
                )
            }
        }
    }
}

private sealed class AndroidRoute(val route: String) {
    data object Home : AndroidRoute("home")
    data object Search : AndroidRoute("search")
    data object Library : AndroidRoute("library") {
        fun createRoute(tab: String) = "library?tab=$tab"
    }
    data object Account : AndroidRoute("account")
    data object Singers : AndroidRoute("singers")
    data object Musicians : AndroidRoute("musicians")
    data object CategoryPrograms : AndroidRoute("category_programs/{title}") {
        fun createRoute(title: String) = "category_programs/$title"
    }
    data object ProgramEpisodeDetail : AndroidRoute("program_episode_detail/{id}") {
        fun createRoute(id: Long) = "program_episode_detail/$id"
    }
}

private fun AppTab.toRoute(): AndroidRoute = when (this) {
    AppTab.Home -> AndroidRoute.Home
    AppTab.Search -> AndroidRoute.Search
    AppTab.Library -> AndroidRoute.Library
    AppTab.Account -> AndroidRoute.Account
}

private fun androidx.navigation.NavDestination?.isOnRoute(route: String): Boolean =
    this?.hierarchy?.any { it.route == route } == true

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
