package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AndroidApp() {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    var reloadToken by remember { mutableIntStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val selectedTab = remember(currentDestination) {
        when {
            currentDestination.isOnRoute(AndroidRoute.Account.route) -> AppTab.Account
            currentDestination.isOnRoute(AndroidRoute.Library.route) -> AppTab.Library
            currentDestination.isOnRoute(AndroidRoute.Search.route) -> AppTab.Search
            else -> AppTab.Home
        }
    }
    val bottomNavItems = remember(selectedTab) {
        buildBottomNavItems(selectedTab)
    }

    GolhaAppTheme {
        NavHost(
            navController = navController,
            startDestination = AndroidRoute.Home.route,
        ) {
            composable(AndroidRoute.Home.route) {
                var homeState by remember { mutableStateOf<HomeUiState?>(null) }
                var isInitialHomeLoading by remember { mutableStateOf(true) }
                var isRefreshingTopTracks by remember { mutableStateOf(false) }

                LaunchedEffect(reloadToken) {
                    val loadedState = runCatching {
                        withContext(Dispatchers.Default) { loadHomeUiState() }
                    }.getOrNull()

                    homeState = loadedState
                    isInitialHomeLoading = false
                }

                LaunchedEffect(isRefreshingTopTracks) {
                    if (!isRefreshingTopTracks) return@LaunchedEffect

                    val refreshStartedAt = System.currentTimeMillis()
                    val refreshedTracks = runCatching {
                        withContext(Dispatchers.Default) { com.radiogolha.mobile.ui.home.loadTopTracks() }
                    }.getOrNull()

                    val elapsed = System.currentTimeMillis() - refreshStartedAt
                    val minimumVisibleDuration = 450L
                    if (elapsed < minimumVisibleDuration) {
                        delay(minimumVisibleDuration - elapsed)
                    }

                    if (refreshedTracks != null) {
                        homeState = homeState?.copy(topTracks = refreshedTracks)
                    }
                    isRefreshingTopTracks = false
                }

                HomeScreen(
                    state = if (isInitialHomeLoading && homeState == null) {
                        null
                    } else {
                        homeState?.copy(bottomNavItems = bottomNavItems)
                    },
                    bottomNavItems = bottomNavItems,
                    onOpenAllSingers = { navController.navigate(AndroidRoute.Singers.route) },
                    onOpenAllMusicians = { navController.navigate(AndroidRoute.Musicians.route) },
                    isRefreshingTopTracks = isRefreshingTopTracks,
                    onRefreshTopTracks = {
                        if (!isRefreshingTopTracks) {
                            isRefreshingTopTracks = true
                        }
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

            composable(AndroidRoute.Library.route) {
                TabRootScreen(
                    title = "کتابخانه",
                    subtitle = "لیست‌های ذخیره‌شده و بخش‌های شخصی اینجا می‌آیند.",
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
    data object Library : AndroidRoute("library")
    data object Account : AndroidRoute("account")
    data object Singers : AndroidRoute("singers")
    data object Musicians : AndroidRoute("musicians")
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
