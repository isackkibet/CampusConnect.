package com.campusconnect.app.features.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.campusconnect.app.ui.components.DemoModeBanner
import com.campusconnect.app.ui.components.PrimaryButton
import com.campusconnect.app.ui.components.SecondaryButton
import com.campusconnect.app.ui.components.TextFieldSupportError

@Composable
private fun AuthHeader(title: String, subtitle: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(76.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    modifier = Modifier.size(38.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun LoginScreen(
    onRegister: () -> Unit,
    onForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.height(28.dp))
        AuthHeader("Welcome back", "Sign in to your CampusConnect account")
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = { Text("Password") },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(14.dp),
        )

        if (state.error != null) {
            Spacer(Modifier.height(10.dp))
            Text(state.error ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (state.info != null) {
            Spacer(Modifier.height(10.dp))
            Text(state.info ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onForgotPassword) { Text("Forgot password?") }
        }

        PrimaryButton(
            text = "Sign in",
            loading = state.loading,
            onClick = { viewModel.login(email, password) },
        )
        Spacer(Modifier.height(12.dp))
        SecondaryButton(
            text = "Continue with demo account",
            onClick = { viewModel.demoLogin() },
        )
        Spacer(Modifier.height(24.dp))
        DemoModeBanner(
            "Demo mode is active until Supabase is configured. Demo sign-in never contacts a server and accepts any valid email with an 8+ character password.",
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("New here?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRegister) { Text("Create an account") }
        }
    }
}

@Composable
fun RegisterScreen(
    onLogin: () -> Unit,
    onVerify: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var accepted by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    LaunchedEffect(state.navigateToVerify) {
        if (state.navigateToVerify) {
            viewModel.clearMessages()
            onVerify()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        AuthHeader("Create your account", "Join the Nairobi National Polytechnic community")
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; viewModel.clearMessages() },
            label = { Text("Full name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email address") },
            leadingIcon = { Icon(Icons.Filled.Mail, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearMessages() },
            label = { Text("Password") },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle password visibility",
                    )
                }
            },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("At least 8 characters, including a letter and a number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = { confirm = it; viewModel.clearMessages() },
            label = { Text("Confirm password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordTransformation,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(14.dp),
        )
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = accepted, onCheckedChange = { accepted = it })
            Text(
                "I agree to the community guidelines and understand this demo stores data only on this device.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (state.error != null) {
            TextFieldSupportError(state.error)
        }
        Spacer(Modifier.height(8.dp))
        PrimaryButton(
            text = "Create account",
            loading = state.loading,
            onClick = { viewModel.register(name, email, password, confirm, accepted) },
        )
        Spacer(Modifier.height(12.dp))
        DemoModeBanner("Demo mode: no email is sent. Use verification code 123456 on the next screen.")
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text("Already registered?", style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onLogin) { Text("Sign in") }
        }
    }
}

private val PasswordTransformation = PasswordVisualTransformation()

@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        AuthHeader("Reset your password", "We will send a reset link if the account exists")
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearMessages() },
            label = { Text("Email address") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(14.dp),
        )
        TextFieldSupportError(state.error)
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Send reset instructions",
            loading = state.loading,
            onClick = { viewModel.requestPasswordReset(email) },
        )
        if (state.resetSent) {
            Spacer(Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text(state.info ?: "", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SecondaryButton(text = "Back to sign in", onClick = onBack)
        Spacer(Modifier.height(20.dp))
        DemoModeBanner("Demo mode does not send email. Configure Supabase Auth in docs/DEPLOYMENT.md to enable real reset emails.")
    }
}

@Composable
fun VerifyEmailScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        AuthHeader("Verify your email", "Enter the 6-digit code to activate your account")
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it.filter(Char::isDigit).take(6); viewModel.clearMessages() },
            label = { Text("Verification code") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(14.dp),
        )
        TextFieldSupportError(state.error)
        if (state.info != null) {
            Spacer(Modifier.height(8.dp))
            Text(state.info ?: "", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.height(16.dp))
        PrimaryButton(
            text = "Verify email",
            loading = state.loading,
            onClick = { viewModel.verifyEmail(code) },
        )
        Spacer(Modifier.height(10.dp))
        SecondaryButton(text = "Resend code", onClick = { viewModel.resendVerification() })
        Spacer(Modifier.height(16.dp))
        DemoModeBanner("Demo mode: no email is sent. Use code 123456. Real verification requires Supabase Auth email templates.")
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBack) { Text("Use a different email") }
    }
}
