package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var editName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    
    var isChangingPassword by remember { mutableStateOf(false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPasswordConfirmDialog by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

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
                text = userProfile?.name ?: "User",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = currentEmail,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_huge)))

            // Error Display
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { authViewModel.clearError() }) {
                            Icon(Icons.Default.Close, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

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
                        if (!isEditing && !isChangingPassword) {
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
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium)),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
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
                                if (uiState.isLoading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White)
                                else Text("Save")
                            }
                        }
                    } else if (!isChangingPassword) {
                        ProfileInfoItem(icon = Icons.Default.Person, label = "Name", value = userProfile?.name ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileInfoItem(icon = Icons.Default.Phone, label = "Phone", value = userProfile?.phone ?: "-")
                        HorizontalDivider(modifier = Modifier.padding(vertical = dimensionResource(R.dimen.spacing_medium)), color = MaterialTheme.colorScheme.surfaceVariant)
                        ProfileInfoItem(icon = Icons.Default.Email, label = "Email", value = currentEmail)
                    }
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

            // Password Security Card
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
                        Text("Security", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        if (!isChangingPassword && !isEditing) {
                            TextButton(onClick = { isChangingPassword = true }) {
                                Text("Change Password")
                            }
                        }
                    }

                    if (isChangingPassword) {
                        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
                        
                        OutlinedTextField(
                            value = oldPassword,
                            onValueChange = { oldPassword = it },
                            label = { Text("Old Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                                }
                            }
                        )

                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text("New Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = repeatPassword,
                            onValueChange = { repeatPassword = it },
                            label = { Text("Repeat New Password") },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            isError = repeatPassword.isNotEmpty() && repeatPassword != newPassword,
                            supportingText = {
                                if (repeatPassword.isNotEmpty() && repeatPassword != newPassword) {
                                    Text("Passwords do not match", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { 
                                    isChangingPassword = false
                                    oldPassword = ""; newPassword = ""; repeatPassword = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text("Cancel") }
                            
                            Button(
                                onClick = {
                                    if (newPassword == repeatPassword && newPassword.isNotEmpty() && oldPassword.isNotEmpty()) {
                                        showPasswordConfirmDialog = true
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                enabled = newPassword == repeatPassword && newPassword.isNotEmpty() && oldPassword.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
                            ) { Text("Update") }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = CanteenGreen, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Password last changed: Recently", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
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

    if (showPasswordConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordConfirmDialog = false },
            title = { Text("Confirm Password Change") },
            text = { Text("Are you sure you want to change your password? You will need to use your new password next time you log in.") },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordConfirmDialog = false
                        authViewModel.changePassword(oldPassword, newPassword) { success ->
                            if (success) {
                                isChangingPassword = false
                                oldPassword = ""; newPassword = ""; repeatPassword = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
                ) { Text("Yes, Change It") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordConfirmDialog = false }) { Text("No, Keep Old") }
            }
        )
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
