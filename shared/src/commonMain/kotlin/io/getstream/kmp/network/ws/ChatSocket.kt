package io.getstream.kmp.network.ws

import io.ktor.client.request.header
import io.getstream.kmp.auth.TokenProvider
import io.getstream.kmp.core.ApiConfig
import io.getstream.kmp.models.MessageDto
import io.getstream.kmp.models.ThreadDto
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import kotlin.concurrent.Volatile

class ChatSocket(
  private val tokenProvider: TokenProvider,
  private val client: HttpClient,
  private val json: Json = Json { ignoreUnknownKeys = true; explicitNulls = false }
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val _incomingMessages = MutableSharedFlow<MessageDto>(extraBufferCapacity = 256)
  val incomingMessages: SharedFlow<MessageDto> = _incomingMessages.asSharedFlow()

  private val _threadUpdates = MutableSharedFlow<ThreadDto>(extraBufferCapacity = 256)
  val threadUpdates: SharedFlow<ThreadDto> = _threadUpdates.asSharedFlow()

  @Volatile
  private var running = false

  fun start() {
    if (running) return
    running = true
    scope.launch { reconnectLoop() }
  }

  suspend fun stop() {
    running = false
    scope.coroutineContext.cancelChildren()
  }

  private suspend fun reconnectLoop() {
    var backoffMs = 800L
    while (running && scope.isActive) {
      try {
        val token = tokenProvider.getAccessToken() ?: error("No access token")

        client.webSocket(
          urlString = ApiConfig.WS_URL,
          request = { header(HttpHeaders.Authorization, "Bearer $token") }
        ) {
          backoffMs = 800L

          // subscribe (server derives user from token)
          val subscribe = buildJsonObject {
            put("v", 1)
            put("type", "subscribe")
            put("data", buildJsonObject { put("threads", JsonNull) })
          }
          send(Frame.Text(subscribe.toString()))

          for (frame in incoming) {
            val text = (frame as? Frame.Text)?.readText() ?: continue
            handle(text)
          }
        }
      } catch (_: Throwable) {
        delay(backoffMs)
        backoffMs = (backoffMs * 1.7).toLong().coerceAtMost(10_000L)
      }
    }
  }

  private suspend fun handle(text: String) {
    val root = json.parseToJsonElement(text).jsonObject
    val type = root["type"]?.jsonPrimitive?.content ?: return
    val dataEl = root["data"]

    when (type) {
      "message.new" -> if (dataEl != null && dataEl !is JsonNull) {
        val msg = json.decodeFromJsonElement(WsMessageNew.serializer(), dataEl).message
        _incomingMessages.emit(msg)
      }
      "thread.updated" -> if (dataEl != null && dataEl !is JsonNull) {
        val th = json.decodeFromJsonElement(WsThreadUpdated.serializer(), dataEl).thread
        _threadUpdates.emit(th)
      }
      else -> Unit
    }
  }
}
