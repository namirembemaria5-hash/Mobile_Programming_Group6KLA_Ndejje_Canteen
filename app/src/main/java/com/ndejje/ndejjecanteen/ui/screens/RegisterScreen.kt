package com.ndejje.ndejjecanteen.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ndejje.ndejjecanteen.R
import com.ndejje.ndejjecanteen.ui.theme.*
import com.ndejje.ndejjecanteen.ui.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToFAQ: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onRegisterSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CanteenGreen, CanteenGreenLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = dimensionResource(R.dimen.screen_padding),
                        vertical = dimensionResource(R.dimen.spacing_small)
                    ),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = onNavigateToHome,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Home")
                }
            }

            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))
            Text(
                text = "🍳",
                fontSize = dimensionResource(R.dimen.text_size_emoji_large).value.sp,
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_small))
            )
            Text(
                text = "Join Ndejje Canteen",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Create an account to start ordering",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = dimensionResource(R.dimen.spacing_huge))
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = dimensionResource(R.dimen.screen_padding_extra_large)),
                shape = RoundedCornerShape(dimensionResource(R.dimen.radius_card)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(dimensionResource(R.dimen.elevation_medium))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(dimensionResource(R.dimen.screen_padding_extra_large)),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Error message
                    val errorToShow = localError ?: uiState.error
                    AnimatedVisibility(visible = errorToShow != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = dimensionResource(R.dimen.spacing_large)),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(dimensionResource(R.dimen.radius_medium))
                        ) {
                            Text(
                                text = "⚠️ $errorToShow",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(dimensionResource(R.dimen.spacing_medium))
                            )
                        }
                    }

                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it; localError = null; authViewModel.clearError() },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = CanteenGreen) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_medium)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Phone field
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; localError = null; authViewModel.clearError() },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, tint = CanteenGreen) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_medium)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; localError = null; authViewModel.clearError() },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = CanteenGreen) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_medium)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; localError = null; authViewModel.clearError() },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CanteenGreen) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_medium)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )

                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; localError = null; authViewModel.clearError() },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = CanteenGreen) },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = dimensionResource(R.dimen.spacing_extra_large)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    // Sign Up button
                    Button(
                        onClick = {
                            if (password != confirmPassword) localError = "Passwords do not match"
                            else if (name.isBlank() || phone.isBlank() || email.isBlank() || password.isBlank()) localError = "All fields are required"
                            else authViewModel.signUp(email, password, name, phone)
                        },
                        modifier = Modifier.fillMaxWidth().height(dimensionResource(R.dimen.box_size_large)),
                        shape = RoundedCornerShape(dimensionResource(R.dimen.radius_button)),
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = CanteenGreen)
                    ) {
                        if (uiState.isLoading) CircularProgressIndicator(
                            modifier = Modifier.size(dimensionResource(R.dimen.icon_size_medium)),
                            color = Color.White,
                            strokeWidth = dimensionResource(R.dimen.border_width_medium)
                        )
                        else Text("Create Account", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_large)))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        TextButton(onClick = onNavigateToLogin) {
                            Text("Sign In", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CanteenGreen)
                        }
                    }

                    TextButton(
                        onClick = onNavigateToFAQ,
                        modifier = Modifier.padding(top = dimensionResource(R.dimen.spacing_small))
                    ) {
                        Text(
                            "Frequently Asked Questions (FAQs)",
                            style = MaterialTheme.typography.labelLarge,
                            color = CanteenGreen.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(dimensionResource(R.dimen.spacing_huge)))
        }
    }
}
