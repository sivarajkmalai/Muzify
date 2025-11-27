package com.muzify.app.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.muzify.app.ui.screens.*
import com.muzify.app.ui.viewmodel.PlayerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MuzifyNavigation(
    playerViewModel: PlayerViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var showFullPlayer by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in listOf("home", "library", "profile")) {
                NavigationBar {
                    val navItems = listOf(
                        NavItem("home", "Home", Icons.Default.Home),
                        NavItem("library", "Library", Icons.Default.LibraryMusic),
                        NavItem("profile", "Profile", Icons.Default.Person)
                    )

                    navItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("home") {
                    HomeScreen(
                        onNavigateToPlayer = { showFullPlayer = true },
                        playerViewModel = playerViewModel
                    )
                }
                composable("library") {
                    LibraryScreen(
                        onNavigateToPlaylist = { playlistId ->
                            navController.navigate("playlist/$playlistId")
                        },
                        playerViewModel = playerViewModel
                    )
                }
                composable("playlist/{playlistId}") { backStackEntry ->
                    val playlistId = backStackEntry.arguments?.getString("playlistId")?.toLongOrNull()
                    if (playlistId != null) {
                        PlaylistScreen(
                            playlistId = playlistId,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToPlayer = { showFullPlayer = true },
                            playerViewModel = playerViewModel
                        )
                    }
                }
                composable("profile") {
                    ProfileScreen()
                }
            }

            // Mini Player
            val currentTrack by playerViewModel.currentTrack.collectAsState()
            if (currentTrack != null) {
                MiniPlayer(
                    viewModel = playerViewModel,
                    onTap = { showFullPlayer = true },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
                )
            }

            // Full Player
            if (showFullPlayer) {
                FullPlayerScreen(
                    viewModel = playerViewModel,
                    onDismiss = { showFullPlayer = false }
                )
            }
        }
    }
}

data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

