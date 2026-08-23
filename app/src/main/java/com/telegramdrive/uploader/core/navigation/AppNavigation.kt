package com.telegramdrive.uploader.core.navigation

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
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
import com.telegramdrive.uploader.feature.queue.QueueScreen
import com.telegramdrive.uploader.feature.history.HistoryScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingViewModel
import com.telegramdrive.uploader.feature.settings.SettingsScreen
import com.telegramdrive.uploader.feature.upload.UploadScreen
import com.telegramdrive.uploader.feature.upload.UploadViewModel
import com.telegramdrive.uploader.feature.telegram.TelegramAuthScreen
import com.telegramdrive.uploader.feature.telegram.TelegramDestinationScreen

sealed class Screen(val route: String, val title: String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector) {
    object Home : Screen(AppRoutes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home)
    object Queue : Screen(AppRoutes.QUEUE, "Queue", Icons.Filled.Layers, Icons.Outlined.Layers)
    object History : Screen(AppRoutes.HISTORY, "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen(AppRoutes.SETTINGS, "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

val bottomNavItems = listOf(
    Screen.Home,
    Screen.Queue,
    Screen.History,
    Screen.Settings
)

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val onboardingCompleted by onboardingViewModel.completed.collectAsStateWithLifecycle()

    if (!onboardingCompleted) {
        OnboardingScreen(
            onFinished = { },
            viewModel = onboardingViewModel
        )
        return
    }

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
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        modifier = Modifier.testTag("nav_tab_${screen.route}")
                    )
                }
            }
        }

        Scaffold(
            contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (!isExpanded && showBottomBar) {
                    NavigationBar {
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
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                modifier = Modifier.testTag("nav_tab_${screen.route}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                        onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) },
                        onVideosSelected = { uris ->
                            uploadViewModel.setPrepareUris(uris)
                            navController.navigate(AppRoutes.UPLOAD_PREPARATION)
                        }
                    )
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
