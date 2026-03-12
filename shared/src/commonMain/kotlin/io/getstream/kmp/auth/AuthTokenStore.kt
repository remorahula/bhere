package io.getstream.kmp.auth

import kotlinx.serialization.json.Json

class AuthTokenStore(
  private val secureStore: SecureStore,
  private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {
  companion object { private const val KEY = "auth.tokens.json" }

  suspend fun save(tokens: AuthTokens) {
    val raw = json.encodeToString(AuthTokens.serializer(), tokens)
    secureStore.putString(KEY, raw)
  }

  suspend fun clear() = secureStore.delete(KEY)

  suspend fun getTokens(): AuthTokens? {
    val raw = secureStore.getString(KEY) ?: return null
    return runCatching { json.decodeFromString(AuthTokens.serializer(), raw) }.getOrNull()
  }

  suspend fun getAccessToken(): String? = getTokens()?.accessToken
}
