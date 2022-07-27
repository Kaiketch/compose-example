package com.redpond.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.redpond.base.*
import com.redpond.base.Args.Companion.CODE
import com.redpond.country.CountryScreen
import com.redpond.profile.ProfileScreen
import com.redpond.search.SearchScreen

@ExperimentalComposeUiApi
@Composable
fun MainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isNavigationScreen = bottomNavItems.map { it.route }.contains(currentDestination?.route)

    CompositionLocalProvider(
        LocalNavController provides navController,
    ) {
        Scaffold(
            bottomBar = {
                if (isNavigationScreen) {
                    AppBottomNavigation(
                        navController = navController,
                        currentDestination = currentDestination
                    )
                }
            }
        ) { paddingValues ->
            AppNavHost(navController = navController, paddingValues = paddingValues)
        }
    }
}

@Composable
fun AppBottomNavigation(
    navController: NavHostController,
    currentDestination: NavDestination?
) {
    BottomNavigation {
        bottomNavItems.forEach { screen ->
            val icon = screen.icon
            BottomNavigationItem(
                icon = { icon?.let { Icon(icon, null) } },
                label = { screen.resourceId?.let { Text(stringResource(it)) } },
                selected = currentDestination?.route == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
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

@Composable
fun AppNavHost(
    navController: NavHostController,
    paddingValues: PaddingValues
) {
    NavHost(
        navController = navController,
        startDestination = Graph.Search.route,
        modifier = Modifier.padding(paddingValues),
    ) {
        searchGraph(navController = navController)
        profileGraph(navController = navController)
    }
}

fun NavGraphBuilder.searchGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Search.route, route = Graph.Search.route) {
        composable(Screen.Search.route) {
            SearchScreen(
                userViewModel = hiltViewModel(LocalActivity.current),
                searchViewModel = hiltViewModel(),
                navigateToCountry = { countryCode ->
                    navController.navigate("${Screen.Detail.route}/$countryCode")
                }
            )
        }
        composable(
            "${Screen.Detail.route}/{$CODE}",
            arguments = listOf(navArgument(CODE) { type = NavType.StringType }),
        ) {
            CountryScreen(
                userViewModel = hiltViewModel(LocalActivity.current),
                countryViewModel = hiltViewModel(),
                popBackStack = { navController.popBackStack() }
            )
        }
    }
}

fun NavGraphBuilder.profileGraph(navController: NavHostController) {
    navigation(startDestination = Screen.Profile.route, route = Graph.Profile.route) {
        composable(Screen.Profile.route) {
            ProfileScreen(
                userViewModel = hiltViewModel(LocalActivity.current),
                navigateToCountry = { countryCode ->
                    navController.navigate("${Screen.Detail.route}/$countryCode")
                }
            )
        }
    }
}
