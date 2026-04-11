package com.example.fundtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.example.fundtracker.ui.features.fund_list.FundListScreen
import com.example.fundtracker.ui.features.home.ExploreScreen
import com.example.fundtracker.ui.features.navigation.NavRoutes
import com.example.fundtracker.ui.features.search.SearchScreen
import com.example.fundtracker.ui.theme.FundTrackerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable



@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FundTrackerTheme {
                val navController = rememberNavController()
                val showBottomBar = remember { mutableStateOf(true) }

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(visible = showBottomBar.value) {
                            NavigationBar(containerColor = Color.White) {
                                val navBackStackEntry by navController.currentBackStackEntryAsState()
                                val currentDest = navBackStackEntry?.destination

                                val items = listOf(
                                    Triple(NavRoutes.Explore, Icons.Default.List, "Explore"),
                                )

                                items.forEach { (route, icon, label) ->
                                    val selected = currentDest?.hierarchy?.any {
                                        it.hasRoute(route::class)
                                    } == true

                                    NavigationBarItem(
                                        selected = selected,
                                        label = { Text(label) },
                                        onClick = {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (selected) Color(0xFF6200EE) else Color.Gray
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavRoutes.Explore,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable<NavRoutes.Explore> {
                            showBottomBar.value = true
                            ExploreScreen(
                                onViewAllClick = { category ->
                                    navController.navigate(NavRoutes.FundList(title = "$category Funds", category = category))
                                },
                                onSearchClick = {
                                    navController.navigate(NavRoutes.Search)
                                }
                            )
                        }
                        composable<NavRoutes.Search> {
                            showBottomBar.value = false
                            SearchScreen(
                                onFundClick = {},
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable<NavRoutes.FundList>{
                            val args = it.toRoute<NavRoutes.FundList>()
                            FundListScreen(
                                title = args.title,
                                onFundClick = { code ->

                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}