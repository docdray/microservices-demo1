package org.example

import kotlinx.serialization.Serializable

@Serializable
data class BookInput(
    val title: String,
    val isbn: String,
    val authorIds: List<Long>
)

@Serializable
data class AuthorSummary(
    val id: Long,
    val firstName: String,
    val lastName: String
)

@Serializable
data class BookDto(
    val id: Long,
    val title: String,
    val isbn: String,
    val authors: List<AuthorSummary>
)
