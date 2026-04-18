package com.example.bt_transit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bt_transit.ui.alerts.AlertsScreen
import com.example.bt_transit.ui.home.HomeScreen
import com.example.bt_transit.ui.map.MapScreen
import com.example.bt_transit.ui.schedule.ScheduleScreen
import com.example.bt_transit.ui.search.SearchScreen

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem("home", "Home", Icons.Default.Home),
    NavItem("map", "Map", Icons.Default.Map),
    NavItem("schedule", "Schedule", Icons.Default.Schedule),
    NavItem("alerts", "Alerts", Icons.Default.Notifications)
)

private val bottomBarRoutes = navItems.map { it.route }.toSet()

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarRoutes) {
                NavigationBar {
                    navItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                nav.navigate(item.route) {
                                    launchSingleTop = true
                                    popUpTo("home") { saveState = true }
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    innerPadding = innerPadding,
                    onSearchClick = { nav.navigate("search") }
                )
            }
            composable("map") { MapScreen(innerPadding) }
            composable("schedule") { ScheduleScreen(innerPadding) }
            composable("alerts") { AlertsScreen(innerPadding) }
            composable("search") {
                SearchScreen(onBack = { nav.popBackStack() })
            }
        }
    }
}
