package com.example.myapplication.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.CacheCleanerScreen
import com.example.myapplication.ui.screens.DashboardScreen
import com.example.myapplication.ui.screens.ProcessManagerScreen
import com.example.myapplication.ui.screens.RamBoosterScreen
import com.example.myapplication.ui.screens.StorageAnalyzerScreen
import com.example.myapplication.ui.theme.CyanAccent
import com.example.myapplication.ui.theme.DarkBackground
import com.example.myapplication.ui.theme.DarkSurface
import com.example.myapplication.ui.theme.DarkSurfaceBorder
import com.example.myapplication.ui.theme.TextMuted
import com.example.myapplication.ui.theme.TextPrimary
import com.example.myapplication.viewmodel.CacheViewModel
import com.example.myapplication.viewmodel.DashboardViewModel
import com.example.myapplication.viewmodel.ProcessViewModel
import com.example.myapplication.viewmodel.RamBoosterViewModel
import com.example.myapplication.viewmodel.StorageViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val dashboardViewModel: DashboardViewModel = viewModel()
    val processViewModel: ProcessViewModel = viewModel()
    val storageViewModel: StorageViewModel = viewModel()
    val cacheViewModel: CacheViewModel = viewModel()
    val ramViewModel: RamBoosterViewModel = viewModel()

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentRoute == screen.route
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(Screen.Dashboard.route) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CyanAccent,
                            selectedTextColor = CyanAccent,
                            unselectedIconColor = TextMuted,
                            unselectedTextColor = TextMuted,
                            indicatorColor = DarkSurfaceBorder
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(280)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(280)
                )
            }
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToProcesses = { navController.navigate(Screen.Processes.route) },
                    onNavigateToStorage = { navController.navigate(Screen.Storage.route) },
                    onNavigateToCache = { navController.navigate(Screen.Cache.route) },
                    onNavigateToRam = { navController.navigate(Screen.RamBooster.route) }
                )
            }

            composable(Screen.Processes.route) {
                ProcessManagerScreen(
                    viewModel = processViewModel,
                    onBack = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            composable(Screen.Storage.route) {
                StorageAnalyzerScreen(
                    viewModel = storageViewModel,
                    onBack = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            composable(Screen.Cache.route) {
                CacheCleanerScreen(
                    viewModel = cacheViewModel,
                    onBack = { navController.navigate(Screen.Dashboard.route) }
                )
            }

            composable(Screen.RamBooster.route) {
                RamBoosterScreen(
                    viewModel = ramViewModel,
                    onBack = { navController.navigate(Screen.Dashboard.route) }
                )
            }
        }
    }
}
