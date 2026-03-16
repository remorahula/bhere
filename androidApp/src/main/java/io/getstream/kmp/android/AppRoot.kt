package io.getstream.kmp.android

import androidx.compose.runtime.*
import io.getstream.kmp.android.ui.navigation.MainTabs
import io.getstream.kmp.android.ui.screens.login.LoginScreen
import io.getstream.kmp.auth.AuthTokens
import kotlinx.coroutines.launch


@Composable
fun AppRoot(onGoogleLogin: () -> Unit, onAppleLogin: () -> Unit) {
    val tokenStore = AndroidGraph.shared.tokenStore
    val tokens by tokenStore.tokens.collectAsState() // reactive

    if (tokens == null) {
        LoginScreen(
            onGoogleLogin = onGoogleLogin,
            onAppleLogin = onAppleLogin
        )
    } else {
        MainTabs()
    }
}