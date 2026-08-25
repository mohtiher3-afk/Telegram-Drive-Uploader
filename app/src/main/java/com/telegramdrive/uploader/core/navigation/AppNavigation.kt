package com.telegramdrive.uploader.core.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.components.MissionControlPage
import com.telegramdrive.uploader.core.ui.components.glowSignalRim
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

    // Adaptive Navigation Pattern based on screen width
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 600

    val uploadViewModel: UploadViewModel = hiltViewModel()

    Row(modifier = Modifier.fillMaxSize()) {
        if (isExpanded && showBottomBar) {
            NavigationRail {
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
                        modifier = Modifier
                            .glowSignalRim(
                                shape = MaterialTheme.shapes.small,
                                enabled = isSelected
                            )
                            .testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }

        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!isExpanded && showBottomBar) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = AppSpacing.sm)
                    ) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .liquidGlassOverlay(
                                    shape = MaterialTheme.shapes.extraLarge,
                                    accent = MaterialTheme.colorScheme.primary
                                ),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            tonalElevation = 6.dp,
                            shadowElevation = 8.dp
                        ) {
                            NavigationBar(
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
                                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                                            indicatorColor = MaterialTheme.colorScheme.primary,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .glowSignalRim(
                                                shape = MaterialTheme.shapes.small,
                                                enabled = isSelected
                                            )
                                            .testTag("nav_tab_${screen.route}")
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
                            }
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
