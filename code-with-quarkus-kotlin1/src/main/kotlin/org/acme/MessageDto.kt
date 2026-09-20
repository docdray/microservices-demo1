package org.acme

import kotlinx.serialization.Serializable

@Serializable
data class MessageInput(
    val text: String
)

@Serializable
data class MessageDto(
    val id: Long,
    val text: String,
    val createdAt: String,
    val updatedAt: String
)

fun Message.toDto() = MessageDto(
    id = id!!,
    text = text,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString()
)
