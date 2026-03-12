package io.getstream.kmp.network.ws

import io.getstream.kmp.models.MessageDto
import io.getstream.kmp.models.ThreadDto
import kotlinx.serialization.Serializable

@Serializable data class WsSubscribe(val threads: List<String>? = null)
@Serializable data class WsSubscribed(val threads: List<String>)
@Serializable data class WsMessageNew(val message: MessageDto)
@Serializable data class WsThreadUpdated(val thread: ThreadDto)
@Serializable data class WsError(val code: String, val message: String)
