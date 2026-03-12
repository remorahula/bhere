package io.getstream.kmp.repo

import io.getstream.kmp.models.*
import io.getstream.kmp.network.HttpApi
import io.getstream.kmp.network.ws.ChatSocket
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ChatRepository(
  private val api: HttpApi,
  private val socket: ChatSocket
) {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

  private val _threads = MutableStateFlow<List<ThreadDto>>(emptyList())

  @NativeCoroutinesState
  val threads: StateFlow<List<ThreadDto>> = _threads.asStateFlow()

  private val messageFlows = mutableMapOf<String, MutableStateFlow<List<MessageDto>>>()

  fun startRealtime() {
    socket.start()

    scope.launch {
      socket.threadUpdates.collect { updated ->
        val cur = _threads.value.toMutableList()
        val idx = cur.indexOfFirst { it.id == updated.id }
        if (idx >= 0) cur[idx] = updated else cur.add(0, updated)
        _threads.value = cur
      }
    }

    scope.launch {
      socket.incomingMessages.collect { msg ->
        val flow = messageFlows.getOrPut(msg.threadId) { MutableStateFlow(emptyList()) }
        flow.value = (flow.value + msg).distinctBy { it.id }
      }
    }
  }

  suspend fun stopRealtime() {
    socket.stop()
    scope.coroutineContext.cancelChildren()
  }

  suspend fun refreshThreads() {
    _threads.value = api.getThreads().items
  }

  @NativeCoroutines
  fun observeMessages(threadId: String): Flow<List<MessageDto>> =
    messageFlows.getOrPut(threadId) { MutableStateFlow(emptyList()) }.asStateFlow()

  suspend fun loadMessages(threadId: String) {
    val items = api.getMessages(threadId).items
    messageFlows.getOrPut(threadId) { MutableStateFlow(emptyList()) }.value = items
  }

  suspend fun send(threadId: String, text: String) {
    // REST send only; WS pushes message.new afterwards.
    api.sendMessage(threadId, text)
  }
}
