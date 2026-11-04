package com.oceanwalls.app.ui.nav

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.oceanwalls.app.core.AppContainer
import com.oceanwalls.app.ui.screens.category.CategoryScreen
import com.oceanwalls.app.ui.screens.credits.CreditsScreen
import com.oceanwalls.app.ui.screens.detail.DetailScreen
import com.oceanwalls.app.ui.screens.downloads.DownloadsScreen
import com.oceanwalls.app.ui.screens.favorites.FavoritesScreen
import com.oceanwalls.app.ui.screens.home.HomeScreen
import com.oceanwalls.app.ui.screens.search.SearchScreen
import com.oceanwalls.app.ui.theme.Aqua
import com.oceanwalls.app.ui.theme.MistDim

object Routes {
    const val HOME = "home"
    const val SEARCH = "search"
    const val FAVORITES = "favorites"
    const val LIBRARY = "library"
    const val CREDITS = "credits"
    const val CATEGORY = "category/{categoryId}"
    const val DETAIL = "detail/{pageId}"

    fun category(id: String) = "category/$id"
    fun detail(pageId: Long) = "detail/$pageId"
}

private data class TabItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val icon: ImageVector
)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TabItem(Routes.SEARCH, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    TabItem(Routes.FAVORITES, "Favourites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TabItem(Routes.LIBRARY, "Library", Icons.Outlined.Download, Icons.Outlined.Download)
)

@Composable
fun OceanWallsApp(container: AppContainer) {
    val navController: NavHostController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBar = tabs.any { it.route == currentRoute }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = Color.Black, tonalElevation = 0.dp) {
                    tabs.forEach { tab ->
                        val selected = currentRoute == tab.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { navController.navigateToTab(tab.route) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.icon,
                                    contentDescription = tab.label
                                )
                            },
                            label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Aqua,
                                selectedTextColor = Aqua,
                                unselectedIconColor = MistDim,
                                unselectedTextColor = MistDim,
                                indicatorColor = Aqua.copy(alpha = 0.12f)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            composable(Routes.HOME) {
                HomeScreen(
                    container = container,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) },
                    onOpenCategory = { navController.navigate(Routes.category(it)) },
                    onOpenSearch = { navController.navigateToTab(Routes.SEARCH) },
                    onOpenCredits = { navController.navigate(Routes.CREDITS) },
                    onOpenFavorites = { navController.navigateToTab(Routes.FAVORITES) }
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    container = container,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }
            composable(Routes.FAVORITES) {
                FavoritesScreen(
                    container = container,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) },
                    onBrowse = { navController.navigateToTab(Routes.HOME) }
                )
            }
            composable(Routes.LIBRARY) {
                DownloadsScreen(
                    container = container,
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) },
                    onBrowse = { navController.navigateToTab(Routes.HOME) }
                )
            }
            composable(Routes.CREDITS) {
                CreditsScreen(onBack = { navController.popBackStack() })
            }
            composable(Routes.CATEGORY) { entry ->
                val categoryId = entry.arguments?.getString("categoryId").orEmpty()
                CategoryScreen(
                    container = container,
                    categoryId = categoryId,
                    onBack = { navController.popBackStack() },
                    onOpenWallpaper = { navController.navigate(Routes.detail(it)) }
                )
            }
            composable(Routes.DETAIL) { entry ->
                val pageId = entry.arguments?.getString("pageId")?.toLongOrNull() ?: 0L
                DetailScreen(
                    container = container,
                    pageId = pageId,
                    onBack = { navController.popBackStack() },
                    onOpenCredits = { navController.navigate(Routes.CREDITS) }
                )
            }
        }
    }
}

private fun NavHostController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
