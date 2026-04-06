package com.radiogolha.mobile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.radiogolha.mobile.ui.home.SingerListItemUiModel
import com.radiogolha.mobile.ui.home.loadHomeUiState
import com.radiogolha.mobile.ui.home.sampleHomeUiState
import com.radiogolha.mobile.ui.settings.SettingsScreen
import com.radiogolha.mobile.ui.singers.SingersScreen
import com.radiogolha.mobile.ui.singers.loadSingersUiState
import kotlinx.coroutines.launch

@Composable
fun App() {
    var selectedTab by remember { mutableStateOf(AppTab.Home) }
    var overlayScreen by remember { mutableStateOf<AppOverlayScreen?>(null) }
    var reloadToken by remember { mutableStateOf(0) }
    var isImportingDatabase by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val homeState by produceState(initialValue = sampleHomeUiState(), key1 = reloadToken) {
        value = runCatching { loadHomeUiState() }
            .getOrNull()
            ?: sampleHomeUiState()
    }

    val singers by produceState(initialValue = sampleSingerBrowseItems(), key1 = reloadToken) {
        value = runCatching { loadSingersUiState() }
            .getOrNull()
            ?.ifEmpty { sampleSingerBrowseItems() }
            ?: sampleSingerBrowseItems()
    }

    val bottomNavItems = remember(selectedTab) {
        buildBottomNavItems(selectedTab)
    }

    GolhaAppTheme {
        when (overlayScreen) {
            AppOverlayScreen.Singers -> {
                SingersScreen(
                    singers = singers,
                    bottomNavItems = bottomNavItems,
                    onBottomNavSelected = {
                        selectedTab = it
                        overlayScreen = null
                    },
                    onBackClick = { overlayScreen = null },
                )
            }

            null -> {
                when (selectedTab) {
                    AppTab.Home, AppTab.Search, AppTab.Library -> {
                        HomeScreen(
                            state = homeState.copy(bottomNavItems = bottomNavItems),
                            onOpenAllSingers = { overlayScreen = AppOverlayScreen.Singers },
                            onBottomNavSelected = {
                                selectedTab = it
                                overlayScreen = null
                            },
                        )
                    }

                    AppTab.Account -> {
                        SettingsScreen(
                            bottomNavItems = bottomNavItems,
                            onBottomNavSelected = {
                                selectedTab = it
                                overlayScreen = null
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
                                            selectedTab = AppTab.Home
                                            overlayScreen = null
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

private enum class AppOverlayScreen {
    Singers,
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

private fun sampleSingerBrowseItems(): List<SingerListItemUiModel> = listOf(
    SingerListItemUiModel(name = "محمدرضا شجریان", programCount = 99),
    SingerListItemUiModel(name = "محمدرضا بنان", programCount = 96),
    SingerListItemUiModel(name = "مرضیه", programCount = 173),
    SingerListItemUiModel(name = "دلکش", programCount = 72),
)
