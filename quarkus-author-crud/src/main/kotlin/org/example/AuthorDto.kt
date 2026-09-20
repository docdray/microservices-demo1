package org.example

import kotlinx.serialization.Serializable

@Serializable
data class AuthorInput(
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

@Serializable
data class AuthorDto(
    val id: Long,
    val firstName: String,
    val lastName: String,
    val birthDate: String
)

fun Author.toDto() = AuthorDto(
    id = id!!,
    firstName = firstName,
    lastName = lastName,
    birthDate = birthDate.toString()
)
