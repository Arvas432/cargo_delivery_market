package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Account(
    val id: String? = null,
    val login: String,
    val passwordHash: String,
    val email: String,
    val role: String,
    val balance: Double = 0.0,
    val isBlocked: Boolean = false
)

