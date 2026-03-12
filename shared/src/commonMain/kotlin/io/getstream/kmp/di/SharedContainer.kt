package io.getstream.kmp.di

import io.getstream.kmp.auth.*
import io.getstream.kmp.network.*
import io.getstream.kmp.network.ws.ChatSocket
import io.getstream.kmp.repo.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class SharedContainer {
  private val secureStore = createSecureStore()
  val tokenStore = AuthTokenStore(secureStore)

  private val httpClient = HttpClientFactory.create()

  private val authHttpClient = HttpClientFactory.create().config {
    install(ContentNegotiation) {
      json(Json { ignoreUnknownKeys = true; explicitNulls = false })
    }
  }

  private val tokenProvider: TokenProvider =
    RefreshingTokenProvider(tokenStore, authHttpClient)

  private val api = HttpApi.create(tokenProvider, httpClient)
  private val socket = ChatSocket(tokenProvider, httpClient)

  val realsRepository = RealsRepository(api)
  val chatRepository = ChatRepository(api, socket)
}
