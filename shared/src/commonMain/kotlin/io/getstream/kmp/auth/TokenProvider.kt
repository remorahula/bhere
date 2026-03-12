package io.getstream.kmp.auth

interface TokenProvider {
  suspend fun getAccessToken(): String?
}

class StoredTokenProvider(private val tokenStore: AuthTokenStore) : TokenProvider {
  override suspend fun getAccessToken(): String? = tokenStore.getAccessToken()
}
