package io.getstream.kmp.models

import kotlinx.serialization.Serializable

@Serializable enum class GenderPreference { male, female }

@Serializable
data class RealDto(
  val id: String,
  val createdAt: String,
  val expiresAt: String,
  val distanceMeters: Int,
  val media: MediaDto,
  val engagement: EngagementDto,
  val user: RealUserDto? = null
)

@Serializable data class MediaDto(val rearImageUrl: String, val frontImageUrl: String)
@Serializable data class EngagementDto(val likeCount: Int, val likedByMe: Boolean)
@Serializable data class RealUserDto(val id: String, val displayName: String? = null, val gender: String? = null)

@Serializable
data class NearbyRealsResponse(
  val items: List<RealDto>,
  val nextCursor: String? = null,
  val serverTime: String? = null
)

@Serializable
data class LikeResponse(
  val realId: String,
  val liked: Boolean,
  val likeCount: Int,
  val match: MatchDto
)

@Serializable
data class MatchDto(val isMutual: Boolean, val threadId: String? = null)
