package io.getstream.kmp.android.ui.screens.reals


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.kmp.models.GenderPreference
import io.getstream.kmp.android.AndroidGraph
import io.getstream.kmp.android.platform.AndroidLocationProvider
import kotlinx.coroutines.launch

@Composable
fun RealsScreen(padding: PaddingValues) {
    val scope = rememberCoroutineScope()
    val repo = AndroidGraph.shared.realsRepository
    val locationProvider = remember { AndroidLocationProvider(AndroidGraph.shared.tokenStore /* not context */.let { io.getstream.kmp.platform.AndroidPlatform.appContext }) }

    var items by remember { mutableStateOf(emptyList<io.getstream.kmp.models.RealDto>()) }
    var radius by remember { mutableIntStateOf(3000) }
    var gender by remember { mutableStateOf<GenderPreference?>(null) } // null => server default
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        scope.launch {
            busy = true; error = null
            try {
                val loc = locationProvider.current()
                val resp = repo.nearby(loc.lat, loc.lng, radius, gender)
                items = resp.items
            } catch (t: Throwable) {
                error = t.message
            } finally {
                busy = false
            }
        }
    }

    LaunchedEffect(Unit) { refresh() }

    Column(Modifier.padding(padding).fillMaxSize().padding(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { refresh() }, enabled = !busy) { Text("Refresh") }
            Button(onClick = {
                // TODO: open capture flow screen/activity to get dual JPEGs, then:
                // repo.upload(front, rear, capturedAtIso, lat, lng, idempotencyKey)
            }) { Text("Capture") }
        }

        Spacer(Modifier.height(8.dp))

        if (error != null) Text("Error: $error", color = MaterialTheme.colorScheme.error)

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items, key = { it.id }) { real ->
                Card {
                    Column(Modifier.padding(12.dp)) {
                        Text("Real: ${real.id}")
                        Text("Distance: ${real.distanceMeters}m")
                        Text("Likes: ${real.engagement.likeCount}")

                        Spacer(Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                scope.launch {
                                    try { repo.like(real.id); refresh() } catch (_: Throwable) {}
                                }
                            }) {
                                Text(if (real.engagement.likedByMe) "Liked" else "Like")
                            }
                        }
                    }
                }
            }
        }
    }
}