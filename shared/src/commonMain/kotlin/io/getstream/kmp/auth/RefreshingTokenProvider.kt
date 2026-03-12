package io.getstream.kmp.auth

import io.getstream.kmp.core.AuthConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.TimeSource

class RefreshingTokenProvider(
    private val tokenStore: AuthTokenStore,
    private val authHttp: HttpClient,
    private val timeSource: TimeSource = TimeSource.Monotonic
) : TokenProvider {

    // refresh a bit before expiry to avoid race conditions
    private val leewayMs = 60_000L

    private val mutex = Mutex()

    override suspend fun getAccessToken(): String? {
        val tokens = tokenStore.getTokens() ?: return null

        val nowEpochMs = currentEpochMs()

        val expiresAt = tokens.expiresAtEpochMs
        val accessLooksValid =
            expiresAt != null && nowEpochMs < (expiresAt - leewayMs)

        if (accessLooksValid) return tokens.accessToken

        val refresh = tokens.refreshToken ?: return null

        // ensure only 1 refresh happens at a time
        return mutex.withLock {
            // re-check after acquiring lock (someone else may have refreshed)
            val latest = tokenStore.getTokens() ?: return null
            val latestExpiresAt = latest.expiresAtEpochMs
            val latestValid =
                latestExpiresAt != null && nowEpochMs < (latestExpiresAt - leewayMs)

            if (latestValid) return latest.accessToken

            val refreshed = refreshWithKeycloak(latest.refreshToken ?: refresh, nowEpochMs)

            tokenStore.save(refreshed)
            refreshed.accessToken
        }
    }

    private suspend fun refreshWithKeycloak(refreshToken: String, nowEpochMs: Long): AuthTokens {
        val resp: KeycloakTokenResponse = authHttp.submitForm(
            url = AuthConfig.TOKEN_ENDPOINT,
            formParameters = Parameters.build {
                append("grant_type", "refresh_token")
                append("client_id", AuthConfig.CLIENT_ID)
                append("refresh_token", refreshToken)
            }
        ).body()

        val expiresAt = nowEpochMs + max(1, resp.expiresIn) * 1000L

        return AuthTokens(
            accessToken = resp.accessToken,
            // IMPORTANT: Keycloak may rotate refresh tokens; if it sends a new one, store it.
            refreshToken = resp.refreshToken ?: refreshToken,
            expiresAtEpochMs = expiresAt
        )
    }

    private fun currentEpochMs(): Long {
        // You can replace this with platform epoch time if you prefer.
        // For your use (leeway + short expiry), system time is fine.
        return kotlin.system.getTimeMillis()
    }
}

@Serializable
private data class KeycloakTokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Long,
    @SerialName("refresh_token") val refreshToken: String? = null
)
