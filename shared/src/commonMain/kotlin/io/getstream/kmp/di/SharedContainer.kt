package io.getstream.kmp.di

import io.getstream.kmp.auth.*
import io.getstream.kmp.network.*
import io.getstream.kmp.network.ws.ChatSocket
import io.getstream.kmp.repo.*

class SharedContainer {
  private val secureStore = createSecureStore()
  val tokenStore = AuthTokenStore(secureStore)
  private val tokenProvider: TokenProvider = StoredTokenProvider(tokenStore)

  private val httpClient = HttpClientFactory.create()
  private val api = HttpApi.create(tokenProvider, httpClient)
  private val socket = ChatSocket(tokenProvider, httpClient)

  val realsRepository = RealsRepository(api)
  val chatRepository = ChatRepository(api, socket)
}
