package io.getstream.kmp.models

import kotlinx.serialization.Serializable

@Serializable
data class ThreadDto(
  val id: String,
  val createdAt: String,
  val otherUser: OtherUserDto,
  val lastMessage: MessageDto? = null,
  val unreadCount: Int = 0
)

@Serializable data class OtherUserDto(val id: String, val displayName: String? = null)

@Serializable
data class MessageDto(
  val id: String,
  val threadId: String,
  val senderUserId: String,
  val text: String,
  val createdAt: String
)

@Serializable data class ThreadsResponse(val items: List<ThreadDto>, val nextCursor: String? = null)
@Serializable data class MessagesResponse(val items: List<MessageDto>, val nextCursor: String? = null)
@Serializable data class SendMessageRequest(val text: String)
