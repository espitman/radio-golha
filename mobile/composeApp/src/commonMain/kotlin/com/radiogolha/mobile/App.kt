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
import com.radiogolha.mobile.ui.programs.loadProgramsUiState
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.SingersScreen
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun App() {
    val navigationStack = remember { mutableStateListOf<AppRoute>(AppRoute.Root(AppTab.Home)) }
    var reloadToken by remember { mutableStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentRoute by remember {
        androidx.compose.runtime.derivedStateOf { navigationStack.lastOrNull() ?: AppRoute.Root(AppTab.Home) }
    }
    val selectedTab by remember {
        androidx.compose.runtime.derivedStateOf { (navigationStack.firstOrNull() as? AppRoute.Root)?.tab ?: AppTab.Home }
    }
    
    var isProgramsLoading by remember { mutableStateOf(false) }
    var isSingersLoading by remember { mutableStateOf(false) }
    var isMusiciansLoading by remember { mutableStateOf(false) }

    PlatformBackHandler(enabled = navigationStack.size > 1) {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
    }

    var trackPool by remember { mutableStateOf<List<TrackUiModel>>(emptyList()) }
    var isTopTracksRefreshing by remember { mutableStateOf(false) }

    val homeState by produceState<HomeUiState?>(initialValue = null, key1 = reloadToken) {
        if (value != null && trackPool.isNotEmpty()) {
            // 1. Show stored 5 instantly
            val currentReserve = trackPool
            value = value?.copy(topTracks = currentReserve)
            
            // 2. Refresh icon animation
            isTopTracksRefreshing = true
            
            // 3. Get 5 more and replace stored ones (refill pool)
            val newReserve = withContext(Dispatchers.Default) {
                delay(400) 
                runCatching { loadTopTracks() }.getOrNull()
            }
            if (newReserve != null) {
                trackPool = newReserve
            }
            isTopTracksRefreshing = false
        } else {
            // Initial load: Get 10, show 5, store 5
            isTopTracksRefreshing = true
            val newState = withContext(Dispatchers.Default) {
                runCatching { loadHomeUiState() }.getOrNull()
            }
            if (newState != null) {
                val initiallyVisible = newState.topTracks.take(5)
                trackPool = newState.topTracks.drop(5)
                value = newState.copy(topTracks = initiallyVisible)
            }
            isTopTracksRefreshing = false
        }
    }

    val singers by produceState(
        initialValue = emptyList<com.radiogolha.mobile.ui.home.SingerListItemUiModel>(),
        key1 = reloadToken,
        key2 = currentRoute,
    ) {
        if (currentRoute != AppRoute.Singers) {
            value = emptyList()
            return@produceState
        }

        isSingersLoading = true
        value = runCatching {
            withContext(Dispatchers.Default) { loadSingersUiState() }
        }.getOrNull() ?: emptyList()
        isSingersLoading = false
    }
    val musicians by produceState(
        initialValue = emptyList<com.radiogolha.mobile.ui.home.MusicianListItemUiModel>(),
        key1 = reloadToken,
        key2 = currentRoute,
    ) {
        if (currentRoute != AppRoute.Musicians) {
            value = emptyList()
            return@produceState
        }

        isMusiciansLoading = true
        value = runCatching {
            withContext(Dispatchers.Default) { loadMusiciansUiState() }
        }.getOrNull() ?: emptyList()
        isMusiciansLoading = false
    }
    
    val programs by produceState(
        initialValue = emptyList<com.radiogolha.mobile.ui.home.ProgramUiModel>(),
        key1 = reloadToken,
        key2 = selectedTab,
    ) {
        if (selectedTab != AppTab.Library) {
            value = emptyList()
            return@produceState
        }
        
        isProgramsLoading = true
        value = runCatching {
            withContext(Dispatchers.Default) { loadProgramsUiState() }
        }.getOrNull() ?: emptyList()
        isProgramsLoading = false
    }

    val bottomNavItems = remember(selectedTab) {
        buildBottomNavItems(selectedTab)
    }

    GolhaAppTheme {
        when (currentRoute) {
            AppRoute.Singers -> {
                SingersScreen(
                    singers = singers,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = {
                        navigationStack.resetToRoot(it)
                    },
                    onBackClick = { navigationStack.pop() },
                    onSingerClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                )
            }

            AppRoute.Musicians -> {
                MusiciansScreen(
                    musicians = musicians,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = {
                        navigationStack.resetToRoot(it)
                    },
                    onBackClick = { navigationStack.pop() },
                    onMusicianClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                )
            }
            
            is AppRoute.ArtistDetail -> {
                com.radiogolha.mobile.ui.artists.ArtistDetailScreen(
                    artistId = (currentRoute as AppRoute.ArtistDetail).id,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { navigationStack.resetToRoot(it) },
                    onBackClick = { navigationStack.pop() },
                    onArtistClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                )
            }

            is AppRoute.CategoryPrograms -> {
                val route = currentRoute as AppRoute.CategoryPrograms
                com.radiogolha.mobile.ui.programs.CategoryProgramsScreen(
                    categoryTitle = route.category.title,
                    programs = route.programs,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = { navigationStack.resetToRoot(it) },
                    onBackClick = { navigationStack.pop() },
                    onTrackClick = { /* Handle track click if needed */ },
                    onPlayTrack = { /* Handle play */ },
                    onArtistClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                    onProgramClick = { /* If clicking a program inside category does something */ }
                )
            }

            is AppRoute.Root -> {
                val rootRoute = currentRoute as AppRoute.Root
                when (rootRoute.tab) {
                    AppTab.Home, AppTab.Search -> {
                        HomeScreen(
                            state = homeState?.copy(bottomNavItems = bottomNavItems),
                            bottomNavItems = bottomNavItems,
                            onOpenAllSingers = { navigationStack.push(AppRoute.Singers) },
                            onOpenAllMusicians = { navigationStack.push(AppRoute.Musicians) },
                            onRefreshTopTracks = { reloadToken += 1 },
                            isRefreshingTopTracks = isTopTracksRefreshing,
                            onSingerClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                            onMusicianClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                            onBottomNavSelected = {
                                navigationStack.resetToRoot(it)
                            },
                        )
                    }

                    AppTab.Library -> {
                        LibraryScreen(
                            programs = programs,
                            singers = emptyList(), 
                            musicians = emptyList(),
                            isProgramsLoading = isProgramsLoading,
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = {
                                navigationStack.resetToRoot(it)
                            },
                            onProgramClick = { category ->
                                scope.launch {
                                    val catPrograms = withContext(Dispatchers.Default) {
                                        com.radiogolha.mobile.ui.programs.loadCategoryPrograms(category.title)
                                    }
                                    navigationStack.push(AppRoute.CategoryPrograms(category, catPrograms))
                                }
                            },
                            onSingerClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                            onMusicianClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                        )
                    }

                    AppTab.Account -> {
                        SettingsScreen(
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = {
                                navigationStack.resetToRoot(it)
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
                                            navigationStack.resetToRoot(AppTab.Home)
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
        val category: com.radiogolha.mobile.ui.home.ProgramUiModel,
        val programs: List<com.radiogolha.mobile.ui.home.CategoryProgramUiModel>
    ) : AppRoute
}

private fun MutableList<AppRoute>.resetToRoot(tab: AppTab) {
    clear()
    add(AppRoute.Root(tab))
}

private fun MutableList<AppRoute>.push(route: AppRoute) {
    if (lastOrNull() != route) {
        add(route)
    }
}

private fun MutableList<AppRoute>.pop() {
    if (size > 1) {
        removeAt(lastIndex)
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
