package io.getstream.kmp.android

import androidx.compose.runtime.*
import io.getstream.kmp.android.ui.navigation.MainTabs
import io.getstream.kmp.android.ui.screens.login.LoginScreen
import io.getstream.kmp.auth.AuthTokens
import kotlinx.coroutines.launch


@Composable
fun AppRoot(onGoogleLogin: () -> Unit, onAppleLogin: () -> Unit) {
    val scope = rememberCoroutineScope()
    var loggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loggedIn = AndroidGraph.shared.tokenStore.getTokens() != null
    }

    if (!loggedIn) {
        LoginScreen(
            onGoogleLogin = {
                onGoogleLogin()
                scope.launch {
                    kotlinx.coroutines.delay(1200)
                    loggedIn = AndroidGraph.shared.tokenStore.getTokens() != null
                }
            },
            onAppleLogin = {
                onAppleLogin()
                scope.launch {
                    kotlinx.coroutines.delay(1200)
                    loggedIn = AndroidGraph.shared.tokenStore.getTokens() != null
                }
            }
        )
    } else {
        MainTabs()
    }
}