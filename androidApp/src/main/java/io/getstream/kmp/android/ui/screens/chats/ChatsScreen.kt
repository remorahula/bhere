package io.getstream.kmp.android.ui.screens.chats


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.kmp.android.AndroidGraph
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val repo = AndroidGraph.shared.chatRepository

    var threads by remember { mutableStateOf(emptyList<io.getstream.kmp.models.ThreadDto>()) }

    LaunchedEffect(Unit) {
        // start realtime while screen visible (simple)
        repo.startRealtime()
        repo.refreshThreads()
        // collect StateFlow
        repo.threads.collect { threads = it }
    }

    Column(Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { scope.launch { repo.refreshThreads() } }) { Text("Refresh") }
        }
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(threads, key = { it.id }) { th ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text(th.otherUser.displayName ?: "Unknown")
                        Text(th.lastMessage?.text ?: "", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}