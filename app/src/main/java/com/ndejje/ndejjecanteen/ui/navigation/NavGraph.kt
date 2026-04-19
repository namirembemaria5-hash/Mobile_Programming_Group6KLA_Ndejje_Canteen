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
                    if (role == "ADMIN") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    } else if (role == "KITCHEN") {
                        navController.navigate(Screen.KitchenOrders.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    } else if (role == "DELIVERY") {
                        navController.navigate(Screen.DeliveryOrders.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    } else {
                        navController.popBackStack()
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToFAQ = {
                    navController.navigate(Screen.FAQ.route)
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
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToFAQ = {
                    navController.navigate(Screen.FAQ.route)
                }
            )
        }

        composable(Screen.Orders.route) {
            OrderHistoryScreen(
                authViewModel = authViewModel,
                orderViewModel = orderViewModel,
                onOrderClick = { orderId ->
                    navController.navigate(Screen.OrderStatus.createRoute(orderId))
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
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
            ProfileScreen(
                authViewModel = authViewModel,
                onLogout = {
                    authViewModel.signOut()
                    cartViewModel.clearCart()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToFAQ = { navController.navigate(Screen.FAQ.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route)
                }
            )
        }

        // Management Routes
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                viewModel = managementViewModel,
                onLogout = {
                    authViewModel.signOut()
                    cartViewModel.clearCart()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToFAQ = { navController.navigate(Screen.FAQ.route) }
            )
        }
        composable(Screen.KitchenOrders.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            val isAdmin = userProfile?.role == "ADMIN"
            KitchenOrdersScreen(managementViewModel, isAdmin = isAdmin)
        }
        composable(Screen.DeliveryOrders.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            val isAdmin = userProfile?.role == "ADMIN"
            DeliveryOrdersScreen(
                viewModel = managementViewModel,
                isAdmin = isAdmin,
                deliveryPersonId = userProfile?.uid
            )
        }
        composable(Screen.DeliveryHistory.route) {
            val userProfile by authViewModel.userProfile.collectAsState()
            DeliveryHistoryScreen(
                viewModel = managementViewModel,
                deliveryPersonId = userProfile?.uid
            )
        }
        composable(Screen.FAQ.route) {
            FAQScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
