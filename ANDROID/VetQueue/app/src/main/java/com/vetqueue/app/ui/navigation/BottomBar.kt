package com.vetqueue.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

private data class BottomItem(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomItems = listOf(
    BottomItem(Screen.Home, "Home", Icons.Filled.Home),
    BottomItem(Screen.ClinicSearch, "Clinics", Icons.Filled.LocalHospital),
    BottomItem(Screen.MyPets, "Pets", Icons.Filled.Pets),
    BottomItem(Screen.Profile, "Profile", Icons.Filled.Person),
)

@Composable
fun VetQueueBottomBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    NavigationBar {
        bottomItems.forEach { item ->
            val selected = currentRoute?.hierarchy?.any { it.route == item.screen.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
