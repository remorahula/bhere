package io.getstream.kmp.android.ui.screens.login

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(onGoogleLogin: () -> Unit, onAppleLogin: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
        Text("Welcome", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onGoogleLogin) { Text("Continue with Google") }
        Button(onClick = onAppleLogin) { Text("Continue with Apple") }
    }
}

@Composable
fun LoginButtons(
    onGoogle: () -> Unit,
    onApple: () -> Unit
) {
    Column {
        Button(onClick = onGoogle) { Text("Continue with Google") }
        Button(onClick = onApple) { Text("Continue with Apple") }
    }
}