package com.ndejje.ndejjecanteen.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.ndejje.ndejjecanteen.ui.screens.*
import com.ndejje.ndejjecanteen.ui.viewmodel.*

@Composable
fun CanteenNavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    menuViewModel: MenuViewModel,
    cartViewModel: CartViewModel,
    orderViewModel: OrderViewModel,
    managementViewModel: ManagementViewModel,
    modifier: Modifier = Modifier
) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                authViewModel = authViewModel,
                menuViewModel = menuViewModel,
                onCategoryClick = { category ->
                    navController.navigate(Screen.Menu.createRoute(category))
                }
            )
        }

        composable(Screen.Menu.route) { backStackEntry ->
            val category = backStackEntry.arguments?.getString("category") ?: ""
            MenuScreen(
                category = category,
                menuViewModel = menuViewModel,
                cartViewModel = cartViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Cart.route) {
            CartScreen(
                cartViewModel = cartViewModel,
                authViewModel = authViewModel,
                orderViewModel = orderViewModel,
                onOrderPlaced = { orderId ->
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onRequireLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { role ->
                    if (role == "ADMIN" || role == "KITCHEN" || role == "DELIVERY") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                authViewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        composable(Screen.Orders.route) {
            if (!isLoggedIn) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        // Pop up to the home destination to avoid the login loop
                        popUpTo(Screen.Home.route)
                    }
                }
            } else {
                OrderHistoryScreen(
                    authViewModel = authViewModel,
                    orderViewModel = orderViewModel,
                    onOrderClick = { orderId ->
                        navController.navigate(Screen.OrderStatus.createRoute(orderId))
                    }
                )
            }
        }

        composable(Screen.OrderStatus.route) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            OrderStatusScreen(
                orderId = orderId,
                orderViewModel = orderViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Profile.route) {
            if (!isLoggedIn) {
                LaunchedEffect(Unit) {
                    navController.navigate(Screen.Login.route) {
                        // Pop up to the home destination to avoid the login loop
                        popUpTo(Screen.Home.route)
                    }
                }
            } else {
                ProfileScreen(
                    authViewModel = authViewModel,
                    onLogout = {
                        // Navigate to Home FIRST to avoid the redirect-to-login logic
                        navController.navigate(Screen.Home.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        authViewModel.signOut()
                        cartViewModel.clearCart()
                    }
                )
            }
        }

        composable(Screen.FAQ.route) {
            FAQScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Management Routes
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                viewModel = managementViewModel,
                onLogout = {
                    // Navigate to Home FIRST
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                    authViewModel.signOut()
                    cartViewModel.clearCart()
                },
                onNavigateToFAQ = {
                    navController.navigate(Screen.FAQ.route)
                }
            )
        }
        composable(Screen.KitchenOrders.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            val isAdmin = userProfile?.role == "ADMIN"
            KitchenOrdersScreen(
                viewModel = managementViewModel,
                isAdmin = isAdmin,
                onNavigateToFAQ = {
                    navController.navigate(Screen.FAQ.route)
                }
            )
        }
        composable(Screen.DeliveryOrders.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            val isAdmin = userProfile?.role == "ADMIN"
            DeliveryOrdersScreen(
                viewModel = managementViewModel,
                isAdmin = isAdmin
            )
        }
    }
}
