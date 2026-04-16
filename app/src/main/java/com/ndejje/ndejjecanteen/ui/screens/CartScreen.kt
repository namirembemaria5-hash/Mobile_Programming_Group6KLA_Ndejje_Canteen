package com.ndejje.ndejjecanteen.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.data.model.*
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.*
import com.ndejje.ndejjecanteen.utils.LocationHelper
import com.ndejje.ndejjecanteen.utils.formatUGX
import com.ndejje.ndejjecanteen.utils.getItemEmoji
import com.ndejje.ndejjecanteen.utils.isWeekend
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun CartScreen(
    cartViewModel: CartViewModel,
    authViewModel: AuthViewModel,
    orderViewModel: OrderViewModel,
    onOrderPlaced: (String) -> Unit,
    onRequireLogin: () -> Unit
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val totalPrice by cartViewModel.totalPrice.collectAsState()
    val orderState by orderViewModel.orderUiState.collectAsState()
    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf("") }
    var isPreOrder by remember { mutableStateOf(false) }
    var preOrderDate by remember { mutableStateOf("") }
    var locationStatus by remember { mutableStateOf("Tap to get location") }
    var currentLocation by remember { mutableStateOf<OrderLocation?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var mobileMoneyNumber by remember { mutableStateOf("") }

    // Pre-fill phone number from profile
    LaunchedEffect(userProfile) {
        if (mobileMoneyNumber.isEmpty() && !userProfile?.phone.isNullOrBlank()) {
            mobileMoneyNumber = userProfile?.phone ?: ""
        }
    }

    val locationHelper = remember { LocationHelper(context) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            locationStatus = "Getting location..."
            scope.launch {
                val loc = locationHelper.getLastKnownLocation()
                if (loc != null) {
                    currentLocation = OrderLocation(loc.latitude, loc.longitude, "On Campus")
                    locationStatus = "📍 Location captured (${String.format("%.4f", loc.latitude)}, ${String.format("%.4f", loc.longitude)})"
                } else {
                    locationStatus = "Could not get location"
                }
            }
        } else {
            locationStatus = "Location permission denied"
        }
    }

    LaunchedEffect(orderState) {
        if (orderState is OrderUiState.Success) {
            val orderId = (orderState as OrderUiState.Success).orderId
            cartViewModel.clearCart()
            // Reset state AFTER navigation or let the next screen handle it
            onOrderPlaced(orderId)
            orderViewModel.resetState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "🛒 Your Cart",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (cartItems.isNotEmpty()) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Clear cart",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (cartItems.isEmpty() && orderState !is OrderUiState.Success) {
            EmptyCartState(modifier = Modifier.padding(paddingValues))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(dimensionResource(R.dimen.screen_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
            ) {
                items(cartItems, key = { it.menuItem.id }) { cartItem ->
                    CartItemCard(
                        cartItem = cartItem,
                        onAdd = { cartViewModel.addToCart(cartItem.menuItem) },
                        onRemove = { cartViewModel.removeFromCart(cartItem.menuItem.id) },
                        onDelete = { cartViewModel.deleteFromCart(cartItem.menuItem.id) }
                    )
                }

                // Order details
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                    Text(
                        "Order Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                    )

                    // Location button
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        colors = CardDefaults.cardColors(
                            containerColor = if (currentLocation != null)
                                CanteenGreenContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(dimensionResource(R.dimen.radius_button)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (currentLocation != null) CanteenGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Pickup Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                Text(locationStatus, style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                            }
                            TextButton(onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }) {
                                Text(if (currentLocation != null) "Refresh" else "Get Location")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))

                    // Pre-order toggle
                    val hasSpecialItems = cartItems.any { it.menuItem.isWeekendOnly }
                    if (hasSpecialItems && !isWeekend()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                            colors = CardDefaults.cardColors(containerColor = CanteenAmberContainer)
                        ) {
                            Column(modifier = Modifier.padding(dimensionResource(R.dimen.radius_button))) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("📅 Pre-Order", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = CanteenBrown)
                                        Text("Schedule for weekend pickup", style = MaterialTheme.typography.bodySmall, color = CanteenBrown.copy(alpha = 0.75f))
                                    }
                                    Switch(
                                        checked = isPreOrder,
                                        onCheckedChange = { isPreOrder = it },
                                        colors = SwitchDefaults.colors(checkedThumbColor = CanteenGreen)
                                    )
                                }
                                AnimatedVisibility(visible = isPreOrder) {
                                    OutlinedTextField(
                                        value = preOrderDate,
                                        onValueChange = { preOrderDate = it },
                                        label = { Text("Pickup date (e.g. Sat 18 Jan)") },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = dimensionResource(R.dimen.spacing_small)),
                                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Special instructions (optional)") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        maxLines = 3
                    )
                }

                // Payment Method
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_padding)))
                    Text(
                        "Payment Method",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_medium))
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))) {
                        PaymentMethod.values().forEach { method ->
                            val isSelected = selectedPaymentMethod == method
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPaymentMethod = method },
                                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CanteenGreen.copy(alpha = 0.1f) 
                                                     else MaterialTheme.colorScheme.surface
                                ),
                                border = if (isSelected) BorderStroke(2.dp, CanteenGreen) 
                                         else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding)),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(method.emoji, fontSize = dimensionResource(R.dimen.text_size_heading_large).value.sp)
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                                    Text(
                                        method.displayName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedPaymentMethod = method },
                                        colors = RadioButtonDefaults.colors(selectedColor = CanteenGreen)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Mobile Money Number Input
                    AnimatedVisibility(visible = selectedPaymentMethod == PaymentMethod.MTN_MOMO || selectedPaymentMethod == PaymentMethod.AIRTEL_MONEY) {
                        Column(modifier = Modifier.padding(top = dimensionResource(R.dimen.screen_padding))) {
                            Text(
                                "Mobile Money Number",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
                            )
                            OutlinedTextField(
                                value = mobileMoneyNumber,
                                onValueChange = { if (it.length <= 10) mobileMoneyNumber = it },
                                label = { Text("Enter 10-digit number") },
                                placeholder = { Text("07XXXXXXXX") },
                                leadingIcon = { Icon(Icons.Default.PhoneAndroid, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            Text(
                                "A payment prompt will be sent to this number.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_extra_small), start = dimensionResource(R.dimen.spacing_extra_small))
                            )
                        }
                    }
                }

                // Order summary
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))
                    OrderSummaryCard(
                        cartItems = cartItems,
                        total = totalPrice
                    )
                }

                // Place Order button
                item {
                    val isLoading = orderState is OrderUiState.Loading || orderState is OrderUiState.ProcessingPayment
                    val errorMessage = (orderState as? OrderUiState.Error)?.message
                    
                    // Validation for Mobile Money
                    val isMomo = selectedPaymentMethod == PaymentMethod.MTN_MOMO || selectedPaymentMethod == PaymentMethod.AIRTEL_MONEY
                    val isNumberValid = !isMomo || (mobileMoneyNumber.length >= 10)

                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                        ) {
                            Text(
                                text = "⚠️ $errorMessage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
                            )
                        }
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
                    }

                    Button(
                        onClick = {
                            if (!isLoggedIn) {
                                onRequireLogin()
                                return@Button
                            }
                            
                            val uid = authViewModel.currentUserId ?: return@Button
                            val profile = userProfile
                            orderViewModel.placeOrder(
                                userId = uid,
                                userName = profile?.name ?: "Student",
                                userPhone = mobileMoneyNumber.ifBlank { profile?.phone ?: "" },
                                cartItems = cartItems,
                                location = currentLocation ?: OrderLocation(address = "On Campus"),
                                isPreOrder = isPreOrder,
                                preOrderDate = preOrderDate,
                                notes = notes,
                                paymentMethod = selectedPaymentMethod
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.box_size_extra_large)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
                        enabled = !isLoading && isNumberValid,
                        colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
                    ) {
                        if (isLoading) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                                    color = Color.White, strokeWidth = 2.dp)
                                if (orderState is OrderUiState.ProcessingPayment) {
                                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
                                    Text("Processing ${(orderState as OrderUiState.ProcessingPayment).method}...")
                                }
                            }
                        } else {
                            Icon(Icons.Default.ShoppingCartCheckout, contentDescription = null,
                                modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)))
                            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_small)))
                            val buttonText = if (isLoggedIn) {
                                when (selectedPaymentMethod) {
                                    PaymentMethod.CASH -> "Place Order — ${formatUGX(totalPrice)}"
                                    else -> if (isNumberValid) "Pay & Order — ${formatUGX(totalPrice)}" else "Enter Phone Number"
                                }
                            } else "Sign in to Order"
                            
                            Text(
                                buttonText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.screen_padding)))
                }
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear Cart?") },
            text = { Text("Remove all items from your cart?") },
            confirmButton = {
                TextButton(onClick = {
                    cartViewModel.clearCart()
                    showClearDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onAdd: () -> Unit,
    onRemove: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.spacing_medium)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(dimensionResource(R.dimen.box_size_medium))
                    .clip(RoundedCornerShape(dimensionResource(R.dimen.spacing_small)))
                    .background(CanteenGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(getItemEmoji(cartItem.menuItem), fontSize = dimensionResource(R.dimen.text_size_heading_large).value.sp)
            }
            Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
            Column(modifier = Modifier.weight(1f)) {
                Text(cartItem.menuItem.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    "${formatUGX(cartItem.menuItem.price)} × ${cartItem.quantity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
                Text(
                    formatUGX(cartItem.subtotal),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = CanteenGreen
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_extra_small))) {
                FilledIconButton(onClick = onRemove, modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CanteenGreen.copy(alpha = 0.15f),
                        contentColor = CanteenGreen)) {
                    Icon(Icons.Default.Remove, null, modifier = Modifier.size(dimensionResource(R.dimen.radius_button)))
                }
                Text(cartItem.quantity.toString(), style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                FilledIconButton(onClick = onAdd, modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = CanteenGreen,
                        contentColor = Color.White)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(dimensionResource(R.dimen.radius_button)))
                }
                Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_extra_small)))
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Close, null,
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(dimensionResource(R.dimen.screen_padding)))
                }
            }
        }
    }
}

@Composable
fun OrderSummaryCard(cartItems: List<CartItem>, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_large)),
        colors = CardDefaults.cardColors(containerColor = CanteenGreenContainer),
        border = BorderStroke(1.dp, CanteenGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding))) {
            Text("Order Summary", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = CanteenGreen)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_small)))
            cartItems.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${item.menuItem.name} × ${item.quantity}",
                        style = MaterialTheme.typography.bodyMedium)
                    Text(formatUGX(item.subtotal), style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_small)), color = CanteenGreen.copy(alpha = 0.3f))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CanteenGreen)
                Text(formatUGX(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = CanteenGreen)
            }
        }
    }
}

@Composable
fun EmptyCartState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🛒", fontSize = dimensionResource(R.dimen.box_size_extra_large).value.sp)
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
            Text("Your cart is empty", style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold)
            Text("Browse the menu and add items!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
        }
    }
}
