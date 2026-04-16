package com.ndejje.ndejjecanteen

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.messaging.FirebaseMessaging
import com.ndejje.ndejjecanteen.ui.navigation.CanteenNavGraph
import com.ndejje.ndejjecanteen.ui.navigation.bottomNavItems
import com.ndejje.ndejjecanteen.ui.navigation.adminNavItems
import com.ndejje.ndejjecanteen.ui.navigation.Screen
import com.ndejje.ndejjecanteen.ui.theme.NdejjeCanteenTheme
import com.ndejje.ndejjecanteen.ui.viewmodel.*
import com.ndejje.ndejjecanteen.utils.CanteenFirebaseMessagingService

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setupNotificationChannel()
        requestPermissionsIfNeeded()
        saveFcmToken()

        setContent {
            NdejjeCanteenTheme {
                MainScreen()
            }
        }
    }

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
                "ADMIN" -> adminNavItems.filter { it.route == Screen.AdminDashboard.route || it.route == Screen.Profile.route }
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

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CanteenFirebaseMessagingService.CHANNEL_ID,
                CanteenFirebaseMessagingService.CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CanteenFirebaseMessagingService.CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissionsToRequest = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 101)
        }
    }

    private fun saveFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                getSharedPreferences("canteen_prefs", Context.MODE_PRIVATE).edit().putString("fcm_token", token).apply()
            }
        }
    }
}
