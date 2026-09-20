package org.example

import kotlinx.serialization.Serializable

@Serializable
data class ErrorMessage(val error: String)
