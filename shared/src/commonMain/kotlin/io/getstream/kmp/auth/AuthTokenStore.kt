package io.getstream.kmp.auth

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

class AuthTokenStore(
  private val secureStore: SecureStore,
  private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {
  companion object { private const val KEY = "auth.tokens.json" }

  // Reactive StateFlow
  private val _tokens = MutableStateFlow<AuthTokens?>(null)
  val tokens: StateFlow<AuthTokens?> get() = _tokens.asStateFlow()

  init {
    // optionally, load tokens from secure storage at startup

    loadFromDisk()
  }

  private fun loadFromDisk() {
    val raw = runCatching { secureStore.getString(KEY) }.getOrNull() ?: return
    val stored = runCatching { json.decodeFromString(AuthTokens.serializer(), raw) }.getOrNull()
    _tokens.value = stored
  }

  // Save tokens to disk AND update StateFlow
  suspend fun save(tokens: AuthTokens) {
    _tokens.value = tokens
    val raw = json.encodeToString(AuthTokens.serializer(), tokens)
    secureStore.putString(KEY, raw)
  }

  // Clear tokens from disk AND update StateFlow
  suspend fun clear() {
    _tokens.value = null
    secureStore.delete(KEY)
  }

  // Quick helper to get access token
  fun getAccessToken(): String? = _tokens.value?.accessToken
}
