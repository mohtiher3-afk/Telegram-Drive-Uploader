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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.automirrored.outlined.PlaylistAdd
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
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
import com.telegramdrive.uploader.core.ui.theme.AppContentWidth
import com.telegramdrive.uploader.core.ui.theme.AppSpacing
import com.telegramdrive.uploader.core.ui.components.AppNavPill
import com.telegramdrive.uploader.core.ui.components.NavPillItem
import com.telegramdrive.uploader.core.ui.components.UploadIsland
import com.telegramdrive.uploader.feature.home.HomeScreen
import com.telegramdrive.uploader.feature.home.HomeViewModel
import com.telegramdrive.uploader.feature.home.HomeUiState
import com.telegramdrive.uploader.feature.splash.SplashScreen
import com.telegramdrive.uploader.feature.queue.QueueScreen
import com.telegramdrive.uploader.feature.queue.QueueViewModel
import com.telegramdrive.uploader.feature.history.HistoryScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingScreen
import com.telegramdrive.uploader.feature.onboarding.OnboardingViewModel
import com.telegramdrive.uploader.feature.settings.SettingsScreen
import com.telegramdrive.uploader.feature.upload.UploadScreen
import com.telegramdrive.uploader.feature.upload.UploadViewModel
import com.telegramdrive.uploader.data.local.datastore.SettingsDataStore
import com.telegramdrive.uploader.feature.telegram.TelegramAuthScreen
import com.telegramdrive.uploader.feature.telegram.TelegramDestinationScreen
import com.telegramdrive.uploader.domain.model.UploadStatus
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.testTag
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.telegramdrive.uploader.R
import kotlinx.coroutines.launch

sealed class Screen(
    val route: String,
    @androidx.annotation.StringRes val titleRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen(AppRoutes.HOME, R.string.nav_home, Icons.Filled.Home, Icons.Outlined.Home)
    object Send : Screen(AppRoutes.TELEGRAM_DESTINATION, R.string.nav_send, Icons.AutoMirrored.Filled.Chat, Icons.AutoMirrored.Outlined.Chat)
    object Add : Screen(AppRoutes.UPLOAD_PREPARATION, R.string.nav_add, Icons.AutoMirrored.Filled.PlaylistAdd, Icons.AutoMirrored.Outlined.PlaylistAdd)
    object Queue : Screen(AppRoutes.QUEUE, R.string.nav_queue, Icons.Filled.Layers, Icons.Outlined.Layers)
    object Setup : Screen(AppRoutes.SETTINGS, R.string.nav_setup, Icons.Filled.Settings, Icons.Outlined.Settings)
}

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

    // Unified navigation items with routes for both tablet rail and phone pill
    val navItems = listOf(
        NavPillItem(Icons.Filled.Home, stringResource(R.string.nav_home), "nav_tab_${Screen.Home.route}", Screen.Home.route),
        NavPillItem(Icons.AutoMirrored.Filled.Chat, stringResource(R.string.nav_send), "nav_tab_${Screen.Send.route}", Screen.Send.route),
        NavPillItem(Icons.AutoMirrored.Filled.PlaylistAdd, stringResource(R.string.nav_add), "nav_tab_${Screen.Add.route}", Screen.Add.route),
        NavPillItem(Icons.Filled.Layers, stringResource(R.string.nav_queue), "nav_tab_${Screen.Queue.route}", Screen.Queue.route),
        NavPillItem(Icons.Filled.Settings, stringResource(R.string.nav_setup), "nav_tab_${Screen.Setup.route}", Screen.Setup.route),
    )
    val showBottomBar = navItems.any { it.route == currentRoute }

    val windowInfo = LocalWindowInfo.current
    val density = LocalDensity.current
    val windowWidthDp = Dp(windowInfo.containerSize.width / density.density)
    val isExpanded = windowWidthDp >= 600.dp

    val homeViewModel: HomeViewModel = hiltViewModel()
    val queueViewModel: QueueViewModel = hiltViewModel()
    val uploadViewModel: UploadViewModel = hiltViewModel()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.medium)
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isExpanded && showBottomBar) {
            NavigationRail(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                header = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppSpacing.medium),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudUpload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            ) {
                navItems.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationRailItem(
                        selected = isSelected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
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
                                imageVector = item.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(item.label) },
                        alwaysShowLabel = true,
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!isExpanded && showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.phoneEdge, vertical = AppSpacing.phoneNavInset)
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp,
                        shadowElevation = 8.dp
                    ) {
                    // Floating nav: circular indicators, active tab expands into a pill with label
                        AppNavPill(
                            items = navItems,
                            selectedIndex = navItems.indexOfFirst { it.route == currentRoute },
                            onSelect = { index ->
                                val selectedItem = navItems[index]
                                if (currentRoute != selectedItem.route) {
                                    navController.navigate(selectedItem.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = AppSpacing.phoneEdge, vertical = AppSpacing.phoneNavInset)
                        )
                    }
                    }
                }
            }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surface
            ) {
                // Island notification overlay — shows active upload progress at top of screen
                val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle(HomeUiState())
                val activeUpload = homeUiState.activeUploads.firstOrNull()
                val showIsland = activeUpload != null &&
                    activeUpload.status != UploadStatus.COMPLETED &&
                    activeUpload.status != UploadStatus.CANCELLED &&
                    currentRoute != AppRoutes.SPLASH &&
                    currentRoute != AppRoutes.TELEGRAM_AUTH
                activeUpload?.let { upload ->
                    UploadIsland(
                        activeUpload = upload,
                        isVisible = showIsland,
                        onExpandClick = { /* Island handles its own expansion */ },
                        onPauseClick = { queueViewModel.pauseUpload(upload.id) },
                        onCancelClick = { queueViewModel.cancelUpload(upload.id) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.medium, vertical = 16.dp)
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = if (openingCompleted) Screen.Home.route else AppRoutes.SPLASH,
                    modifier = Modifier
                        .padding(innerPadding)
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
                                onSettingsClick = { navController.navigate(Screen.Setup.route) },
                                onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) },
                                onVideosSelected = { uris ->
                                    uploadViewModel.setPrepareUris(uris)
                                    navController.navigate(Screen.Add.route) {
                                        popUpTo(Screen.Home.route)
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                    composable(Screen.Send.route) {
                        TelegramDestinationScreen(
                            onBackClick = { navController.navigate(Screen.Home.route) },
                            onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) },
                            onDestinationSelected = { dest ->
                                uploadViewModel.onDestinationSelected(dest)
                                navController.navigate(Screen.Add.route) {
                                    popUpTo(Screen.Home.route)
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                    composable(Screen.Add.route) {
                        UploadScreen(
                            onBackClick = { navController.navigate(Screen.Home.route) },
                            onSelectDestination = { navController.navigate(Screen.Send.route) },
                            onQueueAdded = {
                                navController.navigate(Screen.Queue.route) {
                                    popUpTo(Screen.Home.route)
                                    launchSingleTop = true
                                    restoreState = true
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
                    composable(Screen.Queue.route) { QueueScreen() }
                    composable(AppRoutes.HISTORY) { HistoryScreen() }
                    composable(Screen.Setup.route) {
                        SettingsScreen(
                            onConnectClick = { navController.navigate(AppRoutes.TELEGRAM_AUTH) }
                        )
                    }
                }
            }
        }
    }
}
