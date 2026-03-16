package io.getstream.kmp.network

import io.getstream.kmp.auth.TokenProvider
import io.getstream.kmp.core.ApiConfig
import io.getstream.kmp.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

class HttpApi(
  private val tokenProvider: TokenProvider,
  private val client: HttpClient
) {
  companion object {
    fun create(tokenProvider: TokenProvider, engine: HttpClient): HttpApi {
      val configured = engine.config {
        install(ContentNegotiation) {
          json(Json { ignoreUnknownKeys = true; explicitNulls = false })
        }
        install(HttpTimeout) {
          requestTimeoutMillis = 30_000
          connectTimeoutMillis = 30_000
          socketTimeoutMillis = 30_000
        }
        install(Logging) {
          logger = Logger.DEFAULT
          level = LogLevel.HEADERS
        }
        defaultRequest {
          url(ApiConfig.HTTP_BASE_URL)
          header(HttpHeaders.Accept, ContentType.Application.Json)
        }
      }
      return HttpApi(tokenProvider, configured)
    }
  }

  private suspend fun HttpRequestBuilder.auth() {
    tokenProvider.getAccessToken()?.let { header(HttpHeaders.Authorization, "Bearer $it") }
  }

  // Reals
  suspend fun getNearbyReals(
    lat: Double, lng: Double,
    radiusMeters: Int,
    gender: GenderPreference?,
    cursor: String? = null,
    limit: Int = 30
  ): NearbyRealsResponse {

    val response = client.get("/v1/reals/nearby") {
      auth()
      parameter("lat", lat)
      parameter("lng", lng)
      parameter("radiusMeters", radiusMeters)
      gender?.let { parameter("gender", it.name) }
      cursor?.let { parameter("cursor", it) }
      parameter("limit", limit)
    }

    println("REmo11 " + response.body())

    return response.body()
  }

  suspend fun uploadReal(
    frontJpeg: ByteArray,
    rearJpeg: ByteArray,
    capturedAtIso: String,
    lat: Double,
    lng: Double,
    locationAccuracyMeters: Double? = null,
    idempotencyKey: String
  ): RealDto =
    client.post("/v1/reals") {
      auth()
      header("Idempotency-Key", idempotencyKey)
      setBody(
        MultiPartFormDataContent(
          formData {
            append("capturedAt", capturedAtIso)
            append("lat", lat.toString())
            append("lng", lng.toString())
            locationAccuracyMeters?.let { append("locationAccuracyMeters", it.toString()) }

            append("rearImage", rearJpeg, Headers.build {
              append(HttpHeaders.ContentType, "image/jpeg")
              append(HttpHeaders.ContentDisposition, "filename=\"rear.jpg\"")
            })
            append("frontImage", frontJpeg, Headers.build {
              append(HttpHeaders.ContentType, "image/jpeg")
              append(HttpHeaders.ContentDisposition, "filename=\"front.jpg\"")
            })
          }
        )
      )
    }.body()

  suspend fun likeReal(realId: String): LikeResponse =
    client.post("/v1/reals/$realId/likes") {
      auth()
      contentType(ContentType.Application.Json)
      setBody("{}")
    }.body()

  // Chat
  suspend fun getThreads(cursor: String? = null, limit: Int = 30): ThreadsResponse =
    client.get("/v1/threads") {
      auth()
      cursor?.let { parameter("cursor", it) }
      parameter("limit", limit)
    }.body()

  suspend fun getMessages(threadId: String, cursor: String? = null, limit: Int = 50): MessagesResponse =
    client.get("/v1/threads/$threadId/messages") {
      auth()
      cursor?.let { parameter("cursor", it) }
      parameter("limit", limit)
    }.body()

  suspend fun sendMessage(threadId: String, text: String): MessageDto =
    client.post("/v1/threads/$threadId/messages") {
      auth()
      contentType(ContentType.Application.Json)
      setBody(SendMessageRequest(text))
    }.body()
}
