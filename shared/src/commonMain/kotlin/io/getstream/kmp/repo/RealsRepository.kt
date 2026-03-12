package io.getstream.kmp.repo

import io.getstream.kmp.models.*
import io.getstream.kmp.network.HttpApi

class RealsRepository(private val api: HttpApi) {
  suspend fun nearby(lat: Double, lng: Double, radiusMeters: Int, gender: GenderPreference?) =
    api.getNearbyReals(lat, lng, radiusMeters, gender)

  suspend fun upload(
    frontJpeg: ByteArray,
    rearJpeg: ByteArray,
    capturedAtIso: String,
    lat: Double,
    lng: Double,
    idempotencyKey: String
  ) = api.uploadReal(frontJpeg, rearJpeg, capturedAtIso, lat, lng, idempotencyKey = idempotencyKey)

  suspend fun like(realId: String) = api.likeReal(realId)
}
