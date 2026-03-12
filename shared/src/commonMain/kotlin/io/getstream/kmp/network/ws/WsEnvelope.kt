package io.getstream.kmp.network.ws

import kotlinx.serialization.Serializable

@Serializable
data class WsEnvelope<T>(
  val v: Int = 1,
  val id: String? = null,
  val type: String,
  val ts: String? = null,
  val data: T? = null
)
