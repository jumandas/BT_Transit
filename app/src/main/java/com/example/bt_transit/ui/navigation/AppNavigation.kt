package com.example.bt_transit.ui.navigation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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

private val bottomBarRoutes = setOf("home", "map", "schedule", "alerts")

@Composable
fun AppNavigation() {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentDest = backStack?.destination?.route
    val baseRoute = currentDest?.substringBefore('?')

    Scaffold(
        bottomBar = {
            if (baseRoute in bottomBarRoutes) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    navItems.forEach { item ->
                        val selected = baseRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (selected) return@NavigationBarItem

                                when (item.route) {
                                    "home" -> {
                                        // Reliable pop: clear stack back to home
                                        val popped = nav.popBackStack("home", inclusive = false)
                                        if (!popped) {
                                            nav.navigate("home") {
                                                launchSingleTop = true
                                                popUpTo(nav.graph.startDestinationId) {
                                                    inclusive = false
                                                    saveState = true
                                                }
                                                restoreState = true
                                            }
                                        }
                                    }
                                    else -> {
                                        nav.navigate(item.route) {
                                            launchSingleTop = true
                                            popUpTo("home") { saveState = true }
                                            restoreState = true
                                        }
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            )
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
                    onSearchClick = { nav.navigate("search") },
                    onNearbyRouteClick = { routeId ->
                        nav.navigate("map?routeId=$routeId") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(
                route = "map?routeId={routeId}",
                arguments = listOf(navArgument("routeId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { entry ->
                val routeId = entry.arguments?.getString("routeId")
                MapScreen(innerPadding = innerPadding, focusedRouteId = routeId)
            }
            composable("schedule") { ScheduleScreen(innerPadding) }
            composable("alerts") { AlertsScreen(innerPadding) }
            composable("search") {
                SearchScreen(
                    onBack = { nav.popBackStack() },
                    onRoutePick = { routeId ->
                        nav.navigate("map?routeId=$routeId") {
                            popUpTo("home")
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
