package com.example.fundtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.navigation.toRoute
import com.example.fundtracker.ui.features.fund_list.FundListScreen
import com.example.fundtracker.ui.features.global_list.GlobalFundListScreen
import com.example.fundtracker.ui.features.home.ExploreScreen
import com.example.fundtracker.ui.features.navigation.NavRoutes
import com.example.fundtracker.ui.features.portfolio_details.PortfolioDetailsScreen
import com.example.fundtracker.ui.features.portfolio_list.PortfolioListScreen
import com.example.fundtracker.ui.features.product_details.ProductDetailsScreen
import com.example.fundtracker.ui.features.search.SearchScreen
import com.example.fundtracker.ui.theme.FundTrackerTheme
import dagger.hilt.android.AndroidEntryPoint

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

                                // Added Portfolios to the Bottom Navigation Items
                                val items = listOf(
                                    Triple(NavRoutes.Explore, Icons.Default.Home, "Explore"),
                                    Triple(NavRoutes.Portfolio, Icons.Default.Favorite, "Portfolios")
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
                                onSearchClick = { navController.navigate(NavRoutes.Search) },
                                onGlobalViewAllClick = { navController.navigate(NavRoutes.GlobalList) }
                            )
                        }

                        composable<NavRoutes.Search> {
                            showBottomBar.value = false
                            SearchScreen(
                                onFundClick = { navController.navigate(NavRoutes.ProductDetails(schemeCode = it)) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoutes.FundList> {
                            showBottomBar.value = true
                            val args = it.toRoute<NavRoutes.FundList>()
                            FundListScreen(
                                title = args.title,
                                onFundClick = { code -> navController.navigate(NavRoutes.ProductDetails(schemeCode = code)) },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoutes.ProductDetails> {
                            showBottomBar.value = true
                            ProductDetailsScreen(onBack = { navController.popBackStack() })
                        }

                        // --- NEW PORTFOLIO ROUTES ---

                        composable<NavRoutes.Portfolio> {
                            showBottomBar.value = true
                            PortfolioListScreen(
                                onExploreFundsClick = {
                                    // Redirects to the Explore Tab, which will automatically save state
                                    navController.navigate(NavRoutes.Explore)
                                },
                                onPortfolioClick = { id, name ->
                                    navController.navigate(NavRoutes.PortfolioDetails(id, name))
                                }
                            )
                        }

                        composable<NavRoutes.PortfolioDetails> { backStackEntry ->
                            showBottomBar.value = false
                            val args = backStackEntry.toRoute<NavRoutes.PortfolioDetails>()
                            PortfolioDetailsScreen(
                                portfolioId = args.id,
                                portfolioName = args.name,
                                onFundClick = { code ->
                                    navController.navigate(NavRoutes.ProductDetails(schemeCode = code))
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable<NavRoutes.GlobalList> {
                            showBottomBar.value = true
                            GlobalFundListScreen(
                                onFundClick = { code -> navController.navigate(NavRoutes.ProductDetails(code)) },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}