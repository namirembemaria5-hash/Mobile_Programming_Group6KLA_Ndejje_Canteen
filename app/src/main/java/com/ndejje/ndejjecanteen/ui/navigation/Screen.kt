package com.ndejje.ndejjecanteen.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Main (Customer)
    object Home : Screen("home")
    object Menu : Screen("menu/{category}") {
        fun createRoute(category: String) = "menu/$category"
    }
    object Cart : Screen("cart")
    object OrderStatus : Screen("order_status/{orderId}") {
        fun createRoute(orderId: String) = "order_status/$orderId"
    }
    object Orders : Screen("orders")
    object Profile : Screen("profile")
    object FAQ : Screen("faq")

    // Management
    object AdminDashboard : Screen("admin_dashboard")
    object KitchenOrders : Screen("kitchen_orders")
    object DeliveryOrders : Screen("delivery_orders")
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.Home.route),
    BottomNavItem("Cart", Icons.Default.ShoppingCart, Screen.Cart.route),
    BottomNavItem("Orders", Icons.Default.Receipt, Screen.Orders.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
)

val adminNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Screen.AdminDashboard.route),
    BottomNavItem("Kitchen", Icons.Default.Restaurant, Screen.KitchenOrders.route),
    BottomNavItem("Delivery", Icons.Default.LocalShipping, Screen.DeliveryOrders.route),
    BottomNavItem("Profile", Icons.Default.Person, Screen.Profile.route)
)
