package com.telegramdrive.uploader.core.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.telegramdrive.uploader.core.ui.theme.DesignTokens
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.telegramdrive.uploader.feature.home.HomeScreen
import com.telegramdrive.uploader.feature.splash.SplashScreen
import com.telegramdrive.uploader.feature.queue.QueueScreen
import com.telegramdrive.uploader.feature.history.HistoryScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingViewModel
import com.telegramdrive.uploader.feature.settings.SettingsScreen
import com.telegramdrive.uploader.feature.upload.UploadScreen
import com.telegramdrive.uploader.feature.upload.UploadViewModel
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.feature.telegram.TelegramAuthScreen
import com.telegramdrive.uploader.feature.telegram.TelegramDestinationScreen
import com.telegramdrive.uploader.core.ui.theme.AppContentWidth
import com.telegramdrive.uploader.core.ui.components.MissionControlPage
import com.telegramdrive.uploader.core.ui.components.liquidGlassOverlay
import com.telegramdrive.uploader.R
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    @androidx.annotation.StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(AppRoutes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)
    object Queue : Screen(AppRoutes.QUEUE, R.string.nav_queue, Icons.Filled.Layers, Icons.Outlined.Layers)
    object History : Screen(AppRoutes.HISTORY, R.string.nav_history, Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen(AppRoutes.SETTINGS, R.string.nav_settings, Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Queue,
    Screen.History,
    Screen.Settings
)

@Composable
fun AppNavigation(
    settingsDataStore: SettingsDataStore,
    navController: NavHostController = rememberNavController()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted by onboardingViewModel.completed.collectAsStateWithLifecycle()
    val openingCompleted by settingsDataStore.openingCompleted.collectAsStateWithLifecycle(initialValue = false)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = bottomNavItems.any { it.route == currentRoute }

    // Adaptive Navigation Pattern based on the real available window width.
    // screenWidthDp is rounded and inset-dependent, so prefer the window container size.
    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = Dp(windowInfo.containerSize.width / density.density)
    val isExpanded = windowWidthDp >= 600.dp

    val uploadViewModel: UploadViewModel = hiltViewModel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(DesignTokens.spacingM)
            .background(DesignTokens.AppColors.background)
    ) {
        if (isExpanded && showBottomBar) {
            NavigationRail(
                containerColor = DesignTokens.AppColors.surfaceCard,
                contentColor = DesignTokens.AppColors.contentPrimary,
                header = {
                    // App logo/brand in the rail header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DesignTokens.spacingM),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = DesignTokens.AppColors.lime,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            ) {
                bottomNavItems.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(screen.titleRes)) },
                        alwaysShowLabel = true,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = DesignTokens.AppColors.lime,
                            selectedTextColor = DesignTokens.AppColors.lime,
                            indicatorColor = DesignTokens.AppColors.lime.copy(alpha = 0.15f),
                            unselectedIconColor = DesignTokens.AppColors.contentMuted,
                            unselectedTextColor = DesignTokens.AppColors.contentMuted
                        ),
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }

        Scaffold(
            containerColor = DesignTokens.AppColors.background,
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!isExpanded && showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DesignTokens.AppSpacing.phoneEdge, vertical = DesignTokens.AppSpacing.phoneNavInset)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassOverlay(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    accent = DesignTokens.AppColors.onPrimary
                                ),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = DesignTokens.AppColors.surfaceCard,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp
                        ) {
                            NavigationBar(
                                modifier = Modifier.height(72.dp),
                                containerColor = Color.Transparent,
                                tonalElevation = 0.dp,
                                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                            ) {
                                bottomNavItems.forEach { screen ->
                                    val isSelected = currentRoute == screen.route
                                    NavigationBarItem(
                                        selected = isSelected,
                                        onClick = {
                                            if (currentRoute != screen.route) {
                                                navController.navigate(screen.route) {
                                                    popUpTo(navController.graph.findStartDestination().id) {
                                                        saveState = true
                                                    }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                                contentDescription = null
                                            )
                                        },
                                        label = { Text(stringResource(screen.titleRes)) },
                                        colors = NavigationBarItemDefaults.colors(
                                            // The indicator is a light pill, so the selected
                                            // glyph and label have to be dark ink. Both used to
                                            // be `onPrimary` (white), which painted a white
                                            // icon on a white pill and made the active tab
                                            // look empty.
                                            selectedIconColor = DesignTokens.AppColors.contentPrimaryInverse,
                                            selectedTextColor = DesignTokens.AppColors.contentPrimaryInverse,
                                            indicatorColor = DesignTokens.AppColors.onPrimary,
                                            unselectedIconColor = DesignTokens.AppColors.contentMuted,
                                            unselectedTextColor = DesignTokens.AppColors.contentMuted
                                        ),
                                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            MissionControlPage(pageKey = currentRoute, modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = if (openingCompleted) Screen.Home.route else AppRoutes.SPLASH,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = AppContentWidth.max)
                        .padding(innerPadding)
                        .align(Alignment.TopCenter)
                ) {
                composable(AppRoutes.SPLASH) {
                    SplashScreen(
                        onFinished = {
                            scope.launch {
                                settingsDataStore.setOpeningCompleted()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(AppRoutes.SPLASH) { inclusive = true }
                                }
                            }
                        }
                    )
                }
                composable(Screen.Home.route) {
                    if (!onboardingCompleted) {
                        OnboardingScreen(
                            onFinished = { },
                            viewModel = onboardingViewModel
                        )
                    } else {
                        HomeScreen(
                            onSettingsClick = { navController.navigate(Screen.Settings.route) },
                            onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) },
                            onVideosSelected = { uris ->
                                uploadViewModel.setPrepareUris(uris)
                                navController.navigate(AppRoutes.UPLOAD_PREPARATION)
                            },

                        )
                    }
                }
                composable(AppRoutes.UPLOAD_PREPARATION) {
                    UploadScreen(
                        onBackClick = { navController.popBackStack() },
                        onSelectDestination = { navController.navigate(AppRoutes.TELEGRAM_DESTINATION) },
                        onQueueAdded = {
                            navController.navigate(Screen.Queue.route) {
                                popUpTo(Screen.Home.route)
                            }
                        },
                        viewModel = uploadViewModel
                    )
                }
                composable(AppRoutes.TELEGRAM_AUTH) {
                    TelegramAuthScreen(
                        onBackClick = { navController.popBackStack() },
                        onAuthSuccess = { navController.popBackStack() }
                    )
                }
                composable(AppRoutes.TELEGRAM_DESTINATION) {
                    TelegramDestinationScreen(
                        onBackClick = { navController.popBackStack() },
                        onConnectClick = {
                            navController.navigate(AppRoutes.TELEGRAM_AUTH)
                        },
                        onDestinationSelected = { dest ->
                            uploadViewModel.onDestinationSelected(dest)
                            navController.popBackStack()
                        }
                    )
                }
                composable(Screen.Queue.route) { QueueScreen() }
                composable(Screen.History.route) { HistoryScreen() }
                    composable(Screen.Settings.route) {
                        SettingsScreen(
                            onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) }
                        )
                    }
                }
            }
        }
    }
}
