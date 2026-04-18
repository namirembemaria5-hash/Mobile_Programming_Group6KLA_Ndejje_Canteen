package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ndejje.ndejjecanteen.ui.navigation.CanteenNavGraph
import com.ndejje.ndejjecanteen.ui.navigation.bottomNavItems
import com.ndejje.ndejjecanteen.ui.navigation.adminNavItems
import com.ndejje.ndejjecanteen.ui.navigation.Screen
import com.ndejje.ndejjecanteen.ui.viewmodel.*

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val menuViewModel: MenuViewModel = viewModel()
    val cartViewModel: CartViewModel = viewModel()
    val orderViewModel: OrderViewModel = viewModel()
    val managementViewModel: ManagementViewModel = viewModel()

    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val cartItemCount by cartViewModel.cartItemCount.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val role = userProfile?.role ?: "USER"
    val isStaff = role == "ADMIN" || role == "KITCHEN" || role == "DELIVERY"
    
    // Filter navigation items strictly by role to ensure separation of concerns
    val items = remember(role) {
        when (role) {
            "ADMIN" -> adminNavItems
            "KITCHEN" -> adminNavItems.filter { it.route == Screen.KitchenOrders.route || it.route == Screen.Profile.route }
            "DELIVERY" -> adminNavItems.filter { it.route == Screen.DeliveryOrders.route || it.route == Screen.Profile.route }
            else -> bottomNavItems
        }
    }
    
    // Hide bottom bar on login/register screens
    val showBottomBar = currentDestination?.route != Screen.Login.route && 
                       currentDestination?.route != Screen.Register.route

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar {
                        items.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    if (item.route == Screen.Cart.route && cartItemCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Badge {
                                                    Text(text = cartItemCount.toString())
                                                }
                                            }
                                        ) {
                                            Icon(item.icon, contentDescription = item.label)
                                        }
                                    } else {
                                        Icon(item.icon, contentDescription = item.label)
                                    }
                                },
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
        ) { innerPadding ->
            CanteenNavGraph(
                navController = navController,
                authViewModel = authViewModel,
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                orderViewModel = orderViewModel,
                managementViewModel = managementViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
    
    // Auto-redirect staff to their respective starting points if they land on Home
    LaunchedEffect(isLoggedIn, userProfile) {
        if (isLoggedIn && isStaff && currentDestination?.route == Screen.Home.route) {
            val destination = when (role) {
                "ADMIN" -> Screen.AdminDashboard.route
                "KITCHEN" -> Screen.KitchenOrders.route
                "DELIVERY" -> Screen.DeliveryOrders.route
                else -> Screen.AdminDashboard.route
            }
            navController.navigate(destination) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
        }
    }
}
