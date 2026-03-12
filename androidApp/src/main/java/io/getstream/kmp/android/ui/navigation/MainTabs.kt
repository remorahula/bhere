package io.getstream.kmp.android.ui.navigation

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import io.getstream.kmp.android.ui.screens.chats.ChatsScreen
import io.getstream.kmp.android.ui.screens.reals.RealsScreen

enum class Tab { Reals, Chats }

@Composable
fun MainTabs() {
    var tab by remember { mutableStateOf(Tab.Reals) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tab == Tab.Reals,
                    onClick = { tab = Tab.Reals },
                    icon = { Icon(Icons.Default.CameraAlt, null) },
                    label = { Text("Reals") }
                )
                NavigationBarItem(
                    selected = tab == Tab.Chats,
                    onClick = { tab = Tab.Chats },
                    icon = { Icon(Icons.Default.Chat, null) },
                    label = { Text("Chats") }
                )
            }
        }
    ) { padding ->
        when (tab) {
            Tab.Reals -> RealsScreen(padding)
            Tab.Chats -> ChatsScreen(padding)
        }
    }
}