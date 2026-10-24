package com.example.myapplication.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Processes : Screen("processes", "Processes", Icons.Default.Memory)
    object Storage : Screen("storage", "Storage", Icons.Default.Storage)
    object Cache : Screen("cache", "Cleaner", Icons.Default.CleaningServices)
    object RamBooster : Screen("ram_booster", "Boost", Icons.Default.RocketLaunch)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Processes,
    Screen.Storage,
    Screen.Cache,
    Screen.RamBooster
)
