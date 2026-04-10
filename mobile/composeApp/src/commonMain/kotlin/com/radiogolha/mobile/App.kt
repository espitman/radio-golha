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
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.musicians.MusiciansScreen
import com.radiogolha.mobile.ui.musicians.loadMusiciansUiState
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
    val currentRoute = navigationStack.lastOrNull() ?: AppRoute.Root(AppTab.Home)
    val selectedTab = (navigationStack.firstOrNull() as? AppRoute.Root)?.tab ?: AppTab.Home

    PlatformBackHandler(enabled = navigationStack.size > 1) {
        if (navigationStack.size > 1) {
            navigationStack.removeAt(navigationStack.lastIndex)
        }
    }

    val homeState by produceState<HomeUiState?>(initialValue = null, key1 = reloadToken) {
        value = withContext(Dispatchers.Default) {
            runCatching { loadHomeUiState() }.getOrNull()
        }
    }

    val singers by produceState(
        initialValue = emptyList(),
        key1 = reloadToken,
        key2 = currentRoute,
    ) {
        if (currentRoute != AppRoute.Singers) {
            value = emptyList()
            return@produceState
        }

        value = runCatching {
            withContext(Dispatchers.Default) { loadSingersUiState() }
        }.getOrNull() ?: emptyList()
    }
    val musicians by produceState(
        initialValue = emptyList(),
        key1 = reloadToken,
        key2 = currentRoute,
    ) {
        if (currentRoute != AppRoute.Musicians) {
            value = emptyList()
            return@produceState
        }

        value = runCatching {
            withContext(Dispatchers.Default) { loadMusiciansUiState() }
        }.getOrNull() ?: emptyList()
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
                )
            }

            is AppRoute.Root -> {
                when (currentRoute.tab) {
                    AppTab.Home, AppTab.Search, AppTab.Library -> {
                        HomeScreen(
                            state = homeState?.copy(bottomNavItems = bottomNavItems),
                            bottomNavItems = bottomNavItems,
                            onOpenAllSingers = { navigationStack.push(AppRoute.Singers) },
                            onOpenAllMusicians = { navigationStack.push(AppRoute.Musicians) },
                            onRefreshTopTracks = { reloadToken += 1 },
                            onBottomNavSelected = {
                                navigationStack.resetToRoot(it)
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
