package io.getstream.kmp.auth

import io.getstream.kmp.core.AuthConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.TimeSource

class RefreshingTokenProvider(
    private val tokenStore: AuthTokenStore,
    private val authHttp: HttpClient,
    private val timeSource: TimeSource = TimeSource.Monotonic
) : TokenProvider {

    private val leewayMs = 60_000L
    private val mutex = Mutex()

    override suspend fun getAccessToken(): String? {
        val tokens = tokenStore.tokens.value ?: return null

        val nowEpochMs = currentEpochMs()
        val expiresAt = tokens.expiresAtEpochMs
        val accessLooksValid =
            expiresAt != null && nowEpochMs < (expiresAt - leewayMs)

        if (accessLooksValid) return tokens.accessToken

        val refresh = tokens.refreshToken ?: return null

        return mutex.withLock {
            // re-check after acquiring lock
            val latest = tokenStore.tokens.value ?: return null
            val latestValid =
                latest.expiresAtEpochMs != null && nowEpochMs < (latest.expiresAtEpochMs - leewayMs)

            if (latestValid) return latest.accessToken

            // try refresh
            val refreshed = refreshWithKeycloak(latest.refreshToken ?: refresh, nowEpochMs) ?: return@withLock null
            tokenStore.save(refreshed)
            refreshed.accessToken
        }
    }

    private suspend fun refreshWithKeycloak(refreshToken: String, nowEpochMs: Long): AuthTokens? {
        return try {
            val resp: KeycloakTokenResponse = authHttp.submitForm(
                url = AuthConfig.TOKEN_ENDPOINT,
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("client_id", AuthConfig.CLIENT_ID)
                    append("refresh_token", refreshToken)
                }
            ).body()

            val expiresAt = nowEpochMs + max(1, resp.expiresIn) * 1000L

            AuthTokens(
                accessToken = resp.accessToken,
                refreshToken = resp.refreshToken ?: refreshToken,
                expiresAtEpochMs = expiresAt
            )
        } catch (e: Exception) {
            tokenStore.clear() // logout if refresh fails
            null
        }
    }

    private fun currentEpochMs(): Long {
        return timeSource.markNow().elapsedNow().inWholeMilliseconds
    }
}

@kotlinx.serialization.Serializable
private data class KeycloakTokenResponse(
    @kotlinx.serialization.SerialName("access_token") val accessToken: String,
    @kotlinx.serialization.SerialName("expires_in") val expiresIn: Long,
    @kotlinx.serialization.SerialName("refresh_token") val refreshToken: String? = null
)