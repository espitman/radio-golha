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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun App() {
    val navigationStack = remember { mutableStateListOf<AppRoute>(AppRoute.Root(AppTab.Home)) }
    var reloadToken by remember { mutableStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val homeState by produceState<HomeUiState?>(initialValue = null, reloadToken) {
        value = loadHomeUiState()
    }

    val programs by produceState(initialValue = emptyList(), reloadToken) {
        value = loadProgramsUiState()
    }

    val singers by produceState(initialValue = emptyList(), reloadToken) {
        value = loadSingersUiState()
    }

    val musicians by produceState(initialValue = emptyList(), reloadToken) {
        value = loadMusiciansUiState()
    }

    val orchestras by produceState<List<com.radiogolha.mobile.ui.home.OrchestraListItemUiModel>>(initialValue = emptyList(), reloadToken) {
        println("DEBUG: Loading orchestras...")
        value = loadOrchestrasUiState()
        println("DEBUG: Orchestras loaded: ${value.size}")
    }

    val isProgramsLoading = programs.isEmpty()

    val isTopTracksRefreshing by produceState(initialValue = false, reloadToken) {
        value = false
    }

    GolhaAppTheme {
        val currentRoute = navigationStack.last()
        when (currentRoute) {
            AppRoute.Singers -> {
                SingersScreen(
                    singers = singers,
                    bottomNavItems = buildBottomNavItems(AppTab.Home),
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
                    bottomNavItems = buildBottomNavItems(AppTab.Home),
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
                    bottomNavItems = buildBottomNavItems(AppTab.Home),
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
                    bottomNavItems = buildBottomNavItems(AppTab.Home),
                    onBottomNavSelected = { navigationStack.resetToRoot(it) },
                    onBackClick = { navigationStack.pop() },
                    onTrackClick = { trackId -> navigationStack.push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onPlayTrack = { /* Handle play */ },
                    onArtistClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                    onProgramClick = { program -> navigationStack.push(AppRoute.ProgramEpisodeDetail(program.id)) }
                )
            }

            is AppRoute.OrchestraDetail -> {
                val route = currentRoute as AppRoute.OrchestraDetail
                OrchestraDetailScreen(
                    orchestraId = route.id,
                    orchestraName = route.name,
                    bottomNavItems = buildBottomNavItems(AppTab.Library),
                    onBottomNavSelected = { navigationStack.resetToRoot(it) },
                    onBackClick = { navigationStack.pop() },
                    onTrackClick = { trackId -> navigationStack.push(AppRoute.ProgramEpisodeDetail(trackId)) },
                    onArtistClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                )
            }

            is AppRoute.ProgramEpisodeDetail -> {
                val route = currentRoute as AppRoute.ProgramEpisodeDetail
                com.radiogolha.mobile.ui.programs.ProgramEpisodeDetailScreen(
                    programId = route.programId,
                    bottomNavItems = buildBottomNavItems(AppTab.Home),
                    onBottomNavSelected = { navigationStack.resetToRoot(it) },
                    onBackClick = { navigationStack.pop() },
                    onArtistClick = { id -> navigationStack.push(AppRoute.ArtistDetail(id)) },
                )
            }

            is AppRoute.Root -> {
                val rootRoute = currentRoute as AppRoute.Root
                val bottomNavItems = buildBottomNavItems(rootRoute.tab)
                when (rootRoute.tab) {
                    AppTab.Home, AppTab.Search -> {
                        HomeScreen(
                            state = homeState?.copy(bottomNavItems = bottomNavItems),
                            bottomNavItems = bottomNavItems,
                            onOpenAllSingers = { navigationStack.push(AppRoute.Singers) },
                            onOpenAllMusicians = { navigationStack.push(AppRoute.Musicians) },
                            onRefreshTopTracks = { reloadToken += 1 },
                            isRefreshingTopTracks = isTopTracksRefreshing,
                            onTrackClick = { track -> navigationStack.push(AppRoute.ProgramEpisodeDetail(track.id)) },
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
                            singers = singers,
                            musicians = musicians,
                            orchestras = orchestras,
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
                            onOrchestraClick = { id ->
                                val orchestra = orchestras.find { it.id == id }
                                if (orchestra != null) {
                                    navigationStack.push(AppRoute.OrchestraDetail(id, orchestra.name))
                                }
                            },
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
    data class ProgramEpisodeDetail(val programId: Long) : AppRoute
    data class OrchestraDetail(val id: Long, val name: String) : AppRoute
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
