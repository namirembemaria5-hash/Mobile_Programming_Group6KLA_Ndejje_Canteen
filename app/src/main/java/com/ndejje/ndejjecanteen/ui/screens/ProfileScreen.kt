package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    onLogout: () -> Unit
) {
    val userProfile by authViewModel.userProfile.collectAsState()
    val uiState by authViewModel.uiState.collectAsState()
    val currentEmail = authViewModel.currentUserEmail ?: ""

    var isEditing by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userProfile?.name ?: "") }
    var editPhone by remember { mutableStateOf(userProfile?.phone ?: "") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            editName = it.name
            editPhone = it.phone
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.screen_padding_large)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(CanteenGreen.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (userProfile?.name?.take(1) ?: "S").uppercase(),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = CanteenGreen
                )
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            Text(
                text = userProfile?.name ?: "Student Name",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_huge)))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding_large))) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!isEditing) {
                            TextButton(onClick = { isEditing = true }) {
                                Text("Edit")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    if (isEditing) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text("Full Name") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_medium)),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                        )
                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_large)),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_small))
                        ) {
                            OutlinedButton(
                                onClick = { isEditing = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                            ) {
                                Text("Cancel")
                            }
                            Button(
                                onClick = {
                                    authViewModel.updateProfile(editName, editPhone)
                                    isEditing = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                                colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
                            ) {
                                Text("Save")
                            }
                        }
                    } else {
                        ProfileInfoItem(icon = Icons.Default.Person, label = "Name", value = userProfile?.name ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileInfoItem(icon = Icons.Default.Phone, label = "Phone", value = userProfile?.phone ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileInfoItem(icon = Icons.Default.Email, label = "Email", value = currentEmail)
                    }
                }
            }

            // Success message
            AnimatedVisibility(visible = uiState.error == null && uiState.isLoading == false && !isEditing) {
                // You could show a snackbar or small toast-like card here
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_extra_large)))

            // Support Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_extra_large)),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(dimensionResource(R.dimen.screen_padding_large))) {
                    Text("Support", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_medium)))
                    ProfileInfoItem(icon = Icons.Default.Help, label = "Help Center", value = "FAQs & Contact")
                    HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)), color = MaterialTheme.colorScheme.surfaceVariant)
                    ProfileInfoItem(icon = Icons.Default.Info, label = "App Version", value = "1.0.0")
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("Are you sure you want to sign out of your account?") },
            confirmButton = {
                TextButton(onClick = onLogout) {
                    Text("Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ProfileInfoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = CanteenGreen, modifier = Modifier.size(dimensionResource(R.dimen.icon_size_small)))
        Spacer(modifier = Modifier.width(dimensionResource(R.dimen.spacing_medium)))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
    }
}
