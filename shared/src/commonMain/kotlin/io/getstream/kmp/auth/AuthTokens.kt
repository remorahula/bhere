package io.getstream.kmp.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
  val accessToken: String,
  val refreshToken: String? = null,
  val expiresAtEpochMs: Long? = null
)
